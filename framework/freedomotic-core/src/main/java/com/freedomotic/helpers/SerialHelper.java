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
package com.freedomotic.helpers;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * @author Enrico Nicoletti
 */
public class SerialHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SerialHelper.class.getName());
    private SerialPort serialPort;
    private String portName;
    private String readTerminator = "";
    private int readChunkSize = -1;
    StringBuilder readBuffer = new StringBuilder();

    /**
     * Accepts default parameters and change only the port name. The connect()
     * method should be called to initialize the serial port connection.
     *
     * @param portName
     * @param baudRate
     * @param dataBits
     * @param stopBits
     * @param parity
     * @param consumer
     * @throws jssc.SerialPortException
     */
    public SerialHelper(final String portName, int baudRate, int dataBits, int stopBits, int parity, final SerialPortListener consumer) throws SerialPortException {
        this.portName = portName;
        serialPort = new SerialPort(this.portName);
        boolean isOpen = serialPort.openPort();
        serialPort.addEventListener(new SerialPortEventListener() {

            @Override
            public void serialEvent(SerialPortEvent event) {
                if (event.isRXCHAR()) {

                    if (event.getEventValue() > 0) {
                        try {
                            readBuffer.append(new String(serialPort.readBytes()));
                        } catch (SerialPortException ex) {
                            LOG.warn(ex.getMessage());
                        }
                    }
                    LOG.info("Received message ''{}'' from serial port {}", new Object[]{readBuffer.toString(), portName});
                    sendReadData(consumer);
                }
            }
        });
        serialPort.setParams(baudRate, dataBits, stopBits, parity);
    }

    /**
     * Sends a string message to the device.
     *
     * @param message The message to send
     * @return true if executed succesfully, false otherwise
     * @throws jssc.SerialPortException
     */
    public boolean write(String message) throws SerialPortException {
        LOG.info("Writing {} to serial port {}", new Object[]{message, portName});
        return serialPort.writeString(message);
    }

    /**
     * Sends a bytes message to the device
     *
     * @param bytes The message to send
     * @return
     * @throws jssc.SerialPortException
     */
    public boolean write(byte[] bytes) throws SerialPortException {
        LOG.info("Writing bytes '{}' to serial port {}", new Object[]{bytes.toString(), portName});
        return serialPort.writeBytes(bytes);
    }

    public String[] getPortNames() {
        String[] serialPortList = SerialPortList.getPortNames();
        if (serialPortList.length == 0) {
            LOG.error("No serial ports found");
        }
        return (serialPortList);
    }

    /**
     * Returns port name
     *
     * @return portName
     */
    public String getPortName() {
        return serialPort.getPortName();
    }

    /**
     * Returns port status
     *
     * @return isOpened
     */
    public boolean isOpened() {
        return serialPort.isOpened();
    }

    public boolean disconnect() {
        try {
            return serialPort.closePort();
        } catch (SerialPortException ex) {
            LOG.warn("Error while closing serial port " + serialPort.getPortName(), ex);
            return false;
        }
    }

    public void setDTR(boolean enabled) {
        try {
            serialPort.setDTR(enabled);
        } catch (SerialPortException ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void setRTS(boolean enabled) {
        try {
            serialPort.setRTS(enabled);
        } catch (SerialPortException ex) {
            LOG.error(ex.getMessage());
        }
    }

    public void setChunkTerminator(String readTerminator) {
        this.readTerminator = readTerminator;
        //disable chunk size splitting
        this.readChunkSize = -1;
    }

    public void setChunkSize(int chunkSize) {
        this.readChunkSize = chunkSize;
        // disable chunk terminator splitting
        this.readTerminator = "";
    }

    public void sendReadData(SerialPortListener consumer) {
        String bufferContent = readBuffer.toString();
        // if a terminator is configured
        if (!readTerminator.isEmpty()) {
            // consume chunks until terminator string is reached
            while (bufferContent.contains(readTerminator)) {
                int endOfTerminator = bufferContent.indexOf(readTerminator) + readTerminator.length();
                String chunk = bufferContent.substring(0, endOfTerminator);
                //remove this chunk of data from bufferContent
                bufferContent = bufferContent.substring(endOfTerminator);
                consumer.onDataAvailable(chunk);
            }
        } else {
            if (readChunkSize > 0) {
                // consume chunks of the given size
                while (readChunkSize > 0 && bufferContent.length() >= readChunkSize) {
                    String chunk = bufferContent.substring(0, readChunkSize);
                    //remove this chunk of data from bufferContent
                    bufferContent = bufferContent.substring(readChunkSize);
                    consumer.onDataAvailable(chunk);

                }
            } else {
                // no splitting, send it just as readed
                consumer.onDataAvailable(bufferContent);
                //clear from sent text
                bufferContent = "";
            }
        }

        // Clear the buffer for sent text
        readBuffer.setLength(0);
        readBuffer.append(bufferContent);
    }
}
