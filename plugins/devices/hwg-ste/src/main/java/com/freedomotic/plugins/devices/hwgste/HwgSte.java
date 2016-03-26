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
package com.freedomotic.plugins.devices.hwgste;

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
import java.net.Socket;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class HwgSte extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(HwgSte.class.getName());
    private static ArrayList<Board> boards = null;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private String SNMP_OID = configuration.getStringProperty("snmp-oid", "1.3.6.1.4.1.21796.4");
    private int SNMP_PORT = configuration.getIntProperty("snmp-port", 161);
    private String SNMP_COMMUNITY = configuration.getStringProperty("snmp-community", "public");
    private String SENSOR_NAME_REQUEST = configuration.getStringProperty("sensor-name-request", "1.3.1.2");
    private String SENSOR_STATE_REQUEST = configuration.getStringProperty("sensor-state-request", "1.3.1.3");
    private String SENSOR_VALUE_REQUEST = configuration.getStringProperty("sensor-value-request", "1.3.1.5");
    private String SENSOR_SN_REQUEST = configuration.getStringProperty("sensor-sn-request", "1.3.1.6");
    private String SENSOR_UNIT_REQUEST = configuration.getStringProperty("sensor-unit-request", "1.3.1.7");
    private String SENSOR_ID_REQUEST = configuration.getStringProperty("sensor-id-request", "1.3.1.8");

    /**
     * Initializations
     */
    public HwgSte() {
        super("HwgSte", "/hwg-ste/hwgste-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("HWG-Ste running"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            // filter the tuples with "object.class" property
            String result = configuration.getTuples().getProperty(i, "object.class");
            // if the tuple hasn't an "object.class" property it's a board configuration one 
            if (result == null) {
                String ipToQuery;
                int portToQuery;
                int sensorsNumber;
                ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
                portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
                sensorsNumber = configuration.getTuples().getIntProperty(i, "sensors-number", 1);
                Board board = new Board(ipToQuery, portToQuery, sensorsNumber);
                boards.add(board);
                setDescription(getDescription());
            }
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        LOG.info("Trying to connect to HWg-STE device on address " + address + ':' + port);
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
        boards.clear();
        boards = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "HWg-STE stopped"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            SNMPRequest(board);
            //evaluateDiffs(getXMLStatusFile(board), board); //parses the xml and crosscheck the data with the previous read
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage());
            }
        }
    }

    public void SNMPRequest(Board board) {
        final MYSNMP snmpRequest = new MYSNMP();
        for (int i = 1; i <= board.getSensorsNumber(); i++) {
            String sensorName = snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_NAME_REQUEST + "." + i, SNMP_COMMUNITY);
            System.out.println("Name =" + sensorName);
            String sensorSN = snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_SN_REQUEST + "." + i, SNMP_COMMUNITY);
            System.out.println("SN =" + sensorSN);
            Integer sensorID = Integer.parseInt(snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_ID_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("ID =" + sensorID);
            Integer sensorState = Integer.parseInt(snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_STATE_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("State =" + sensorState);
            Integer sensorValue = Integer.parseInt(snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_VALUE_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("Value =" + sensorValue);
            Integer sensorUnit = Integer.parseInt(snmpRequest.SNMP_GET(this, board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_UNIT_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("Unit =" + sensorUnit);
            String state = null;
            switch (sensorState) {
                case 0:
                    state = "invalid";
                case 1:
                    state = "normal";
                case 2:
                    state = "outOfRangeLow";
                case 3:
                    state = "outOfRangeHigh";
                case 4:
                    state = "alarmLow";
                case 5:
                    state = "alarmHigh";
            }
            String unit = null;
            String objectClass = null;
            switch (sensorUnit) {
                case 0:
                    unit = "unknown";
                    break;
                case 1:
                    unit = "C";
                    objectClass = "Thermometer";
                    break;
                case 2:
                    unit = "F";
                    objectClass = "Thermometer";
                    break;
                case 3:
                    unit = "K";
                    objectClass = "Thermometer";
                    break;
                case 4:
                    unit = "%";
                    objectClass = "Hygrometer";
                    break;
            }

            String address = board.getIpAddress() + ":" + sensorID;
            LOG.info("Sending HWg-STE protocol read event for object address '" + address + "'");
            //building the event
            ProtocolRead event = new ProtocolRead(this, "hwgste", address);
            //adding some optional information to the event
            event.addProperty("object.class", objectClass);
            event.addProperty("object.name", address);
            event.addProperty("sensor.unit", unit);
            event.addProperty("sensor.value", sensorValue.toString());
            event.addProperty("sensor.name", sensorName);
            event.addProperty("sensor.state", state);
            //publish the event on the messaging bus
            this.notifyEvent(event);
        }
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        // do nothing    
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    public Logger getLogger() {
        return LOG;
    }
}
