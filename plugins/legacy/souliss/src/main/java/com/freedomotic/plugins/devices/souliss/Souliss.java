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
package com.freedomotic.plugins.devices.souliss;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.JsonNode;
import java.net.ConnectException;
import java.net.URL;
import java.nio.charset.Charset;
import org.json.JSONObject;
import org.json.JSONException;

/**
 * @author Mauro Cicolella -
 *
 */
public class Souliss extends Protocol {

    private static final Logger LOG = Logger.getLogger(Souliss.class.getName());
    private static ArrayList<Board> boards = null;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);

    /**
     * Initializations
     */
    public Souliss() {
        super("Souliss", "/souliss/souliss.xml");
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
            String statusToQuery;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            statusToQuery = configuration.getTuples().getStringProperty(i, "status-to-query", "http://192.168.1.201:80/status");
            Board board = new Board(ipToQuery, portToQuery, statusToQuery);
            boards.add(board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to Souliss node on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            LOG.severe("Unable to connect to host " + address + " on port " + port + " Exception reported: " + e.toString());
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
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Souliss"));
    }

    @Override
    protected void onRun() {
        for (Board node : boards) {
            evaluateDiffs(getJsonStatusFile(node), node); //parses the xml and crosscheck the data with the previous read
        }
        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            LOG.severe("Thread interrupted Exception reported: " + ex.toString());
            Logger.getLogger(Souliss.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JsonNode getJsonStatusFile(Board board) {
        //get the json stream from the socket connection
        String statusFileURL = null;
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        //statusFileURL = "http://" + board.getIpAddress() + ":"
        //      + Integer.toString(board.getPort()) + "/status";
        statusFileURL = board.getStatusToQuery();
        LOG.info("Souliss Sensor gets nodes status from file " + statusFileURL);
        try {
            // add json server http
            rootNode = mapper.readValue(readJsonFromUrl(statusFileURL), JsonNode.class);
        } catch (IOException ex) {
            LOG.severe("JSON server IOException reported: " + ex.toString());
            Logger.getLogger(Souliss.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            LOG.severe("JSONException reported: " + ex.toString());
            Logger.getLogger(Souliss.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rootNode;
    }

    public static String readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            System.out.println("Json string from server " + jsonText);
            // find the json start point 
            int startJson = jsonText.indexOf('(');
            jsonText = jsonText.substring(startJson + 1, jsonText.length() - 1);
            System.out.println("Json string filtered " + jsonText);
            JSONObject json = new JSONObject(jsonText);
            return jsonText;
        } finally {
            is.close();
        }
    }

    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    private void evaluateDiffs(JsonNode rootNode, Board board) {
        int id = 0;
        int slot = 0;
        String typical = null;
        String val = null;
        //parses json
        if (rootNode != null && board != null) {
            id = 0;
            for (JsonNode node : rootNode.path("id")) {
                String hlt = node.path("hlt").getTextValue();
                System.out.println("Hlt: " + hlt + "\n");
                slot = 0;
                for (JsonNode node2 : node.path("slot")) {
                    typical = node2.path("typ").getTextValue();
                    val = node2.path("val").getTextValue();
                    System.out.println("id:" + id + " slot" + slot + " Typ: " + typical + " Val: " + val + "\n");
                    Freedomotic.logger.severe("Souliss monitorize id: " + id + " slot: " + slot + " typ: " + typical + " val: " + val);
                    // call for notify event
                    sendChanges(board, id, slot, val, typical);
                    slot++;
                }
                id++;
            }
        }
    }

    private void sendChanges(Board board, int id, int slot, String val, String typical) { //
        //reconstruct freedomotic object address
        String address = board.getIpAddress() + ":" + board.getPort() + ":" + id + ":" + slot;
        LOG.info("Sending Souliss protocol read event for object address '" + address + "'");
        //building the event ProtocolRead
        ProtocolRead event = new ProtocolRead(this, "souliss", address);
        event.addProperty("souliss.typical", typical);
        event.addProperty("souliss.val", val);
        switch (Integer.parseInt(typical)) {
            case 11:
                if (val.equals("0")) {
                    event.addProperty("isOn", "false");
                } else {
                    event.addProperty("isOn", "true");
                }
                break;
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
        //connect to the ethernet board
        boolean connected = false;
        try {
            connected = connect(address[0], Integer.parseInt(address[1]));
        } catch (ArrayIndexOutOfBoundsException outEx) {
            LOG.severe("The object address '" + c.getProperty("address") + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            LOG.severe(address[1] + " is not a valid ethernet port to connect to");
            throw new UnableToExecuteException();
        }

        if (connected) {
            String message = createMessage(c);
            //String expectedReply = c.getProperty("expected-reply");
            try {
                String reply = sendToBoard(message);
                //if ((reply != null) && (!reply.equals(expectedReply))) {
                //TODO: implement reply check
                //}
            } catch (IOException iOException) {
                setDescription("Unable to send the message to host " + address[0] + " on port " + address[1]);
                LOG.severe("Unable to send the message to host " + address[0] + " on port " + address[1] + " Exception reported: " + iOException.toString());
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
        String id = null;
        String slot = null;
        String behavior = null;
        String url = null;
        Integer val = 0;

        id = address[2];
        slot = address[3];
        val = Integer.parseInt(c.getProperty("val"));

        //compose requested url
        url = "force?id=" + id + "&slot=" + slot + "&val=" + val;

        // http request sending to the board
        message = "GET /" + url + " HTTP 1.1\r\n\r\n";
        LOG.info("Sending 'GET /" + url + " HTTP 1.1' to Souliss board");
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
}