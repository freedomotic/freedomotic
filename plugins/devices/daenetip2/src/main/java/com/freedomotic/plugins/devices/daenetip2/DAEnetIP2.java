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
package com.freedomotic.plugins.devices.daenetip2;

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
import java.math.BigInteger;
import java.net.Socket;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class DAEnetIP2 extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(DAEnetIP2.class.getName());
    private static int BOARD_NUMBER = 1;
    Map<String, Board> devices = new HashMap<String, Board>();
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 2000);
    private String ADDRESS_DELIMITER = configuration.getStringProperty("address-delimiter", ":");
    private String PROTOCOL_NAME = configuration.getStringProperty("protocol.name", "daenetip2");
    private String SNMP_OID = configuration.getStringProperty("snmp-oid", ".1.3.6.1.4.1.19865");
    private String P3_OID = configuration.getStringProperty("p3-oid", "1.2.1");
    private String P5_OID = configuration.getStringProperty("p5-oid", "1.2.2");
    private String P6_OID = configuration.getStringProperty("p6-oid", "1.2.3");
    private String P3_STATUS_OID = configuration.getStringProperty("p3-status-oid", "1.2.1.33.0");
    private String P5_STATUS_OID = configuration.getStringProperty("p5-status-oid", "1.2.2.33.0");

    /**
     * Initializations
     */
    public DAEnetIP2() {
        super("DAEnetIP2", "/daenetip2/daenetip2-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (devices == null) {
            devices = new HashMap<String, Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipAddress;
            String alias;
            String readOnlyCommunity;
            String readWriteCommunity;
            int snmpPort;
            alias = configuration.getTuples().getStringProperty(i, "alias", "default");
            readOnlyCommunity = configuration.getTuples().getStringProperty(i, "read-only-community", "000000000000");
            readWriteCommunity = configuration.getTuples().getStringProperty(i, "read-write-community", "private");
            ipAddress = configuration.getTuples().getStringProperty(i, "ip-address", "172.16.100.2");
            snmpPort = configuration.getTuples().getIntProperty(i, "snmp-port", 161);
            Board board = new Board(alias, ipAddress, snmpPort, readOnlyCommunity, readWriteCommunity);
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipAddress + ":" + snmpPort + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to a DAEnetIP2 device on address " + address + ':' + port);
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
        loadBoards();
    }

    @Override
    public void onStop() {
        //release resources
        devices.clear();
        devices = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "DAEnetIP2"));
    }

    @Override
    protected void onRun() {
        //for (Board board : boards) {
        Set keys = devices.keySet();
        Iterator keyIter = keys.iterator();
        while (keyIter.hasNext()) {
            String alias = (String) keyIter.next();
            Board board = (Board) devices.get(alias);
            SNMPRequest(board);
            readP6Port(board);
        }

        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
    }

    /**
     *
     * @param board
     */
    public void SNMPRequest(Board board) {
        final MYSNMP snmpRequest = new MYSNMP();
        String relayStatus = null;
        String objectAddress = null;
        String P3Status = null;
        String P5Status = null;
        Integer oldP5Status = 0;

        P3Status = snmpRequest.SNMP_GET(board.getIpAddress(), board.getSnmpPort(), SNMP_OID + "." + P3_STATUS_OID, board.getReadWriteCommunity());
        if (!(Integer.valueOf(P3Status) == board.getP3Status())) {
            System.out.println("P3 status changed");
            BigInteger P3StatusBi = new BigInteger(P3Status);
            String behavior = null;
            for (int i = 0; i < 8; i++) {
                Boolean newStatusBit = P3StatusBi.testBit(i);
                int j = i + 1;
                if (newStatusBit == true) {
                    relayStatus = "1";
                } else {
                    relayStatus = "0";
                }
                objectAddress = board.getAlias() + ADDRESS_DELIMITER + "P3." + j;
                System.out.println(objectAddress + " has changed Status " + relayStatus + " Address " + objectAddress);
                sendEvent(objectAddress, "relay.state", relayStatus);
            }
            board.setP3Status(Integer.valueOf(P3Status));
        }

        P5Status = snmpRequest.SNMP_GET(board.getIpAddress(), board.getSnmpPort(), SNMP_OID + "." + P5_STATUS_OID, board.getReadWriteCommunity());
        if (!(Integer.valueOf(P5Status) == board.getP5Status())) {
            System.out.println("P5 status changed");
            BigInteger P5StatusBi = new BigInteger(P5Status);
            String behavior = null;
            for (int i = 0; i < 8; i++) {
                Boolean newStatusBit = P5StatusBi.testBit(i);
                int j = i + 1;
                if (newStatusBit == true) {
                    relayStatus = "1";
                } else {
                    relayStatus = "0";
                }
                objectAddress = board.getAlias() + ADDRESS_DELIMITER + "P5." + j;
                System.out.println(objectAddress + " has changed Status " + relayStatus + " Address " + objectAddress);
                sendEvent(objectAddress, "relay.state", relayStatus);
            }
            board.setP5Status(Integer.valueOf(P5Status));

        }
        //readP6Port(board);
        System.out.println("P3 =" + P3Status);
        System.out.println("P5 =" + P5Status);

    }

    private void readP6Port(Board board) {
        final MYSNMP snmpRequest = new MYSNMP();
        String P6PinStatus = null;
        String objectAddress = null;
        for (int i = 1; i <= 8; i++) {
            P6PinStatus = snmpRequest.SNMP_GET(board.getIpAddress(), board.getSnmpPort(), SNMP_OID + "." + P6_OID + "." + i + ".0", board.getReadWriteCommunity());
            if (!(Integer.valueOf(P6PinStatus) == board.getP6Status(i - 1))) {
                objectAddress = board.getAlias() + ADDRESS_DELIMITER + "P6." + i;
                //System.out.println(objectAddress + " has changed Status " + relayStatus + " Address " + objectAddress);
                sendEvent(objectAddress, "input.value", P6PinStatus);
                System.out.println(objectAddress + " " + P6PinStatus);
                board.setP6Status(i - 1, Integer.valueOf(P6PinStatus));
            }
        }
    }

    private void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, PROTOCOL_NAME, objectAddress);
        event.addProperty(eventProperty, eventValue);
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    /**
     * Actuator side
     * @throws com.freedomotic.exceptions.UnableToExecuteException
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        final MYSNMP snmpRequest = new MYSNMP();
        String status = null;
        String OID_REQUEST = null;

        address = c.getProperty("address").split(ADDRESS_DELIMITER);
        Board board = (Board) devices.get(address[0]);
        if (address[1].substring(0, 1).equalsIgnoreCase("P3")) {
            OID_REQUEST = P3_OID + "." + address[1].charAt(address[1].length() - 1) + ".0";
        } else {
            OID_REQUEST = P5_OID + "." + address[1].charAt(address[1].length() - 1) + ".0";
        }
        if (c.getProperty("control").equalsIgnoreCase("ON")) {
            status = "1";
        } else {
            status = "0";
        }
        LOG.info("IP " + board.getIpAddress() + " OID " + SNMP_OID + "." + OID_REQUEST + " Status " + status + " pass " + board.getReadWriteCommunity());
        snmpRequest.SNMP_SET(board.getIpAddress(), board.getSnmpPort(), SNMP_OID + "." + OID_REQUEST, status, board.getReadWriteCommunity());
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
