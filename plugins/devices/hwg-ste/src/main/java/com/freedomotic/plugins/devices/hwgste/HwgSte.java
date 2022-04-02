/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @autor Mauro Cicolella
 */
public class HwgSte extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(HwgSte.class.getName());
    private List<Board> boards;
    private Map<String, Board> devices;
    private final int BOARD_NUMBER = configuration.getTuples().size();
    private final int POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
    private final int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private final String ENABLE_AUTOCONFIGURATION = configuration.getStringProperty("enable-autoconfiguration", "true");

    /**
     * SNMP configuration
     */
    private final String SNMP_OID = configuration.getStringProperty("snmp-oid", "1.3.6.1.4.1.21796.4");
    private final int SNMP_PORT = configuration.getIntProperty("snmp-port", 161);
    private final String SNMP_COMMUNITY = configuration.getStringProperty("snmp-community", "public");
    private final String SENSOR_NAME_REQUEST = configuration.getStringProperty("sensor-name-request", "1.3.1.2");
    private final String SENSOR_STATE_REQUEST = configuration.getStringProperty("sensor-state-request", "1.3.1.3");
    private final String SENSOR_VALUE_REQUEST = configuration.getStringProperty("sensor-value-request", "1.3.1.5");
    private final String SENSOR_SN_REQUEST = configuration.getStringProperty("sensor-sn-request", "1.3.1.6");
    private final String SENSOR_UNIT_REQUEST = configuration.getStringProperty("sensor-unit-request", "1.3.1.7");
    private final String SENSOR_ID_REQUEST = configuration.getStringProperty("sensor-id-request", "1.3.1.8");

    /**
     * Initializations
     */
    public HwgSte() {
        super("HwgSte", "/hwg-ste/hwgste-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    /**
     * Loads all configurated boards from the plugin manifest file.
     *
     */
    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<>();
        }
        if (devices == null) {
            devices = new HashMap<>();
        }
        setDescription("HWG-Ste running"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            // filter the tuples with "object.class" property
            String result = configuration.getTuples().getProperty(i, "object.class");
            // if the tuple hasn't an "object.class" property it's a board configuration one 
            if (result == null) {
                String alias = configuration.getTuples().getStringProperty(i, "alias", "board1");
                String ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
                int portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
                int sensorsNumber = configuration.getTuples().getIntProperty(i, "sensors-number", 1);
                Board board = new Board(alias, ipToQuery, portToQuery, sensorsNumber);
                boards.add(board);
                // add board object and its alias as key for the hashmap
                devices.put(alias, board);
                setDescription(getDescription());
            }
        }
    }

    @Override
    public void onStart() {
        setPollingWait(POLLING_TIME);
        loadBoards();
        setDescription("HWg-STE started");
    }

    @Override
    public void onStop() {
        setPollingWait(-1); //disable polling        
        //display the default description
        setDescription("HWg-STE stopped");
    }

    @Override
    protected void onRun() {
        Set<String> keySet = devices.keySet();
        for (String key : keySet) {
            Board board = devices.get(key);
            SNMPRequest(board);
        }
        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            LOG.error(ex.getMessage(), ex);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Sends a SNMP Get request to the board.
     *
     * @param board
     */
    public void SNMPRequest(Board board) {
        MySNMP snmpRequest = new MySNMP();
        String state = null;
        String unit = null;
        String objectClass = null;

        for (int i = 1; i <= board.getSensorsNumber(); i++) {
            String sensorName = snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_NAME_REQUEST + "." + i, SNMP_COMMUNITY);
            String sensorSN = snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_SN_REQUEST + "." + i, SNMP_COMMUNITY);
            Integer sensorID = Integer.parseInt(snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_ID_REQUEST + "." + i, SNMP_COMMUNITY));
            Integer sensorState = Integer.parseInt(snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_STATE_REQUEST + "." + i, SNMP_COMMUNITY));
            Integer sensorValue = Integer.parseInt(snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_VALUE_REQUEST + "." + i, SNMP_COMMUNITY));
            Integer sensorUnit = Integer.parseInt(snmpRequest.SnmpGet(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_UNIT_REQUEST + "." + i, SNMP_COMMUNITY));

            switch (sensorState) {
                case 0:
                    state = "invalid";
                    break;
                case 1:
                    state = "normal";
                    break;
                case 2:
                    state = "outOfRangeLow";
                    break;
                case 3:
                    state = "outOfRangeHigh";
                    break;
                case 4:
                    state = "alarmLow";
                    break;
                case 5:
                    state = "alarmHigh";
                    break;
                default:
                    state = "not defined";
                    break;
            }

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
                default:
                    unit = "";
                    break;
            }

            String address = board.getAlias() + ":" + sensorID;
            LOG.info("Sending HWg-STE protocol read event for object address \"{}\"", address);
            //building the event
            ProtocolRead event = new ProtocolRead(this, "hwgste", address);
            //adding some optional information to the event for auto configuration
            if ("true".equalsIgnoreCase(ENABLE_AUTOCONFIGURATION)) {
                event.getPayload().addStatement("object.class", objectClass);
                event.getPayload().addStatement("object.name", objectClass + " " + address);
            }
            event.getPayload().addStatement("sensor.unit", unit);
            event.getPayload().addStatement("sensor.value", sensorValue.toString());
            event.getPayload().addStatement("sensor.name", sensorName);
            event.getPayload().addStatement("sensor.state", state);
            //publish the event on the messaging bus
            notifyEvent(event);
        }
    }

    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        // no commands available for this plugin    
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
