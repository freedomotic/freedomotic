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
package com.freedomotic.plugins.devices.arduinousb;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.SerialHelper;
import com.freedomotic.helpers.SerialPortListener;
import com.freedomotic.reactions.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jssc.SerialPortException;

/**
 *
 * @author Mauro Cicolella
 */
public class ArduinoUSB extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(ArduinoUSB.class.getName());
    private String portName = configuration.getStringProperty("serial.port", "/dev/usb0");
    private Integer baudRate = configuration.getIntProperty("serial.baudrate", 9600);
    private Integer dataBits = configuration.getIntProperty("serial.databits", 8);
    private Integer parity = configuration.getIntProperty("serial.parity", 0);
    private Integer stopBits = configuration.getIntProperty("serial.stopbits", 1);
    private String chunkTerminator = configuration.getStringProperty("chunk.terminator", "\n");
    //ALTERNITIVE TO CHUNK TERMINATOR: 
    //private Integer chunkSize = configuration.getIntProperty("chunk.size", 5);
    private String delimiter = configuration.getStringProperty("delimiter", ";");
    private SerialHelper serial;

    /**
     *
     */
    public ArduinoUSB() {
        super("Arduino USB", "/arduinousb/arduinousb-manifest.xml");
        //This disables loop execution od onRun() method
        setPollingWait(-1); // onRun() executes once.
    }

    @Override
    public void onStart() throws PluginStartupException {
        try {
            serial = new SerialHelper(portName, baudRate, dataBits, stopBits, parity, new SerialPortListener() {

                @Override
                public void onDataAvailable(String data) {
                    LOG.info("Arduino USB received: {}", data);
                    sendChanges(data);
                }
            });
            // in this example it reads until a string terminator (default: new line char)
            serial.setChunkTerminator(chunkTerminator);
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
        //nothing to do, Arduino messages are read by SerialHelper
    }

    @Override
    protected void onCommand(Command c) throws UnableToExecuteException {
        //this method receives freedomotic commands sent on channel app.actuators.protocol.arduinousb.in
        String message = c.getProperty("arduinousb.message");
        try {
            serial.write(message);
        } catch (SerialPortException ex) {
            throw new UnableToExecuteException("Error writing message '" + message + "' to arduino serial board: " + ex.getMessage(), ex);
        }
    }

    private void sendChanges(String data) {
        // in this example we are using Arduino Serial.println() so
        // remove '\r' and '\n' at the end of the string and split data read
        String[] receivedMessage = data.substring(0, data.length() - 2).split(delimiter);
        String receivedAddress = receivedMessage[0];
        String receivedStatus = receivedMessage[1];

        ProtocolRead event = new ProtocolRead(this, "arduinousb", receivedAddress);
        if (receivedStatus.equalsIgnoreCase("on")) {
            event.addProperty("isOn", "true");
        } else {
            event.addProperty("isOn", "false");
        }
        this.notifyEvent(event);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //not nothing. This plugins doesn't listen to freedomotic events
    }
}
