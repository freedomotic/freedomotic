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

import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jssc.*;

/**
 * Provides connection to serial ports and handles serial port reading and
 * writing. One or more SerialPortListener can be registered to receive a copy
 * of the data read from serial.
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
    private StringBuilder readBuffer = new StringBuilder();

    /**
     * Accepts default parameters and change only the port name. The connect()
     * method should be called to initialize the serial port connection.
     *
     * @param portName serial port name
     * @param baudRate serial port baud rate
     * @param dataBits serial port data bits
     * @param stopBits serial port stop bits
     * @param parity serial port parity bit
     * @param consumer serial port listener
     * @throws jssc.SerialPortException
     */
    public SerialHelper(final String portName, int baudRate, int dataBits, int stopBits, int parity, final SerialPortListener consumer) throws SerialPortException {
        this.portName = portName;
        serialPort = new SerialPort(this.portName);
        serialPort.addEventListener( event -> handleEvent(portName, consumer, event));
        serialPort.setParams(baudRate, dataBits, stopBits, parity);
    }

	/**
	 * Handles an event coming from a serial input.
	 * 
	 * @param portName serial port name
	 * @param consumer serial port listener
	 * @param event event to deal with
	 */
	private void handleEvent(final String portName, final SerialPortListener consumer, SerialPortEvent event) {
		if (event.isRXCHAR()) {

            if (event.getEventValue() > 0) {
                try {
                    readBuffer.append(new String(serialPort.readBytes()));
                } catch (SerialPortException ex) {
                    LOG.warn(ex.getMessage());
                }
            }
            LOG.info("Received message \"{}\" from serial port \"{}\"", readBuffer.toString(), portName);
            sendReadData(consumer);
        }
	}

    /**
     * Sends a string message to the device.
     *
     * @param message the message to send
     * @return true if executed succesfully, false otherwise
     * @throws jssc.SerialPortException
     */
    public boolean write(String message) throws SerialPortException {
        LOG.info("Writing \"{}\" to serial port \"{}\"", message, portName);
        return serialPort.writeString(message);
    }

    /**
     * Sends a bytes message to the device.
     *
     * @param bytes the message to send
     * @return
     * @throws jssc.SerialPortException
     */
    public boolean write(byte[] bytes) throws SerialPortException {
        LOG.info("Writing bytes \"{}\" to serial port \"{}\"", Arrays.toString(bytes), portName);
        return serialPort.writeBytes(bytes);
    }

    /**
     * Returns port names.
     *
     * @return a string vector of port names
     */
    public String[] getPortNames() {
        String[] serialPortList = SerialPortList.getPortNames();
        if (serialPortList.length == 0) {
            LOG.error("No serial ports found");
        }
        return serialPortList;
    }

    /**
     * Returns port name.
     *
     * @return portName the port name
     */
    public String getPortName() {
        return serialPort.getPortName();
    }

    /**
     * Returns port status.
     *
     * @return isOpened true if the port is opened, false otherwise
     */
    public boolean isOpened() {
        return serialPort.isOpened();
    }

    /**
     * Disconnects the port.
     *
     * @return true if deconnected, false instead
     */
    public boolean disconnect() {
        try {
            return serialPort.closePort();
        } catch (SerialPortException ex) {
            LOG.warn("Error while closing serial port \"" + serialPort.getPortName() + "\"", ex);
            return false;
        }
    }

    /**
     * Sets port DTR.
     *
     * @param enabled DTR flag value
     */
    public void setDTR(boolean enabled) {
        try {
            serialPort.setDTR(enabled);
        } catch (SerialPortException ex) {
            LOG.error(ex.getMessage());
        }
    }

    /**
     * Sets port RTS.
     *
     * @param enabled RTS flag value
     */
    public void setRTS(boolean enabled) {
        try {
            serialPort.setRTS(enabled);
        } catch (SerialPortException ex) {
            LOG.error(ex.getMessage());
        }
    }

    /**
     * Sets chunck terminator.
     *
     * @param readTerminator the terminator symbol used for data splitting
     */
    public void setChunkTerminator(String readTerminator) {
        this.readTerminator = readTerminator;
        //disable chunk size splitting
        this.readChunkSize = -1;
    }

    /**
     * Sets chunck size.
     *
     * @param chunkSize the chunck size used for data splitting
     */
    public void setChunkSize(int chunkSize) {
        this.readChunkSize = chunkSize;
        // disable chunk terminator splitting
        this.readTerminator = "";
    }

    /**
     * Sends read data to the listener.
     *
     * @param consumer serial port listener
     */
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
        } else if (readChunkSize > 0) {
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

        // Clear the buffer for sent text
        readBuffer.setLength(0);
        readBuffer.append(bufferContent);
    }
}
