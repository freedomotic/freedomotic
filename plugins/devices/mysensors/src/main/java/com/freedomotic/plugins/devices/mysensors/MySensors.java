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
package com.freedomotic.plugins.devices.mysensors;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.helpers.SerialPortListener;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.SerialPortException;

public class MySensors extends Protocol {

    private static final Logger LOG = Logger.getLogger(MySensors.class.getName());
    private String PORTNAME = configuration.getStringProperty("serial.port", "/dev/ttyACM0");
    private Integer BAUDRATE = configuration.getIntProperty("serial.baudrate", 9600);
    private Integer DATABITS = configuration.getIntProperty("serial.databits", 8);
    private Integer PARITY = configuration.getIntProperty("serial.parity", 0);
    private Integer STOPBITS = configuration.getIntProperty("serial.stopbits", 1);
    private SerialHelper serial;

    public MySensors() {
        super("MySensors", "/mysensors/mysensors-manifest.xml");
        setPollingWait(-1); //disables polling
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            serial = new SerialHelper(PORTNAME, BAUDRATE, DATABITS, STOPBITS, PARITY, new SerialPortListener() {

                @Override
                public void onDataAvailable(String data) {
                    LOG.info("MySensors received: " + data);
                    sendChanges(data);
                }
            });

            serial.setChunkTerminator("\n");
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating serial connection. " + ex.getMessage(), ex);
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
                LOG.info("Impossible to disconnect from ");
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
        write(message);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void write(String data) {
        LOG.info("MySensors writes '" + data + "' to serial connection");
        try {
            serial.write(data);
        } catch (SerialPortException ex) {
            Logger.getLogger(MySensors.class.getName()).log(Level.WARNING, null, ex);
        }
    }

    private void sendChanges(String data) {
        String nodeID;
        String childSensorID;
        String messageType;
        String ack;
        String subType;
        String payload;
        String[] message = data.split(";");
        nodeID = message[0];
        childSensorID = message[1];
        messageType = message[2];
        ack = message[3];
        subType = message[4];
        payload = message[5];

        // 
        if (messageType.equalsIgnoreCase("0") || messageType.equalsIgnoreCase("1")) {
            ProtocolRead event = new ProtocolRead(this, "mysensors", nodeID + ";" + childSensorID);
            String objectClass = configuration.getProperty(subType);
            if (objectClass != null) {
                event.addProperty("object.class", objectClass);
                event.addProperty("object.name", objectClass + " " + nodeID + ":" + childSensorID);
                LOG.info("Created object " + objectClass + " with address " + nodeID + ":" + childSensorID);
            }
            // adds isOn property only for lights
            if ((messageType.equalsIgnoreCase("0") && subType.equalsIgnoreCase("3")) || (messageType.equalsIgnoreCase("1") && subType.equalsIgnoreCase("2"))) {
                if (payload.equalsIgnoreCase("1")) {
                    event.addProperty("sensor.isOn", "true");
                } else {
                    event.addProperty("sensor.isOn", "false");
                }
            }
            event.addProperty("sensor.value", payload);
            this.notifyEvent(event);

        }

    }
}
