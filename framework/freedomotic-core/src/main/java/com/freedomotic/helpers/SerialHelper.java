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
package com.freedomotic.helpers;

import java.util.logging.Level;
import java.util.logging.Logger;
import jssc.*;

/**
 * Provides connection to serial ports and handles serial port reading and
 * writing. One or more SerialPortListener can be registered to receive a copy
 * of the data read from serial
 *
 * To use this class make your client class to implement the SerialPortListener
 * interface and pass a 'this' reference in the constructor of
 * SerialConnectionProvider.
 *
 * @author enrico
 */
public class SerialHelper {

    private static final Logger LOG = Logger.getLogger(SerialHelper.class.getName());
    private SerialPort serialPort;
    private String portName;

    /**
     * Accept default parameters and change only the port name. The connect()
     * method should be called to initialize the serial port connection.
     *
     * @param portName
     * @param baudRate
     * @param dataBits
     * @param stopBits
     * @param parity
     * @param consumer
     */
    public SerialHelper(final String portName, int baudRate, int dataBits, int stopBits, int parity, final SerialPortListener consumer) {
        try {
            this.portName = portName;
            serialPort = new SerialPort(this.portName);
            serialPort.openPort();
            serialPort.addEventListener(new SerialPortEventListener() {

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.isRXCHAR()) {
                        StringBuilder buffer = new StringBuilder();

                        if (event.getEventValue() > 0) {
                            try {
                                buffer.append(new String(serialPort.readBytes()));
                            } catch (SerialPortException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }
                        }
                        LOG.log(Level.CONFIG, "Received message ''{0}'' from serial port {1}", new Object[]{buffer.toString(), portName});
                        consumer.onDataAvailable(buffer.toString());
                    }
                }
            });
            serialPort.setParams(baudRate, dataBits, stopBits, parity);
        } catch (SerialPortException ex) {
            LOG.log(Level.WARNING, "Error while setting serial port " + this.portName, ex);
        }
    }

    /**
     * Send a string message to the device
     *
     * @param message The message to send
     */
    public void write(String message) {
        try {
            LOG.log(Level.CONFIG, "Writing {0} to serial port {1}", new Object[]{message, portName});
            serialPort.writeString(message);
        } catch (SerialPortException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    /**
     * Send a bytes message to the device
     *
     * @param bytes The message to send
     */
    public void write(byte[] bytes) {
        LOG.log(Level.CONFIG, "Writing bytes '{0}' to serial port {1}", new Object[]{bytes.toString(), portName});
        try {
            serialPort.writeBytes(bytes);
        } catch (SerialPortException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
    }

    public boolean disconnect() {
        try {
            return serialPort.closePort();
        } catch (SerialPortException ex) {
            LOG.log(Level.WARNING, "Error while closing serial port " + serialPort.getPortName(), ex);
            return false;
        }
    }
}
