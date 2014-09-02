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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.freedomotic.serial;
package com.freedomotic.plugins.devices.arduinousb;


import com.freedomotic.util.Info;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * Provides connection to serial ports and handles serial port reading and
 * writing. One or more SerialDataConsumer can be registered to receive a copy
 * of the data read from serial
 *
 * To use this class make your client class to implement the SerialDataConsumer
 * interface and pass a 'this' reference in the constructor of
 * SerialConnectionProvider.
 *
 * @author enrico
 */
public class SerialConnectionProvider_1 implements SerialPortEventListener {

    private static boolean ALREADY_IN_CLASSPATH = false;

    private static final Logger LOG = Logger.getLogger(SerialConnectionProvider_1.class.getName());

    private SerialPort serialPort;
    private InputStream in;
    private OutputStream out;
    private boolean isConnected = false;
    private List<SerialDataConsumer> consumers = new ArrayList<SerialDataConsumer>();

    //default serial port configuration
    private int PORT_BAUDRATE = 19200;
    private int PORT_DATABITS = SerialPort.DATABITS_8;
    private int PORT_STOPBITS = SerialPort.STOPBITS_1;
    private int PORT_PARITY = SerialPort.PARITY_NONE;
    private final int PORT_OPENTIME = 2000;
    private final int PORT_IN_TIMEOUT = 1000;
    private final int PORT_IN_THRESHOLD = 1024;
    private String portName = "/dev/ttyUSB0";

    /**
     * Send a list of properties specifying the connection parameters. The
     * connect() method should be called to initialize the serial port
     * connection.
     *
     * @param config
     * @param consumer A reference to a class implementing the
     * SerialDataConsumer interface
     */
    public SerialConnectionProvider_1(Properties config, SerialDataConsumer consumer) {
        addListener(consumer);
        init(config);
    }

    /**
     * Accept default parameters and change only the port name. The connect()
     * method should be called to initialize the serial port connection.
     *
     * @param portName
     * @param consumer A reference to a class implementing the
     * SerialDataConsumer interface
     */
    public SerialConnectionProvider_1(String portName, SerialDataConsumer consumer) {
        addListener(consumer);
        init(new Properties());
        setPortName(portName);
    }

    /**
     * Accept default parameters The connect() method should be called to
     * initialize the serial port connection.
     *
     * @param consumer A reference to a class implementing the
     * SerialDataConsumer interface
     */
    public SerialConnectionProvider_1(SerialDataConsumer consumer) {
        addListener(consumer);
        init(new Properties());
    }

    /**
     * Just for backward compatibility with previous version of
     * SerialConnectionProvider
     *
     * @param config
     */
    public SerialConnectionProvider_1(Properties config) {
        init(config);
    }

    /**
     * Just for backward compatibility with previous version of
     * SerialConnectionProvider
     *
     */
    public SerialConnectionProvider_1() {
        init(new Properties());
    }

    /**
     * Allow a new serial data listeners to observe the data read from this
     * connection
     *
     * @param consumer
     */
    public final void addListener(SerialDataConsumer consumer) {
        this.consumers.add(consumer);
    }

    /**
     * Send a string message to the device
     *
     * @param message The message to send
     */
    public void send(String message) {
        LOG.log(Level.CONFIG, "Writing '{}' to serial port {}", new String[]{message, portName});
        try {
            out.write(message.getBytes());
            out.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error writing '{}' to serial port {}: {}", new String[]{message, portName, e.getMessage()});
        }
    }

