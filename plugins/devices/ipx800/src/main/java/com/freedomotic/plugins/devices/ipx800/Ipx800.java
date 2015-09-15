/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.ipx800;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
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

public class Ipx800 extends Protocol {

    private static final Logger LOG = Logger.getLogger(Ipx800.class.getName());
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
    private String CHANGE_STATE_RELAY_URL = configuration.getStringProperty("change-state-relay-url", "leds.cgi?led=");
    private String SEND_PULSE_RELAY_URL = configuration.getStringProperty("send-pulse-relay-url", "rlyfs.cgi?rlyf=");
   
    /**
     * Initializations
     */
    public Ipx800() {
        super("Ipx800", "/ipx800/ipx800-manifest.xml");
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
            analogInputTag = configuration.getTuples().getStringProperty(i, "analog-input-tag", "analog");
            autoConfiguration = configuration.getTuples().getStringProperty(i, "auto-configuration", "false");
            objectClass = configuration.getTuples().getStringProperty(i, "object.class", "Light");
            Board board = new Board(ipToQuery, portToQuery, alias, relayNumber, analogInputNumber,
                    digitalInputNumber, startingRelay, ledTag, digitalInputTag, analogInputTag, autoConfiguration, objectClass);
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

        LOG.log(Level.INFO, "Trying to connect to ipx800 board on address {0}:{1}", new Object[]{address, port});
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "Unable to connect to host {0} on port {1}", new Object[]{address, port});
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
        setDescription(configuration.getStringProperty("description", "Ipx800"));
    }

    @Override
    protected void onRun() {
        // select all boards in the devices hashmap and evaluate the status
        if (isRunning()) {
            Set<String> keySet = devices.keySet();
            for (String key : keySet) {
                Board board = devices.get(key);
                evaluateDiffs(getXMLStatusFile(board), board);
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
            Logger.getLogger(Ipx800.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/" + GET_STATUS_URL;
            LOG.log(Level.INFO, "Ipx800 gets relay status from file {0}", statusFileURL);
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
            NodeList nl = n.getChildNodes();
            valueTag(doc, board, board.getRelayNumber(), board.getLedTag(), 0);
            valueTag(doc, board, board.getDigitalInputNumber(), board.getDigitalInputTag(), 0);
            valueTag(doc, board, board.getAnalogInputNumber(), board.getAnalogInputTag(), 0);
        }
    }

    private void valueTag(Document doc, Board board, Integer nl, String tag, int startingRelay) {
        for (int i = startingRelay; i < nl; i++) {
            try {
                String tagName = tag + i;
                //Freedomotic.logger.severe("Ipx800 monitorizes tags " + tagName);
                // control for storing value
                if (tag.equalsIgnoreCase("led")) {
                    if (!(board.getRelayStatus(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                        sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                        board.setRelayStatus(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                    }
                } else if (tag.equalsIgnoreCase("btn")) {
                } else if (tag.equalsIgnoreCase("an") || tag.equalsIgnoreCase("analog")) {
                    if (tag.equalsIgnoreCase("an")) {
                        tagName = tag + (i + 1);
                    }
                    if (board.getanalogInputValue(i)
                            != Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent())) {
                        sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                        board.setAnalogInputValue(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                    }
                }
                // sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
            } catch (DOMException dOMException) {
                //do nothing
            } catch (NumberFormatException numberFormatException) {
                //do nothing
            } catch (NullPointerException ex) {
                //do nothing
            }
        }
    }

    private void sendChanges(int relayLine, Board board, String status, String tag) {
        relayLine++;
        //reconstruct freedomotic object address
        //String address = board.getIpAddress() + ":" + board.getPort() + ":" + relayLine + ":" + tag;
        String address = board.getAlias() + ":" + relayLine + ":" + tag;
        //LOG.info("Sending Ipx800 protocol read event for object address '" + address + "'. It's readed status is " + status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "ipx800", address); //IP:PORT:RELAYLINE
        // relay lines - status=0 -> off; status=1 -> on
        
        if (tag.equalsIgnoreCase("led")) {
            event.addProperty("inputValue", "0");
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
        } else // digital inputs (btn tag) status = up -> on; status = down -> off
        if (tag.equalsIgnoreCase("btn")) {
            if (status.equalsIgnoreCase("up")) {
                event.addProperty("isOn", "true");
            } else {
                event.addProperty("isOn", "false");
            }
            event.addProperty("inputValue", status);
        } else {
            // analog inputs (an/analog input) status = 0 -> off; status > 0 -> on
            if (tag.equalsIgnoreCase("an") || tag.equalsIgnoreCase("analog")) {
                if (status.equalsIgnoreCase("0")) {
                    event.addProperty("isOn", "false");
                } else {
                    event.addProperty("isOn", "true");
                }
                event.addProperty("inputValue", status);

            }
        }
        //adding some optional information to the event
        event.addProperty("boardIP", board.getIpAddress());
        event.addProperty("boardPort", new Integer(board.getPort()).toString());
        event.addProperty("relayLine", new Integer(relayLine).toString());

        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection paramentes address:port from received freedomotic command
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
            LOG.log(Level.SEVERE, "The object address ''{0}'' is not properly formatted. Check it!", c.getProperty("address"));
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            LOG.log(Level.SEVERE, "{0} is not a valid ethernet port to connect to", port_board);
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
                LOG.log(Level.SEVERE, "Unable to send the message to host {0} on port {1}", new Object[]{address[0], address[1]});
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
        Integer relay = 0;

        if (c.getProperty("command").equals("CHANGE-STATE-RELAY")) {
            //relay = Integer.parseInt(address[2]) - 1;
            relay = Integer.parseInt(address[1]) - 1;
            page = CHANGE_STATE_RELAY_URL + relay;
        }

        if (c.getProperty("command").equals("PULSE-RELAY")) {
            // mapping relay line -> protocol
            //relay = Integer.parseInt(address[2]) - 1;
            relay = Integer.parseInt(address[1]) - 1;
            //compose requested link
            page = SEND_PULSE_RELAY_URL + relay;
        }

        // http request sending to the board
        message = "GET /" + page + " HTTP 1.1\r\n\r\n";
        //LOG.info("Sending 'GET /" + page + " HTTP 1.1' to relay board");
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
