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
package com.freedomotic.plugins.devices.zway;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.*;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.net.www.http.HttpClient;

/**
 *
 *
 * @author Mauro Cicolella
 */
public class ZWay extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(ZWay.class.getName());
    Map<String, Board> devices = new HashMap<String, Board>();
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private String SEND_COMMAND_URL = configuration.getStringProperty("send-command-url", "ZWaveAPI/Run");

    /**
     * Initializations
     */
    public ZWay() {
        super("ZWay", "/zway/zway-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (devices == null) {
            devices = new HashMap<String, Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            String autoConfiguration;
            String objectClass;
            String alias;
            int portToQuery;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            alias = configuration.getTuples().getStringProperty(i, "alias", "default");
            autoConfiguration = configuration.getTuples().getStringProperty(i, "auto-configuration", "false");
            objectClass = configuration.getTuples().getStringProperty(i, "object.class", "Light");
            Board board = new Board(ipToQuery, portToQuery, alias, autoConfiguration, objectClass);
            // add board object and its alias as key for the hashmap
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to ZWay board on address {}:{}", address, port);
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
        setDescription(configuration.getStringProperty("description", "ZWay"));
    }

    @Override
    protected void onRun() {
        Set<String> keySet = devices.keySet();
        String objectAddress;
        if (keySet.size() > 0) {
            for (String key : keySet) {
                try {
                    Board board = devices.get(key);
                    try {
                        // retrieve a list of configured objects with "zway" protocol
                        List<EnvObjectLogic> objectsList = getApi().things().findByProtocol("zway");
                        if (objectsList.size() > 0) {
                            for (Iterator it = objectsList.iterator(); it.hasNext();) {
                                EnvObjectLogic object = (EnvObjectLogic) it.next();
                                // read status for each object 
                                readStatus(board, object);
                                LOG.info("Object address:" + object.getPojo().getPhisicalAddress() + " type:" + object.getPojo().getType());
                            }
                        }
                    } catch (MalformedURLException ex) {
                        LOG.error(ex.getLocalizedMessage());
                    } catch (IOException ex) {
                        LOG.error(ex.getLocalizedMessage());
                    }
                } catch (UnableToExecuteException ex) {
                    LOG.error(ex.getLocalizedMessage());
                }
            }
        }

        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    /**
     * @param c
     * @throws UnableToExecuteException
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        //get connection paramentes address:port from received freedomotic command
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter); // in the format IP:PORT:ID:INSTANCE
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
            String message = createCommandMessage(c);
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

    public void readStatus(Board board, EnvObjectLogic object) throws UnableToExecuteException, MalformedURLException, IOException {

        String delimiter = configuration.getProperty("address-delimiter");
        String addressComponents[] = object.getPojo().getPhisicalAddress().split(delimiter);
        String deviceAddress = addressComponents[1];
        String deviceInstance = addressComponents[2];
        String objectType = addressComponents[3];
        String type[] = objectType.split(".");

        // sends an update status request .Get() method
        String path = "http://" + board.getIpAddress() + ":" + board.getPort() + "/" + SEND_COMMAND_URL + "/devices[" + deviceAddress + "].instances[" + deviceInstance + "]."
                + configuration.getProperty(objectType) + ".Get()";
        URL url = new URL(path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.flush();
        wr.close();

        // reads a sensor value
        path = "http://" + board.getIpAddress() + ":" + board.getPort() + "/" + SEND_COMMAND_URL + "/devices[" + deviceAddress + "].instances[" + deviceInstance + "]."
                + configuration.getProperty(objectType);
        url = new URL(path);
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setUseCaches(false);
        wr = new DataOutputStream(connection.getOutputStream());
        wr.flush();
        wr.close();
        BufferedReader br = new BufferedReader(new InputStreamReader((connection.getInputStream())));
        String output = null;
        String readValue = "";
        while ((output = br.readLine()) != null) {
            readValue += output;
        }
        System.out.println(Float.parseFloat(readValue));
        //building the event
        ProtocolRead event = new ProtocolRead(this, "zway", board.getAlias() + delimiter + deviceAddress + delimiter + deviceInstance + delimiter + objectType);
        //adding some optional information to the event
        event.addProperty("read.value", String.valueOf(Float.parseFloat(readValue)));
        event.addProperty("object.type", objectType);
        //publishes the event on the messaging bus
        this.notifyEvent(event);

        connection.disconnect();

    }

    private String sendToBoard(String message) throws IOException {
        String receivedReply = null;
        String line = null;
        if (outputStream != null) {
            outputStream.writeBytes(message);
            outputStream.flush();
            inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            try {
                while ((line = inputStream.readLine()) != null) {
                    receivedReply = receivedReply + line; // read device reply
                }
            } catch (IOException iOException) {
                throw new IOException();
            }
        }
        return receivedReply;
    }

    // create command message to send to the board
    // this part must be changed to reflect the board protocol
    /**
     *
     * @param c
     * @return
     */
    public String createCommandMessage(Command c) {
        String message = null;
        String page = null;
        String command = null;
        Integer objectID = 0;
        Integer instanceID = 0;

        objectID = Integer.parseInt(address[1]);
        instanceID = Integer.parseInt(address[2]);
        command = c.getProperty("command");
        page = "/" + SEND_COMMAND_URL + "/" + "devices[" + objectID + "].instances[" + instanceID + "]." + command;

        // http request sending to the board
        message = "POST " + page + " HTTP/1.1\r\n\r\n";
        LOG.info("Sending " + message);
        //LOG.info("Unix timestamp " + getUnixTimeStamp() + "\n");
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

    /**
     * Retrieves a key from value in the hashmap
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

    private int getUnixTimeStamp() {
        Date date = new Date();
        int iTimeStamp = (int) (date.getTime() * .001);
        return iTimeStamp;
    }
}
