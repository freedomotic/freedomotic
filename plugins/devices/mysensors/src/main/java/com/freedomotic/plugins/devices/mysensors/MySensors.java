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
/**
 * @author Mauro Cicolella
 */
package com.freedomotic.plugins.devices.mysensors;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jssc.SerialPortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MySensors extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(MySensors.class.getName());

    // Types of message
    private final String PRESENTATION = "0";
    private final String SET = "1";
    private final String REQ = "2";
    private final String INTERNAL = "3";
    private final String STREAM = "4";

    private final String PORTNAME = configuration.getStringProperty("serial.port", "/dev/ttyACM0");
    private final Integer BAUDRATE;
    private final Integer DATABITS = configuration.getIntProperty("serial.databits", 8);
    private final Integer PARITY = configuration.getIntProperty("serial.parity", 0);
    private final Integer STOPBITS = configuration.getIntProperty("serial.stopbits", 1);
    private SerialHelper serial;
    private final Map<String, String> deviceNodeIDTable = new HashMap<>();

    public MySensors() {
        super("MySensors", "/mysensors/mysensors-manifest.xml");
        this.BAUDRATE = configuration.getIntProperty("serial.baudrate", 9600);
        setPollingWait(-1); //disables polling
    }

    @Override
    public void onStart() throws PluginStartupException {
        loadConfiguredObjects();
        try {
            serial = new SerialHelper(PORTNAME, BAUDRATE, DATABITS, STOPBITS, PARITY, (String data) -> {
                LOG.info("MySensors received message \"{}\"", data);
                manageMessage(data);
            });
            serial.setChunkTerminator("\n");
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error: " + ex.getMessage(), ex);
        }
    }

    @Override
    protected void onRun() {
    }

    @Override
    public void onStop() {
        if (serial != null) {
            if (serial.disconnect()) {
                serial = null;
            } else {
                LOG.warn("Impossible to disconnect from \"{}\"", serial.getPortName());
            }
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String address = c.getProperty("address");
        String IDMessageType = c.getProperty("id-message-type");
        String ack = c.getProperty("ack");
        String subType = c.getProperty("sub-type");
        String payload = c.getProperty("payload");
        String message = address + ";" + IDMessageType + ";" + ack + ";" + subType + ";" + payload + "\n";
        writeSerialData(message);
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
     * Manages messages received from the serial connection.
     *
     * @param data
     */
    private void manageMessage(String data) {
        String nodeID;
        String childSensorID;
        String command;
        String ack;
        String type;
        String payload;
        String[] message = data.split(";");

        if (message.length >= 6) {
            nodeID = message[0];
            childSensorID = message[1];
            command = message[2];
            ack = message[3];
            type = message[4];
            payload = message[5];

            switch (command) {

                case INTERNAL:
                    manageInternalMessage(nodeID, childSensorID, ack, type, payload);
                    break;

                case SET:
                    manageSetMessage(nodeID, childSensorID, ack, type, payload);
                    break;

            }
        } else {
            LOG.warn("Data format incorrect");
        }
    }

    /**
     * Manages 'SET' messages.
     * 
     * 
     * @param nodeID
     * @param childSensorID
     * @param ack
     * @param subType
     * @param payload
     */
    private void manageSetMessage(String nodeID, String childSensorID, String ack, String subType, String payload) {

        ProtocolRead event = new ProtocolRead(this, "mysensors", nodeID + ":" + childSensorID);
        String subTypeName = SetReqSubType.fromInt(Integer.valueOf(subType)).toString();
        String objectClass = configuration.getProperty(subTypeName);
        if (objectClass != null) {
            event.addProperty("object.class", objectClass);
            event.addProperty("object.name", objectClass + " " + nodeID + ":" + childSensorID);
            LOG.info("Created thing \"{}\" with address {}:{}", objectClass, nodeID, childSensorID);
        }
        // adds isOn property only for lights
        if (subType.equalsIgnoreCase("V_LIGHT")) {
            if (payload.equalsIgnoreCase("1")) {
                event.addProperty("sensor.isOn", "true");
            } else {
                event.addProperty("sensor.isOn", "false");
            }
        }
        event.addProperty("sensor.value", payload);
        this.notifyEvent(event);

    }

    /**
     *
     * @param nodeID
     * @param childSensorID
     * @param ack
     * @param subType
     * @param payload
     */
    private void manageInternalMessage(String nodeID, String childSensorID, String ack, String subType, String payload) {
        InternalSubType internalSubType = InternalSubType.I_ID_REQUEST;
        switch (internalSubType) {
            case I_ID_REQUEST:
                if (nodeID.equalsIgnoreCase("255") && childSensorID.equalsIgnoreCase("255")) {
                    // get a new available nodeID
                    String newNodeID = getAvailableNodeID();
                    if (!newNodeID.equalsIgnoreCase("-1")) {
                        LOG.info("Node-ID assigned: {}", newNodeID);
                        sendMessage(nodeID, childSensorID, INTERNAL, ack, subType, newNodeID);
                    } else {
                        LOG.warn("No more Node-ID available");
                    }
                }
                break;
        }
    }

    /**
     * Sends a message to MySensors gateway using the serial connection.
     *
     * @param nodeID
     * @param childSensorID
     * @param messageType
     * @param ack
     * @param subType
     * @param payload
     */
    private void sendMessage(String nodeID, String childSensorID, String messageType, String ack, String subType, String payload) {
        InternalSubType internalSubType = InternalSubType.I_ID_REQUEST;
        StringBuilder messageToSend = new StringBuilder();
        messageToSend.append(nodeID)
                .append(";")
                .append(childSensorID).append(";")
                .append(messageType).append(";")
                .append(ack).append(";")
                .append(InternalSubType.I_ID_RESPONSE)
                .append(";").append(payload)
                .append("\n");

        writeSerialData(messageToSend.toString());
    }

    /**
     * Returns an available NodeID for AUTO-id feature.
     *
     * @return an integer between 1 and 253, -1 if no NodeID is available
     */
    private String getAvailableNodeID() {
        boolean addressAvailable = false;
        int i = 0;
        for (i = 1; i < 254; i++) {
            if (!deviceNodeIDTable.containsKey(String.valueOf(i))) {
                addressAvailable = true;
                break;
            }
        }
        if (addressAvailable) {
            deviceNodeIDTable.put(String.valueOf(i), null);
            return String.valueOf(i);
        }
        return ("-1");
    }

    /* 
     * Creates a list of configured objects in Freedomotic to determine the next
     * available nodeID for 'AUTO-ID' feature.
     */
    private void loadConfiguredObjects() {
        List<EnvObjectLogic> configuredObjects = (List<EnvObjectLogic>) getApi().things().findByProtocol("mysensors");
        for (EnvObjectLogic obj : configuredObjects) {
            String phisicalAddress = obj.getPojo().getPhisicalAddress();
            String[] addrComponents = phisicalAddress.split("\n");
            deviceNodeIDTable.put(addrComponents[0], obj.getPojo().getName());
        }

    }

    /**
     * Writes data on the serial connection.
     *
     * @param data data to write
     */
    private void writeSerialData(String data) {
        try {
            LOG.info("Sending \"{}\" to MySensors gateway", data);
            serial.write(data);
        } catch (SerialPortException ex) {
            LOG.error("Impossible to write on serial for {}" + ex.getLocalizedMessage());
        }
    }
}
