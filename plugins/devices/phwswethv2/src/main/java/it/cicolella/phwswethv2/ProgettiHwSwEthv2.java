/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.cicolella.phwswethv2;

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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class ProgettiHwSwEthv2 extends Protocol {

    private static final Logger LOG = Logger.getLogger(ProgettiHwSwEthv2.class.getName());
    private static ArrayList<Board> boards = null;
    Map<String, Board> devices = new HashMap<String, Board>();
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private String GET_STATUS_URL = configuration.getStringProperty("get-status-url", "status.xml");
    private String CHANGE_STATE_RELAY_URL = configuration.getStringProperty("change-state-relay-url", "forms.htm?led");
    private String TOGGLE_RELAY_URL = configuration.getStringProperty("toggle-relay-url", "toggle.cgi?toggle=");

    /**
     * Initializations
     */
    public ProgettiHwSwEthv2() {
        super("ProgettiHwSwEth", "/phwswethv2/phwswethv2-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        if (devices == null) {
            devices = new HashMap<String, Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            String ledTag;
            String digitalInputTag;
            String analogInputTag;
            String autoConfiguration;
            String objectClass;
            String alias;
            String monitorRelay;
            String monitorAnalogInput;
            String monitorDigitalInput;
            int portToQuery;
            int digitalInputNumber;
            int analogInputNumber;
            int relayNumber;
            int startingRelay;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            alias = configuration.getTuples().getStringProperty(i, "alias", "default");
            relayNumber = configuration.getTuples().getIntProperty(i, "relay-number", 8);
            analogInputNumber = configuration.getTuples().getIntProperty(i, "analog-input-number", 4);
            digitalInputNumber = configuration.getTuples().getIntProperty(i, "digital-input-number", 4);
            startingRelay = configuration.getTuples().getIntProperty(i, "starting-relay", 0);
            ledTag = configuration.getTuples().getStringProperty(i, "led-tag", "led");
            digitalInputTag = configuration.getTuples().getStringProperty(i, "digital-input-tag", "btn");
            analogInputTag = configuration.getTuples().getStringProperty(i, "analog-input-tag", "pot");
            autoConfiguration = configuration.getTuples().getStringProperty(i, "auto-configuration", "false");
            monitorRelay = configuration.getTuples().getStringProperty(i, "monitor-relay", "true");
            monitorAnalogInput = configuration.getTuples().getStringProperty(i, "monitor-analog-input", "true");
            monitorDigitalInput = configuration.getTuples().getStringProperty(i, "monitor-digital-input", "true");
            objectClass = configuration.getTuples().getStringProperty(i, "object.class", "Light");
            Board board = new Board(ipToQuery, portToQuery, alias, relayNumber, analogInputNumber,
                    digitalInputNumber, startingRelay, ledTag, digitalInputTag, analogInputTag, autoConfiguration, objectClass,
                    monitorRelay, monitorAnalogInput, monitorDigitalInput);
            boards.add(board);
            // add board object and its alias as key for the hashmap
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to ProgettiHwSw board on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            LOG.severe("Unable to connect to host " + address + " on port " + port);
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
        loadBoards();
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        boards.clear();
        boards = null;
        devices.clear();
        devices = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "ProgettiHwSwEth"));
    }

    @Override
    protected void onRun() {
        //for (Board board : boards) {
        //  evaluateDiffs(getXMLStatusFile(board), board); //parses the xml and crosscheck the data with the previous read
        // select all boards in the devices hashmap and evaluate the status
        Set<String> keySet = devices.keySet();
        for (String key : keySet) {
            Board board = devices.get(key);
            evaluateDiffs(getXMLStatusFile(board), board);
        }

        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(ProgettiHwSwEthv2.class.getName()).log(Level.SEVERE, null, ex);
        }
        //}
    }

    private Document getXMLStatusFile(Board board) {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(ProgettiHwSwEthv2.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/" + GET_STATUS_URL;
            LOG.info("ProgettiHwSwEth gets relay status from file " + statusFileURL);
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            disconnect();
            this.stop();
            this.setDescription("Connection timed out, no reply from the board at " + statusFileURL);
        } catch (SAXException ex) {
            disconnect();
            this.stop();
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            disconnect();
            this.stop();
            setDescription("Unable to connect to " + statusFileURL);
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        }
        return doc;
    }

    private void evaluateDiffs(Document doc, Board board) {
        //parses xml
        if (doc != null && board != null) {
            Node n = doc.getFirstChild();
            if (board.getMonitorRelay().equalsIgnoreCase("true")) {
                valueTag(doc, board, board.getRelayNumber(), board.getLedTag(), 0);
            }
            if (board.getMonitorDigitalInput().equalsIgnoreCase("true")) {
                valueTag(doc, board, board.getDigitalInputNumber(), board.getDigitalInputTag(), 0);
            }
            if (board.getMonitorAnalogInput().equalsIgnoreCase("true")) {
                valueTag(doc, board, board.getAnalogInputNumber(), board.getAnalogInputTag(), 0);
            }
        }
    }

    private void valueTag(Document doc, Board board, Integer nl, String tag, int startingRelay) {
        for (int i = startingRelay; i < nl; i++) {
            try {
                String tagName = tag + HexIntConverter.convert(i);
                // control for storing value
                if (tag.equalsIgnoreCase(board.getLedTag())) {
                  if (!(board.getRelayStatus(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                     sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                     board.setRelayStatus(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                 }
                }
                if (tag.equalsIgnoreCase(board.getAnalogInputTag())) {
                  if (!(board.getAnalogInputValue(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                     sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                     board.setAnalogInputValue(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                 }
                }
                 if (tag.equalsIgnoreCase(board.getDigitalInputTag())) {
                  if (!(board.getDigitalInputValue(i) == doc.getElementsByTagName(tagName).item(0).getTextContent())) {
                     sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                     board.setDigitalInputValue(i, doc.getElementsByTagName(tagName).item(0).getTextContent());
                 }
                }
                //else
                //sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
            } catch (DOMException dOMException) {
                //do nothing
                LOG.severe("DOMException " + dOMException);
            } catch (NumberFormatException numberFormatException) {
                //do nothing
            } catch (NullPointerException ex) {
                //do nothing
            }
        }
    }

    private void sendChanges(int relayLine, Board board, String status, String tag) {
        // if starting-relay = 0 then increments relayLine to start from 1 not from zero
        if (board.getStartingRelay() == 0) {
            relayLine++;
        }
        //reconstruct freedomotic object address
        String address = board.getAlias() + ":" + relayLine + ":" + tag;
        LOG.info("Sending ProgettiHwSw protocol read event for object address '" + address + "'. It's readed status is " + status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "phwswethv2", address); //IP:PORT:RELAYLINE
        // relay lines - status=0 -> off; status=1 -> on
        if (tag.equalsIgnoreCase(board.getLedTag())) {
            if (status.equals("0")) {
                event.addProperty("isOn", "false");
            } else {
                event.addProperty("isOn", "true");
                //if autoconfiguration is true create an object if not already exists
                if (board.getAutoConfiguration().equalsIgnoreCase("true")) {
                    event.addProperty("object.class", board.getObjectClass());
                    event.addProperty("object.name", address);
                }
            }
        } else // digital inputs status = up -> off/open; status = dn -> on/closed
        if (tag.equalsIgnoreCase(board.getDigitalInputTag())) {
            if (status.equalsIgnoreCase("up")) {
                event.addProperty("isOn", "false");
                event.addProperty("isOpen", "true");
            } else {
                event.addProperty("isOn", "true");
                event.addProperty("isOpen", "false");
            }

        } else {
            // analog inputs status = 0 -> off; status > 0 -> on
            if (tag.equalsIgnoreCase(board.getAnalogInputTag())) {
                if (status.equalsIgnoreCase("0")) {
                    event.addProperty("isOn", "false");
                } else {
                    event.addProperty("isOn", "true");
                }
                event.addProperty("analog.input.value", status);
            }

        }
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection parameters address:port from received freedomotic command
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        Board board = (Board) devices.get(address[0]);
        String ip_board = board.getIpAddress();
        int port_board = board.getPort();
        //connect to the ethernet board
        boolean connected = false;
        try {
            //connected = connect(address[0], Integer.parseInt(address[1]));
            connected = connect(ip_board, port_board);
        } catch (ArrayIndexOutOfBoundsException outEx) {
            LOG.severe("The object address '" + c.getProperty("address") + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            LOG.severe(port_board + " is not a valid ethernet port to connect to");
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
                LOG.severe("Unable to send the message to host " + address[0] + " on port " + address[1]);
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
        String relay = null;

        relay = HexIntConverter.convert(Integer.parseInt(address[1]) - 1);

        if (c.getProperty("command").equals("CHANGE-STATE-RELAY")) {
            page = CHANGE_STATE_RELAY_URL + relay + "=" + c.getProperty("behavior");
        }

        if (c.getProperty("command").equals("TOGGLE-RELAY")) {
            relay = address[1];
            int time = Integer.parseInt(c.getProperty("time-in-ms"));
            int seconds = time / 1000;
            String relayLine = configuration.getProperty("TOGGLE" + seconds + "S" + relay);
            //compose requested link
            page = TOGGLE_RELAY_URL + relayLine;
        }

        // http request sending to the board
        message = "GET /" + page + " HTTP 1.1\r\n\r\n";
        LOG.info("Sending 'GET /" + page + " HTTP 1.1' to relay board");
        return (message);
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
    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
