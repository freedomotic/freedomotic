/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.serial.SerialConnectionProvider;
import com.freedomotic.serial.SerialDataConsumer;
import java.io.IOException;
import java.util.logging.Logger;
import jssc.*;

public class MySensors extends Protocol {

    private static final Logger LOG = Logger.getLogger(MySensors.class.getName());
    //SerialConnectionProvider serial;
    static SerialPort serialPort;

    public MySensors() {
        super("MySensors", "/mysensors/mysensors-manifest.xml");
        setPollingWait(-1); //disables polling
    }

    @Override
    public void onStart() {
        getPortList();
        serialPort = new SerialPort(configuration.getStringProperty("serial.port", "/dev/usb0"));
        try {
            serialPort.openPort();
            serialPort.setParams(configuration.getIntProperty("serial.baudrate", 9600), configuration.getIntProperty("serial.databits", 8), configuration.getIntProperty("serial.stopbits", 1), configuration.getIntProperty("serial.parity", 0));
            serialPort.addEventListener(new SerialPortReader());
        } catch (SerialPortException ex) {
            LOG.severe(ex.getMessage());
        }


    }

    @Override
    protected void onRun() {
    }

    @Override
    public void onStop() {
        //called when the user stops the plugin from UI
        if (serialPort != null) {
            try {
                serialPort.closePort();
                LOG.info("Disconnected from " + serialPort.getPortName());
            } catch (SerialPortException ex) {
                LOG.severe(ex.getMessage());
            }
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void getPortList() {
        String[] portNames = SerialPortList.getPortNames();
        for (int i = 0; i < portNames.length; i++) {
            LOG.info("Found port: " + portNames[i]);
        }
    }

    public void write(String data) {
        try {
            serialPort.writeString(data);
        } catch (SerialPortException ex) {
            LOG.severe(ex.getMessage());
        }
    }

    private void sendChanges(String data) {
        String nodeID;
        String childSensorID;
        String messageType;
        String ack;
        String subType;
        String payload;
        //check if it's a correct message
        System.out.println("Read string: " + data);
        String[] message = data.split(";");
        nodeID = message[0];
        childSensorID = message[1];
        messageType = message[2];
        ack = message[3];
        subType = message[4];
        payload = message[5];

        // 
        if (messageType.equalsIgnoreCase("0") || messageType.equalsIgnoreCase("1")) {
            ProtocolRead event = new ProtocolRead(this, "mysensors", nodeID + ":" + childSensorID);
            String objectClass = configuration.getProperty(subType);
            if (objectClass != null) {
                event.addProperty("object.class", objectClass);
                event.addProperty("object.name", objectClass + " " + nodeID + ":" + childSensorID);
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

    public class SerialPortReader implements SerialPortEventListener {

        StringBuilder message = new StringBuilder();

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR()) {
                try {
                    byte buffer[] = serialPort.readBytes();
                    for (byte b : buffer) {
                        if ((b == '\r' || b == '\n') && message.length() > 0) {
                            String toProcess = message.toString();
                            // call sendChanges(String data);
                            sendChanges(toProcess);
                            message.setLength(0);
                        } else {
                            message.append((char) b);
                        }
                    }
                } catch (SerialPortException ex) {
                    LOG.severe(ex.getMessage());
                }
            }
        }
    }
}
