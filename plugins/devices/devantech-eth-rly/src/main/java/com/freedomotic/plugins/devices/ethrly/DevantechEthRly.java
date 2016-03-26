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
package com.freedomotic.plugins.devices.ethrly;

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
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Mauro Cicolella
 */
public class DevantechEthRly extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(DevantechEthRly.class.getName());
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

    /**
     * Initializations
     */
    public DevantechEthRly() {
        super("Devantech EthRly", "/devantech-eth-rly/devantech-eth-rly-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
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
            String username;
            String password;
            String httpAuthentication;
            int portToQuery;
            int relayNumber;
            // filter the tuples with "object.class" property
            String result = configuration.getTuples().getProperty(i, "object.class");
            // if the tuple hasn't an "object.class" property it's a board configuration one 
            if (result == null) {
                ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
                portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
                alias = configuration.getTuples().getStringProperty(i, "alias", "default");
                username = configuration.getTuples().getStringProperty(i, "username", "admin");
                password = configuration.getTuples().getStringProperty(i, "password", "password");
                httpAuthentication = configuration.getTuples().getStringProperty(i, "http-authentication", "true");
                relayNumber = configuration.getTuples().getIntProperty(i, "relay-number", 8);
                autoConfiguration = configuration.getTuples().getStringProperty(i, "auto-configuration", "false");
                objectClass = configuration.getTuples().getStringProperty(i, "object.class", "Light");
                Board board = new Board(ipToQuery, portToQuery, alias, relayNumber, autoConfiguration, objectClass, username, password, httpAuthentication);
                // add board object and its alias as key for the hashmap
                devices.put(alias, board);
            }
            //setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to Devantech Eth-Rly board on address {}", address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            LOG.error("Unable to connect to host {} on port {}", address, port);
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
        loadBoards();
    }

    @Override
    public void onStop() {
        //release resources
        devices.clear();
        devices = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Devantech Eth-Rly"));
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
        URL url = null;
        try {
            String authString = board.getUsername() + ":" + board.getPassword();
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            //Create a URL for the desired  page 
            url = new URL("http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/" + GET_STATUS_URL);
            URLConnection urlConnection = url.openConnection();
            // if required set the authentication
            if (board.getHttpAuthentication().equalsIgnoreCase("true")) {
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }
            LOG.info("Devantech Eth-Rly gets relay status from file {}", url);
            doc = dBuilder.parse(urlConnection.getInputStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            disconnect();
            this.stop();
            this.setDescription("Connection timed out, no reply from the board at " + url);
        } catch (SAXException ex) {
            disconnect();
            this.stop();
            LOG.error(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            disconnect();
            this.stop();
            setDescription("Unable to connect to " + url);
            LOG.error(Freedomotic.getStackTraceInfo(ex));
        }
        return doc;
    }

    private void evaluateDiffs(Document doc, Board board) {
        //parses xml
        if (doc != null && board != null) {
            Node n = doc.getFirstChild();
            NodeList nl = n.getChildNodes();
            valueTag(doc, board, board.getRelayNumber(), "led", 0);
        }
    }

    private void valueTag(Document doc, Board board, Integer nl, String tag, int startingRelay) {
        for (int i = startingRelay; i < nl; i++) {
            try {
                String tagName = tag + i;
                // control for storing value
                if (!(board.getRelayStatus(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                    sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent());
                    board.setRelayStatus(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                    //System.out.println("led" + i + " status " + Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                }
            } catch (DOMException dOMException) {
                //do nothing
            } catch (NumberFormatException numberFormatException) {
                //do nothing
            } catch (NullPointerException ex) {
                //do nothing
            }
        }
    }

    private void sendChanges(int relayLine, Board board, String status) {
        relayLine++;
        //reconstruct freedomotic object address
        String address = board.getAlias() + ":" + relayLine;
        LOG.info("Sending Devantech Eth-Rly protocol read event for object address '{}'. It's readed status is {}", address, status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "devantech-eth-rly", address); //ALIAS:RELAYLINE
        if (status.equals("0")) {
            event.addProperty("isOn", "false");
        } else {
            event.addProperty("isOn", "true");
        }
        //if autoconfiguration is true create an object if not already exists
        if (board.getAutoConfiguration().equalsIgnoreCase("true")) {
            event.addProperty("object.class", board.getObjectClass());
            event.addProperty("object.name", address);
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
     *
     * @throws com.freedomotic.exceptions.UnableToExecuteException
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
                LOG.error("Unable to send the message to host {} on port ", ip_board, port_board);
                System.err.println(iOException);
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

    /**
     * Create message to send to the board. This part must be changed to reflect
     * board protocol
     *
     * @param c
     * @return
     */
    public String createMessage(Command c) {
        String message = null;
        String page = null;
        Integer relay = 0;

        relay = Integer.parseInt(address[1]) - 1;
        page = CHANGE_STATE_RELAY_URL + relay;

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
}