    /**
     * Send a bytes message to the device
     *
     * @param bytes The message to send
     */
    public void send(byte[] bytes) {
        LOG.log(Level.CONFIG, "Writing bytes '{}' to serial port {}", new String[]{bytes.toString(), portName});
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Error writing bytes '{}' to serial port {}: {}", new String[]{bytes.toString(), portName, e.getMessage()});
        }
    }

    /**
     * Read data produced by the connected device
     *
     * @param event
     */
    @Override
    public void serialEvent(SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPortEvent.BI:
            case SerialPortEvent.OE:
            case SerialPortEvent.FE:
            case SerialPortEvent.PE:
            case SerialPortEvent.CD:
            case SerialPortEvent.CTS:
            case SerialPortEvent.DSR:
            case SerialPortEvent.RI:
            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                break;
            case SerialPortEvent.DATA_AVAILABLE:
                // if data has been received
                StringBuilder buffer = new StringBuilder();
                byte[] readBuffer = new byte[20];
                try {
                    do {
                        // read data from serial device
                        while (in.available() > 0) {
                            int bytes = in.read(readBuffer);
                            buffer.append(new String(readBuffer, 0, bytes));
                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            //do nothing
                        }
                    } while (in.available() > 0);

                    String result = buffer.toString();

                    LOG.log(Level.FINE, "Received message '{}' on serial port {}", new String[]{result, portName});
                    //dispatch the message to subscribers
                    for (SerialDataConsumer handler : consumers) {
                        handler.onDataAvailable(result);
                    }
                    break;

                } catch (IOException e) {
                    LOG.log(Level.WARNING, "Error receiving data on serial port " + portName, e);
                }
                break;
        }
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void connect() {
        //is already connected
        if (isConnected) {
            return;
        }

        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();
            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                LOG.log(Level.INFO, "Found serial port '{}'", portName);
                if (portId.getName().equals(portName)) {
                    openPort(portId);
                    if (serialPort == null) {
                        isConnected = false;
                        LOG.log(Level.WARNING, "Cannot connect to serial port {0}", portId.getName());
                    } else {
                        LOG.log(Level.INFO, "Connected to serial port {0}", portId.getName());
                        // Add this object as an event listener for the serial port.
                        try {
                            serialPort.addEventListener(this);
                        } catch (TooManyListenersException e) {
                            serialPort.close();
                            LOG.log(Level.WARNING, "Too many listeners on serial port {0}", portId.getName());
                        }
                        serialPort.notifyOnDataAvailable(true);
                        serialPort.notifyOnBreakInterrupt(true);
                        try {
                            serialPort.enableReceiveTimeout(30);
                        } catch (UnsupportedCommOperationException e) {
                            //do nothing
                        }
                        isConnected = true;
                    }
                }
            }
        }

    }

    public void disconnect() {
        if (serialPort != null) {
            closePort();
            serialPort = null;
            isConnected = false;
        }
    }

    private void updateJavaLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalAccessException {
        // Resetto il salvataggio della variabile nella classe ClassLoader
        Class<ClassLoader> loaderClass = ClassLoader.class;
        Field userPaths = loaderClass.getDeclaredField("sys_paths");

        userPaths.setAccessible(true);

        userPaths.set(null, null);

        String os = System.getProperty("os.name").toLowerCase();
        String arch = System.getProperty("os.arch").toLowerCase();
        if (os.contains("windows")) {
            os = "windows";
        }
        if (os.contains("linux")) {
            os = "linux";
        }
        String sep = System.getProperty("file.separator");
        pathToAdd = new File(Info.getApplicationPath() + sep + pathToAdd + sep + os + sep + arch).toString();
        // Modifico il valore della variabile a livello d'ambiente
        //LOG.info("Adding to classpath '" + pathToAdd + "'");
        File dynLibraryFile = new File(pathToAdd);
        if (!dynLibraryFile.exists() || (dynLibraryFile.isDirectory() && dynLibraryFile.list().length < 1)) {
            LOG.log(Level.WARNING, "Missing serial port (RXTX) dynamic library for this architecture in {0}", pathToAdd.toString());
        }
        String libraryPath
                = System.getProperty("java.library.path")
                .concat(System.getProperty("path.separator")
                        + pathToAdd)
                .concat(System.getProperty("path.separator")
                        + new File(Info.getApplicationPath() + "/config/serial/").toString());
        System.setProperty("java.library.path", libraryPath);
    }

    /**
     * Close this serial device
     */
    private void closePort() {
        serialPort.removeEventListener();
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
        serialPort.close();
    }

    private boolean openPort(CommPortIdentifier port) {
        //check port reference
        if (port == null) {
            LOG.log(Level.WARNING, "Unspecified serial port identifier when connecting to serial port");
            return false;
        }

        // is port already in use?
        if (port.isCurrentlyOwned()) {
            LOG.log(Level.WARNING, "Port \"" + port.getName() + "\" in use.");
            return false;
        }

        try {
            CommPort commPort = port.open("freedomotic", PORT_OPENTIME);//2 seconds wait
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;
            } else {
                LOG.log(Level.WARNING, "Port {0} is not a serial port", port.getName());
                commPort.close();
                return false;
            }
        } catch (PortInUseException e) {
            LOG.log(Level.WARNING, "Port {0} in use", port.getName());
            return false;
        }

        //setting connection parameters
        try {
            serialPort.setSerialPortParams(PORT_BAUDRATE, PORT_DATABITS, PORT_STOPBITS, PORT_PARITY);
            serialPort.enableReceiveTimeout(PORT_IN_TIMEOUT);
            serialPort.enableReceiveThreshold(PORT_IN_THRESHOLD);
        } catch (UnsupportedCommOperationException e) {
            LOG.log(Level.WARNING, "Parameters not allowed for port {0}", port.getName());
            closePort();
            return false;
        }

        try {
            this.in = serialPort.getInputStream();
            this.out = serialPort.getOutputStream();
        } catch (IOException e) {
            LOG.log(Level.WARNING, "Can''t open streams of the port {0}", port.getName());
            closePort();
            return false;
        }
        isConnected = true;
        return true;
    }

    private void init(Properties config) {
        try {
            if (!ALREADY_IN_CLASSPATH) {
                updateJavaLibraryPath("config/serial");
                ALREADY_IN_CLASSPATH = true;
            }
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(SerialConnectionProvider_1.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SerialConnectionProvider_1.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * serial connection data
         */
        PORT_BAUDRATE = Integer.parseInt(config.getProperty("baudrate", "19200"));
        PORT_DATABITS = Integer.parseInt(config.getProperty("data-bits", new Integer(SerialPort.DATABITS_8).toString()));
        PORT_STOPBITS = Integer.parseInt(config.getProperty("stop-bits", new Integer(SerialPort.STOPBITS_1).toString()));
        PORT_PARITY = Integer.parseInt(config.getProperty("parity", new Integer(SerialPort.PARITY_NONE).toString()));
        portName = config.getProperty("port", null);

    }

    /**
     *
     * @param PORT_BAUDRATE
     */
    public void setPortBaudrate(int PORT_BAUDRATE) {
        this.PORT_BAUDRATE = PORT_BAUDRATE;
    }

    /**
     *
     * @param PORT_DATABITS
     */
    public void setPortDatabits(int PORT_DATABITS) {
        this.PORT_DATABITS = PORT_DATABITS;
    }

    /**
     *
     * @param PORT_NAME
     */
    public final void setPortName(String PORT_NAME) {
        this.portName = PORT_NAME;
    }

    /**
     *
     * @param PORT_PARITY
     */
    public void setPortParity(int PORT_PARITY) {
        this.PORT_PARITY = PORT_PARITY;
    }

    /**
     *
     * @param PORT_STOPBITS
     */
    public void setPortStopbits(int PORT_STOPBITS) {
        this.PORT_STOPBITS = PORT_STOPBITS;
    }

}
