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

    static class SerialPortReader implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            int eventValue = event.getEventValue();
            if (event.isRXCHAR()) {//If data is available
                if (eventValue >= 15) {// <---- don't check for exact length but also for anything larger
                //Read data, if 15 bytes or more available
                    try {
                        byte buffer[] = serialPort.readBytes(eventValue);
                        // call sendChanges(String data);
                        LOG.info("Read data: " + new String(buffer, 0, buffer.length));
                    } catch (SerialPortException ex) {
                        System.out.println(ex);
                    }
                } else if (event.isCTS()) {//If CTS line has changed state
                    if (event.getEventValue() == 1) {//If line is ON
                        System.out.println("CTS - ON");
                    } else {
                        System.out.println("CTS - OFF");
                    }
                } else if (event.isDSR()) {///If DSR line has changed state
                    if (event.getEventValue() == 1) {//If line is ON
                        System.out.println("DSR - ON");
                    } else {
                        System.out.println("DSR - OFF");
                    }
                }
            }
        }
    }
}
