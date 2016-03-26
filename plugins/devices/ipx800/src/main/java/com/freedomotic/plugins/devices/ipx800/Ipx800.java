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
package com.freedomotic.plugins.devices.ipx800;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author mauro
 */
public class Ipx800 extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Ipx800.class.getName());
    private static ArrayList<Board> boards = null;
    Map<String, Board> devices = new HashMap<String, Board>();
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private static String DELIMITER = ":";
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
        setDescription("Connected to "); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            String ledTag;
            String digitalInputTag;
            String analogInputTag;
            String autoConfiguration;
            String authentication;
            String pathAuthentication;
            String username;
            String password;
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
            authentication = configuration.getTuples().getStringProperty(i, "authentication", "false");
            pathAuthentication = configuration.getTuples().getStringProperty(i, "path-authentication", "");
            username = configuration.getTuples().getStringProperty(i, "username", "admin");
            password = configuration.getTuples().getStringProperty(i, "password", "pass");
            autoConfiguration = configuration.getTuples().getStringProperty(i, "auto-configuration", "false");
            objectClass = configuration.getTuples().getStringProperty(i, "object.class", "Light");
            Board board = new Board(ipToQuery, portToQuery, alias, relayNumber, analogInputNumber,
                    digitalInputNumber, startingRelay, ledTag, digitalInputTag, analogInputTag, autoConfiguration,
                    objectClass, authentication, username, password, pathAuthentication);
            boards.add(board);
            // add board object and its alias as key for the hashmap
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery);
        }
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        BOARD_NUMBER = configuration.getTuples().size();
        DELIMITER = configuration.getProperty("address-delimiter");
        setPollingWait(POLLING_TIME);
        loadBoards();
    }

    @Override
    public void onStop() {
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
        final Board b = board;
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.error(ex.getMessage());
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            if (board.getAuthentication().equalsIgnoreCase("true")) {
                Authenticator.setDefault(new Authenticator() {

                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(b.getUsername(), b.getPassword().toCharArray());
                    }
                });
                statusFileURL = "http://" + b.getIpAddress() + ":"
                        + Integer.toString(b.getPort()) + b.getPathAuthentication() + "/" + GET_STATUS_URL;
            } else {
                statusFileURL = "http://" + board.getIpAddress() + ":"
                        + Integer.toString(board.getPort()) + "/" + GET_STATUS_URL;
            }
            LOG.info("Ipx800 gets relay status from file {0}", statusFileURL);
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            LOG.error(Freedomotic.getStackTraceInfo(connEx));
            //disconnect();
            this.stop();
            this.setDescription("Connection timed out, no reply from the board at " + statusFileURL);
        } catch (SAXException ex) {
            //this.stop();
            LOG.error(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            //this.stop();
            setDescription("Unable to connect to " + statusFileURL);
            LOG.error(Freedomotic.getStackTraceInfo(ex));
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
                // control for storing value
                if (tag.equalsIgnoreCase("led")) {
                    if (!(board.getRelayStatus(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                        sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                        board.setRelayStatus(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                    }
                } else if (tag.equalsIgnoreCase("btn")) {
                    if (!(board.getDigitalInputValue(i).equalsIgnoreCase(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                        sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                        board.setDigitalInputValue(i, doc.getElementsByTagName(tagName).item(0).getTextContent());
                    }
                } else if (tag.equalsIgnoreCase("an") || tag.equalsIgnoreCase("analog")) {
                    if (!(board.getAnalogInputValue(i) == Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()))) {
                        sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent(), tag);
                        board.setAnalogInputValue(i, Integer.parseInt(doc.getElementsByTagName(tagName).item(0).getTextContent()));
                    }
                }
            } catch (DOMException domException) {
                LOG.error("DOMException " + domException.getLocalizedMessage());
            }
        }
    }

    private void sendChanges(int relayLine, Board board, String status, String tag) {
        relayLine++;
        //reconstruct freedomotic object address
        //ALIAS:LINE:TAG
        String address = board.getAlias() + DELIMITER + relayLine + DELIMITER + tag;
        //building the event
        ProtocolRead event = new ProtocolRead(this, "ipx800", address);
        // relay lines - status=0 -> off; status=1 -> on

        if (tag.equalsIgnoreCase("led")) {
            event.addProperty("inputValue", status);
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
        } else // digital inputs (btn tag) status = up -> off; status = dn -> on
        if (tag.equalsIgnoreCase("btn")) {
            if (status.equalsIgnoreCase("up")) {
                event.addProperty("isOn", "false");
            } else {
                event.addProperty("isOn", "true");
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
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    /**
     * Actuator side
     * @throws com.freedomotic.exceptions.UnableToExecuteException    */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection paramentes address:port from received freedomotic command
        address = c.getProperty("address").split(DELIMITER);
        Board board = (Board) devices.get(address[0]);
        try {
            sendToBoard(board, c);
        } catch (IOException ex) {
            LOG.error("Impossibile to send command " + ex.getLocalizedMessage());
        }
    }

    private void sendToBoard(Board board, Command c) throws IOException {
        try {
            URL url = null;
            URLConnection urlConnection;
            String delimiter = configuration.getProperty("address-delimiter");
            String[] address = c.getProperty("address").split(delimiter);
            Integer relayNumber = Integer.parseInt(address[1]) - 1;

            if (c.getProperty("command").equals("CHANGE-STATE-DIGITAL-INPUT")) {
                relayNumber = relayNumber + 100;
            }
            // if required set the authentication
            if (board.getAuthentication().equalsIgnoreCase("true")) {
                String authString = board.getUsername() + ":" + board.getPassword();
                byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
                String authStringEnc = new String(authEncBytes);
                //Create a URL for the desired  page   
                url = new URL("http://" + board.getIpAddress() + ":" + board.getPort() + board.getPathAuthentication() + "/" + CHANGE_STATE_RELAY_URL + relayNumber + "=" + c.getProperty("state-value"));
                urlConnection = url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            } else {
                //Create a URL for the desired  page   
                url = new URL("http://" + board.getIpAddress() + ":" + board.getPort() + "/" + CHANGE_STATE_RELAY_URL + relayNumber + "=" + c.getProperty("state-value"));
                urlConnection = url.openConnection();
            }
            LOG.info("Freedomotic sends the command " + url);
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String result = sb.toString();
        } catch (MalformedURLException e) {
            LOG.error("Command malformed URL " + e.toString());
        } catch (IOException e) {
            LOG.error("Command IOexception" + e.toString());
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
    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
