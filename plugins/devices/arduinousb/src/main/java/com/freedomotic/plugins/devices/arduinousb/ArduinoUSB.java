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
package com.freedomotic.plugins.devices.arduinousb;

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
import java.util.logging.Logger;
import jssc.SerialPortException;

public class ArduinoUSB extends Protocol {

    private static final Logger LOG = Logger.getLogger(ArduinoUSB.class.getName());
    private String PORTNAME = configuration.getStringProperty("serial.port", "/dev/usb0");
    private Integer BAUDRATE = configuration.getIntProperty("serial.baudrate", 9600);
    private Integer DATABITS = configuration.getIntProperty("serial.databits", 8);
    private Integer PARITY = configuration.getIntProperty("serial.parity", 0);
    private Integer STOPBITS = configuration.getIntProperty("serial.stopbits", 1);
    private String CHUNK_TERMINATOR = configuration.getStringProperty("chunk.terminator", "\n");
    private Integer CHUNK_SIZE = configuration.getIntProperty("chunk.size", 5);
    private SerialHelper serial;

    public ArduinoUSB() {
        super("Arduino USB", "/arduinousb/arduinousb-manifest.xml");
        setPollingWait(-1); // onRun() executes once
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            serial = new SerialHelper(PORTNAME, BAUDRATE, DATABITS, STOPBITS, PARITY, new SerialPortListener() {

                @Override
                public void onDataAvailable(String data) {
                    LOG.info("Arduino USB received: " + data);
                    sendChanges(data);
                }
            });
            // in this example it reads until terminator
            serial.setChunkTerminator(CHUNK_TERMINATOR);
            //serial.setChunkSize(CHUNK_SIZE);
        } catch (SerialPortException ex) {
            throw new PluginStartupException("Error while creating Arduino serial connection. " + ex.getMessage(), ex);
        }
    }

    @Override
    public void onStop() {
        if (serial != null) {
            serial.disconnect();
        }
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //this method receives freedomotic commands send on channel app.actuators.protocol.arduinousb.in
        String message = c.getProperty("arduinousb.message");
        String reply = null;
        try {
            serial.write(message);
        } catch (SerialPortException ex) {
            LOG.severe("Error sending command " + c.getName() + " for " + ex.getMessage());
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

    private void sendChanges(String data) {
        String[] message = null;
        String address = null;
        String status = null;

        // remove '\n' and split data read
        message = data.substring(0, data.length() - 1).split(";");
        address = message[0];
        status = message[1];

        ProtocolRead event = new ProtocolRead(this, "arduinousb", address);
        if (status.equalsIgnoreCase("on")) {
            event.addProperty("isOn", "true");
        } else {
            event.addProperty("isOn", "false");
        }
        this.notifyEvent(event);
    }
}
