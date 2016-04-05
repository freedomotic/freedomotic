/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.zibase;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 *
 * @author Mauro Cicolella
 */
public class Zibase extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Zibase.class.getName());
    Map<String, Board> devices = new HashMap<String, Board>();
    private static Map<String, String> X10Map;
    private static Map<String, String> ZwaveMap;
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
        super("Zibase", "/zibase/zibase-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (devices == null) {
            devices = new HashMap<String, Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String alias = null;
            String ipToQuery;
            int portToQuery;
            alias = configuration.getTuples().getStringProperty(i, "alias", "default");
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.0.115");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            Board board = new Board(alias, ipToQuery, portToQuery);
            //boards.add(board);
            // add board object and its alias as key for the hashmap
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to Zibase board on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            LOG.error("Unable to connect to host " + address + " on port " + port);
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
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        BOARD_NUMBER = configuration.getTuples().size();
        setPollingWait(POLLING_TIME);
        initX10Map(); // inizialize X10 addresses hash map
        initZwaveMap(); // inizialize Zwave addresses hash map
        loadBoards();
        initializeEnvironmentObjects();
    }

    @Override
    public void onStop() {
        //release resources
        devices.clear();
        devices = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Zibase"));
    }

    @Override
    protected void onRun() {
        // select all boards in the devices hashmap and evaluate the status
        Set<String> keySet = devices.keySet();
        for (String key : keySet) {
            Board board = devices.get(key);
            evaluateDiffs(getXMLStatusFile(board), board);
        }
        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    private Document getXMLStatusFile(Board board) {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/" + GET_SENSORS_URL;
            LOG.info("Zibase gets relay status from file " + statusFileURL); // FOR DEBUG
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            disconnect();
            this.stop();
            this.setDescription("Connection timed out, no reply from the board at " + statusFileURL);
        } catch (SAXException ex) {
            disconnect();
            this.stop();
            LOG.error(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            disconnect();
            this.stop();
            setDescription("Unable to connect to " + statusFileURL);
            LOG.error(Freedomotic.getStackTraceInfo(ex));
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
            // split <x10tab> in groups of 4 digits (each of them is an x10 houseCode A,B,C...)
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
                        sendChanges(board, address, status, "DOMIA");
                        sendChanges(board, address, status, "XDD433ALRM");
                        sendChanges(board, address, status, "XDD868ALRM");
                        sendChanges(board, address, status, "XDD868INTER");
                        sendChanges(board, address, status, "X10");
                    }
                }
            }
            // read <zwtab> element from sensors.xml
            String zwtab = doc.getElementsByTagName("zwtab").item(0).getTextContent();
            // split <zwtab> in groups of 4 digits (each of them is an x10 houseCode A,B,C...)
            x10Address = zwtab.split("(?<=\\G.{4})");
            for (int i = 0; i < x10Address.length; i++) {
                String[] x10AddressDigits = x10Address[i].split("(?<=\\G.{1})");
                String x10AddressReordered = x10AddressDigits[2] + x10AddressDigits[3] + x10AddressDigits[0] + x10AddressDigits[1];
                System.out.println(x10HouseCode[i] + x10AddressReordered + " converted " + hexToBinary(x10AddressReordered));
                String[] deviceAddress = hexToBinary(x10AddressReordered).split("(?<=\\G.{1})");
                for (int j = 0; j < deviceAddress.length; j++) {
                    int step = 16 - j;
                    // zwave address in the format Z+HouseCode+Device e.g. ZA1, ZB3 ...
                    address = "Z" + x10HouseCode[i] + step;
                    status = deviceAddress[j];
                    // if actual device status is different from stored one
                    // notify the event and change the value in the map
                    if (!ZwaveMap.get(address).equalsIgnoreCase(status)) {
                        ZwaveMap.put(address, status);
                        sendChanges(board, address, status, "ZWAVE");
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
                //address = board.getIpAddress() + ":" + board.getPort() + ":" + protocol + id;
                address = board.getAlias() + ":" + protocol + id;
                // print out its manager attribute value
                //System.out.println("Device = " + protocol + id + " v1=" + v1 + " v2=" + v2 + " lowbatt=" + lowbatt);
                ProtocolRead event = new ProtocolRead(this, "zibase", address); //object address ALIAS:PROTOCOL+ID
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
        String address = board.getAlias() + ":" + x10Address + ":" + protocol;
        //String address = board.getIpAddress() + ":" + board.getPort() + ":" + x10Address + ":" + protocol;
        LOG.error("Sending Zibase protocol read event for object address '" + address + "'. It's readed status is " + status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "zibase", address); //object address ALIAS:X10ADDRESS:PROTOCOL
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
     *
     * @throws com.freedomotic.exceptions.UnableToExecuteException
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection paramentes address:port from received freedom command
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        //Board board = (Board) getKeyFromValue(devices, address[0]);
        Board board = (Board) devices.get(address[0]);
        String ip_board = board.getIpAddress();
        int port_board = board.getPort();
        String address_object = address[1];
        String protocol_object = address[2];
        //connect to the Zibase board
        boolean connected = false;
        try {
            connected = connect(ip_board, port_board);
        } catch (ArrayIndexOutOfBoundsException outEx) {
            LOG.error("The object address '" + c.getProperty("address") + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            LOG.error(port_board + " is not a valid ethernet port to connect to");
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
                LOG.error("Unable to send the message to host " + address[0] + " on port " + address[1]);
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
    /**
     *
     * @param c
     * @return
     */
    public String createMessage(Command c) {
        String message = null;
        String page = null;
        String control = c.getProperty("control").toUpperCase();
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        Board board = (Board) devices.get(address[0]);
        String ipBoard = board.getIpAddress();
        int portBoard = board.getPort();
        String addressObject = address[1];
        String protocolObject = address[2];
        String protocol = configuration.getStringProperty(protocolObject, "X10");

        //  if (cmd.equals("DIM")) {
        //relay = HexIntConverter.convert(Integer.parseInt(address[2]) - 1);
        //relay = HexIntConverter.convert(Integer.parseInt(address[2]));
        //    page = "cgi-bin/domo.cgi?cmd=" + relay;
        //} else
        page = SEND_COMMANDS_URL + control + "%20" + addressObject + "%20" + protocol;

        // http request sending to the board
        message = "GET /" + page + " HTTP 1.0\r\n\r\n";
        LOG.info("Sending 'GET /" + page + " HTTP 1.1' to Zibase board");
        return (message);
    }

    /**
     * Converts an hexadecimal digit to a 4 binary digits format
     *
     * @param hexString
     * @return
     */
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
    // A1 = 0, A2 =0 etc...
    // this map stores the status of each device
    /**
     *
     */
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

    // Inizializes Zwave x10 addresses map to "0" - all devices OFF
    // ZA1 = 0, ZA2 = 0 etc...
    // this map stores the status of each device
    /**
     *
     */
    public static void initZwaveMap() {
        ZwaveMap = new HashMap();
        String prefix = "Z";

        for (int i = 1; i <= 16; i++) {
            ZwaveMap.put(prefix + "A" + i, "0");
            ZwaveMap.put(prefix + "B" + i, "0");
            ZwaveMap.put(prefix + "C" + i, "0");
            ZwaveMap.put(prefix + "D" + i, "0");
            ZwaveMap.put(prefix + "E" + i, "0");
            ZwaveMap.put(prefix + "F" + i, "0");
            ZwaveMap.put(prefix + "G" + i, "0");
            ZwaveMap.put(prefix + "H" + i, "0");
            ZwaveMap.put(prefix + "I" + i, "0");
            ZwaveMap.put(prefix + "J" + i, "0");
            ZwaveMap.put(prefix + "K" + i, "0");
            ZwaveMap.put(prefix + "L" + i, "0");
            ZwaveMap.put(prefix + "M" + i, "0");
            ZwaveMap.put(prefix + "N" + i, "0");
            ZwaveMap.put(prefix + "O" + i, "0");
            ZwaveMap.put(prefix + "P" + i, "0");
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

    // retrieve a key from value in the hashmap 
    /**
     *
     * @param hm
     * @param value
     * @return
     */
    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    // initialize configured environment objects 
    /**
     *
     */
    public void initializeEnvironmentObjects() {
        List<EnvObjectLogic> objectsList = getApi().things().findByProtocol("zibase");
        if (objectsList.size() > 0) {
            for (EnvObjectLogic obj : objectsList) {
                String objectAddress = obj.getPojo().getPhisicalAddress();
                String delimiter = configuration.getProperty("address-delimiter");
                address = objectAddress.split(delimiter);
                if (address.length == 3) {
                    if (address[2].equalsIgnoreCase("ZWAVE")) {
                        ZwaveMap.put(address[1], "-1");
                    } else {
                        X10Map.put(address[1], "-1");
                    }
                }
            }
        }
    }
}
