/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.zibase;

/**
 * Plugin for Zibase board by zodianet.com
 *
 * @author Mauro Cicolella - www.emmecilab.net
 */
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 */
public class Zibase extends Protocol {

    private static ArrayList<Board> boards = null;
    private static Map<String, String> X10Map;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private String GET_SENSORS_URL = configuration.getStringProperty("get-sensors-url", "sensors.xml");
    private String SEND_COMMANDS_URL = configuration.getStringProperty("send-commands-url", "cgi-bin/domo.cgi?cmd=");

    /**
     * Initializations
     */
    public Zibase() {
        super("Zibase", "/it.cicolella.zibase/zibase.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            int portToQuery;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.0.115");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            Board board = new Board(ipToQuery, portToQuery);
            boards.add(board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        Freedomotic.logger.info("Trying to connect to Zibase board on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            Freedomotic.logger.severe("Unable to connect to host " + address + " on port " + port);
            return false;
        }
    }

    private void disconnect() {
        // close streams and socket
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception ex) {
            //do nothing. Best effort
        }
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        BOARD_NUMBER = configuration.getTuples().size();
        setPollingWait(POLLING_TIME);
        initX10Map(); // inizialize X10 addresses hash map
        loadBoards();
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        boards.clear();
        boards = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Zibase"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            evaluateDiffs(getXMLStatusFile(board), board); //parses the xml and crosscheck the data with the previous read
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(Zibase.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private Document getXMLStatusFile(Board board) {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(Zibase.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/" + GET_SENSORS_URL;
            //statusFileURL = "http://lnx.virtualcalcio.com/sensors.xml";
            //Freedomotic.logger.info("Zibase gets relay status from file " + statusFileURL); // FOR DEBUG
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            disconnect();
            this.stop();
            this.setDescription("Connection timed out, no reply from the board at " + statusFileURL);
        } catch (SAXException ex) {
            disconnect();
            this.stop();
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            disconnect();
            this.stop();
            setDescription("Unable to connect to " + statusFileURL);
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        }
        return doc;
    }

    private void evaluateDiffs(Document doc, Board board) {
        String[] x10HouseCode = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "Z"};
        String address = null;
        String status = null;
        //parses xml
        if (doc != null && board != null) {
            Node n = doc.getFirstChild();
            NodeList nl = n.getChildNodes();
            // read <x10tab> element from sensors.xml
            String x10tab = doc.getElementsByTagName("x10tab").item(0).getTextContent();
            // split <x10tab> in groups of 4 digits (each of them is an x10 houseCode
            String[] x10Address = x10tab.split("(?<=\\G.{4})");
            for (int i = 0; i < x10Address.length; i++) {
                String[] x10AddressDigits = x10Address[i].split("(?<=\\G.{1})");
                String x10AddressReordered = x10AddressDigits[2] + x10AddressDigits[3] + x10AddressDigits[0] + x10AddressDigits[1];
                System.out.println(x10HouseCode[i] + x10AddressReordered + " converted " + hexToBinary(x10AddressReordered));
                String[] deviceAddress = hexToBinary(x10AddressReordered).split("(?<=\\G.{1})");
                for (int j = 0; j < deviceAddress.length; j++) {
                    int step = 16 - j;
                    address = x10HouseCode[i] + step;
                    status = deviceAddress[j];
                    // if actual device status is different from stored one
                    // notify the event and change the value in the map
                    if (!X10Map.get(address).equalsIgnoreCase(status)) {
                        X10Map.put(address, status);
                        sendChanges(board, address, status, "CHACON");
                        sendChanges(board, address, status, "XDD");
                        sendChanges(board, address, status, "X10");
                    }
                }
            }
            // for sensor devices get all <ev> tags in the sensors.xml
            NodeList evs = doc.getElementsByTagName("ev");
            for (int i = 0; i < evs.getLength(); i++) {
                // for every <ev> tag
                Element ev = (Element) evs.item(i);
                String protocol = ev.getAttribute("pro");
                String id = ev.getAttribute("id");
                String v1 = ev.getAttribute("v1");
                String v2 = ev.getAttribute("v2");
                String lowbatt = ev.getAttribute("lowbatt");
                address = board.getIpAddress() + ":" + board.getPort() + ":" + protocol + id;
                // print out its manager attribute value
                //System.out.println("Device = " + protocol + id + " v1=" + v1 + " v2=" + v2 + " lowbatt=" + lowbatt);
                ProtocolRead event = new ProtocolRead(this, "zibase", address); //IP:PORT:PROTOCOl+ID
                event.addProperty("sensor.v1", v1);
                event.addProperty("sensor.v2", v2);
                event.addProperty("sensor.lowbatt", lowbatt);
                //publish the event on the messaging bus
                this.notifyEvent(event);
            }
        }
    }

    private void sendChanges(Board board, String x10Address, String status, String protocol) {
        //reconstruct freedomotic object address
        String address = board.getIpAddress() + ":" + board.getPort() + ":" + x10Address + ":" + protocol;
        //Freedomotic.logger.info("Sending Zibase protocol read event for object address '" + address + "'. It's readed status is " + status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "zibase", address); //IP:PORT:X10ADDRESS:PROTOCOL
        if (status.equals("0")) {
            event.addProperty("isOn", "false");
        } else {
            event.addProperty("isOn", "true");
        }
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection paramentes address:port from received freedom command
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        String ip_board = address[0];
        String port_board = address[1];
        String address_object = address[2];
        String protocol_object = address[3];
        //connect to the ethernet board
        boolean connected = false;
        try {
            connected = connect(ip_board, Integer.parseInt(port_board));
        } catch (ArrayIndexOutOfBoundsException outEx) {
            Freedomotic.logger.severe("The object address '" + c.getProperty("address") + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            Freedomotic.logger.severe(port_board + " is not a valid ethernet port to connect to");
            throw new UnableToExecuteException();
        }

        if (connected) {
            String message = createMessage(c);
            String expectedReply = c.getProperty("expected-reply");
            try {
                String reply = sendToBoard(message);
                if ((reply != null) && (!reply.equals(expectedReply))) {
                    //TODO: implement reply check
                }
            } catch (IOException iOException) {
                setDescription("Unable to send the message to host " + address[0] + " on port " + address[1]);
                Freedomotic.logger.severe("Unable to send the message to host " + address[0] + " on port " + address[1]);
                throw new UnableToExecuteException();
            } finally {
                disconnect();
            }
        } else {
            throw new UnableToExecuteException();
        }
    }

    private String sendToBoard(String message) throws IOException {
        String receivedReply = null;
        if (outputStream != null) {
            outputStream.writeBytes(message);
            outputStream.flush();
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try {
                receivedReply = inputStream.readLine(); // read device reply
            } catch (IOException iOException) {
                throw new IOException();
            }
        }
        return receivedReply;
    }

    // create message to send to the board
    // this part must be changed to relect board protocol
    public String createMessage(Command c) {
        String message = null;
        String page = null;
        String cmd = c.getProperty("control").toUpperCase();
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        String ip_board = address[0];
        String port_board = address[1];
        String address_object = address[2];
        String protocol_object = address[3];
        String protocol = configuration.getStringProperty(protocol_object, "X10");

        //  if (cmd.equals("DIM")) {
        //relay = HexIntConverter.convert(Integer.parseInt(address[2]) - 1);
        //relay = HexIntConverter.convert(Integer.parseInt(address[2]));
        //    page = "cgi-bin/domo.cgi?cmd=" + relay;
        //} else
        page = SEND_COMMANDS_URL + cmd + "%20" + address_object + "%20" + protocol;

        // http request sending to the board
        message = "GET /" + page + " HTTP 1.0\r\n\r\n";
        Freedomotic.logger.info("Sending 'GET /" + page + " HTTP 1.1' to Zibase board");
        return (message);
    }

    // convert an hexadecimal digit to binary format
    public static String hexToBinary(String hexString) {
        String[] hexDigit = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F"};
        String[] binary = {"0000", "0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"};
        String result = "";

        for (int i = 0; i < hexString.length(); i++) {
            char temp = hexString.charAt(i);
            String temp2 = "" + temp + "";
            for (int j = 0; j < hexDigit.length; j++) {
                if (temp2.equalsIgnoreCase(hexDigit[j])) {
                    result = result + binary[j];
                }
            }
        }
        return (result);
    }

    // inizialize the x10 addresses map to "0" - all devices OFF
    // this map stores the status of each device
    public static void initX10Map() {
        X10Map = new HashMap();

        for (int i = 1; i <= 16; i++) {
            X10Map.put("A" + i, "0");
            X10Map.put("B" + i, "0");
            X10Map.put("C" + i, "0");
            X10Map.put("D" + i, "0");
            X10Map.put("E" + i, "0");
            X10Map.put("F" + i, "0");
            X10Map.put("G" + i, "0");
            X10Map.put("H" + i, "0");
            X10Map.put("I" + i, "0");
            X10Map.put("J" + i, "0");
            X10Map.put("K" + i, "0");
            X10Map.put("L" + i, "0");
            X10Map.put("M" + i, "0");
            X10Map.put("N" + i, "0");
            X10Map.put("O" + i, "0");
            X10Map.put("P" + i, "0");
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
