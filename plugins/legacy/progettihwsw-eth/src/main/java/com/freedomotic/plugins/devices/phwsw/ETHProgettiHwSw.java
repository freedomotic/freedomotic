/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.plugins.devices.phwsw;

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

public class ETHProgettiHwSw extends Protocol {

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
    public ETHProgettiHwSw() {
        super("ETH Progetti HwSw", "/phwsw/progettihwsw-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            String lineToMonitorize;
            int portToQuery;
            int digitalInputNumber;
            int analogInputNumber;
            int relayNumber;
            int startingRelay;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            lineToMonitorize = configuration.getTuples().getStringProperty(i, "line-to-monitorize", "led");
            relayNumber = configuration.getTuples().getIntProperty(i, "relay-number", 8);
            analogInputNumber = configuration.getTuples().getIntProperty(i, "analog-input-number", 4);
            digitalInputNumber = configuration.getTuples().getIntProperty(i, "digital-input-number", 4);
            startingRelay = configuration.getTuples().getIntProperty(i, "starting-relay", 0);
            Board board = new Board(ipToQuery, portToQuery, relayNumber, analogInputNumber,
                    digitalInputNumber, lineToMonitorize, startingRelay);
            boards.add(board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ":" + lineToMonitorize + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        Freedomotic.logger.info("Trying to connect to ethernet relay board on address " + address + ':' + port);
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
        setDescription(configuration.getStringProperty("description", "ETH Progetti HWSW"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            evaluateDiffs(getXMLStatusFile(board), board); //parses the xml and crosscheck the data with the previous read
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(ETHProgettiHwSw.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(ETHProgettiHwSw.class.getName()).log(Level.SEVERE, null, ex);
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://" + board.getIpAddress() + ":"
                    + Integer.toString(board.getPort()) + "/status.xml";
            Freedomotic.logger.info("Progetti HWSW Eth Sensor gets relay status from file " + statusFileURL);
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
        //parses xml
        if (doc != null && board != null) {
            Node n = doc.getFirstChild();
            NodeList nl = n.getChildNodes();
            int startingRelay = board.getStartingRelay();
            for (int i = startingRelay; i < nl.getLength(); i++) {
                try {
                    // converts i into hexadecimal value (string) and sends the parameters
                    String tagName = board.getLineToMonitorize() + HexIntConverter.convert(i);
                    Freedomotic.logger.severe("Progetti HWSW Sensor monitorize tags " + tagName);
                    sendChanges(i, board, doc.getElementsByTagName(tagName).item(0).getTextContent());
                } catch (DOMException dOMException) {
                    //do nothing
                } catch (NumberFormatException numberFormatException) {
                    //do nothing
                } catch (NullPointerException ex) {
                    //do nothing
                }
            }
        }
    }

    private void sendChanges(int relayLine, Board board, String status) {
        // if starting-relay = 0 then increments relayLine to start from 1 not from zero
        if (board.getStartingRelay() == 0) {
            relayLine++;
        }
        //reconstruct freedom object address
        String address = board.getIpAddress() + ":" + board.getPort() + ":" + relayLine;
        Freedomotic.logger.info("Sending Progetti HWSW protocol read event for object address '" + address + "'. It's readed status is " + status);
        //building the event
        ProtocolRead event = new ProtocolRead(this, "SNT084Eth8R8I", address); //IP:PORT:RELAYLINE
        // relay lines - status=0 -> off; status=1 -> on
        if (board.getLineToMonitorize().equalsIgnoreCase("led")) {
            if (status.equals("0")) {
                event.addProperty("isOn", "false");
            } else {
                event.addProperty("isOn", "true");
            }
        } else // digital inputs - status=up -> off; status=dn -> on
        if (board.getLineToMonitorize().equalsIgnoreCase("btn")) {
            if (status.equalsIgnoreCase("up")) {
                event.addProperty("isOn", "false");
            } else {
                event.addProperty("isOn", "true");
            }
        } else {
            // analog inputs - status=0 -> off; status>0 -> on
            if (board.getLineToMonitorize().equalsIgnoreCase("ptn")) {
                if (status.equalsIgnoreCase("0")) {
                    event.addProperty("isOn", "false");
                } else {
                    event.addProperty("isOn", "true");
                }
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
        //get connection paramentes address:port from received freedom command
        String delimiter = configuration.getProperty("address-delimiter");
        address = c.getProperty("address").split(delimiter);
        //connect to the ethernet board
        boolean connected = false;
        try {
            connected = connect(address[0], Integer.parseInt(address[1]));
        } catch (ArrayIndexOutOfBoundsException outEx) {
            Freedomotic.logger.severe("The object address '" + c.getProperty("address") + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            Freedomotic.logger.severe(address[1] + " is not a valid ethernet port to connect to");
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
        String behavior = null;
        String relay = null;

        if (c.getProperty("command").equals("RELAY")) {
            // convert relay number (integer) into hexadecimal value (string) for board compatibility
            relay = HexIntConverter.convert(Integer.parseInt(address[2]) - 1);
            // convert freedom behavior(on/off) to board behavior (1/0)
            if (c.getProperty("behavior").equals("on")) {
                behavior = "1";
            }
            if (c.getProperty("behavior").equals("off")) {
                behavior = "0";
            }
            page = "forms.htm?led" + relay + "=" + behavior;
        }

        if (c.getProperty("command").equals("TOGGLE")) {
            // mapping relay line -> protocol
            relay = address[2]; // toggle range from 1
            int time = Integer.parseInt(c.getProperty("time-in-ms"));
            int seconds = time / 1000;
            String relayLine = configuration.getProperty("TOGGLE" + seconds + "S" + relay);

            //compose requested link
            page = "toggle.cgi?toggle=" + relayLine;
        }


        // http request sending to the board
        message = "GET /" + page + " HTTP 1.1\r\n\r\n";
        Freedomotic.logger.info("Sending 'GET /" + page + " HTTP 1.1' to relay board");
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
