/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class SerialConnectionProvider implements SerialPortEventListener {

    private CommPortIdentifier currentPortID = null;
    private SerialPort currentPort = null;
    private InputStream in;
    private OutputStream out;
    private String APP_ID = "";
    private static boolean ALREADY_IN_CLASSPATH = false;
    private int PORT_BAUDRATE = 19200;
    private int PORT_DATABITS = SerialPort.DATABITS_8;
    private int PORT_STOPBITS = SerialPort.STOPBITS_1;
    private int PORT_PARITY = SerialPort.PARITY_NONE;
    private final int PORT_OPENTIME = 2000;
    private final int PORT_IN_TIMEOUT = 1000;
    private final int PORT_IN_THRESHOLD = 1024;
    private String PORT_NAME;
    private String HELLO_MESSAGE;
    private String POLLING_MESSAGE = "";
    private boolean isConnected = false;
    private String HELLO_REPLY;
    private String DEVICE_NAME = "";
    private int POLLING_TIME;
    private long MAX_WAIT_RESPONSE_TIME;
    private ArrayList<SerialDataConsumer> consumers = new ArrayList<SerialDataConsumer>();

    public SerialConnectionProvider(Properties config) {
        init(config);
    }

    public SerialConnectionProvider() {
        init(new Properties());
    }

    public void addListener(SerialDataConsumer consumer) {
        this.consumers.add(consumer);
    }

    private void init(Properties config) {
        try {
            if (!ALREADY_IN_CLASSPATH) {
                updateJavaLibraryPath("config/serial");
                ALREADY_IN_CLASSPATH = true;
            }
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
        }
        /*
         * serial connection data
         */
        PORT_BAUDRATE = Integer.parseInt(config.getProperty("baudrate", "19200"));
        PORT_DATABITS = Integer.parseInt(config.getProperty("data-bits", new Integer(SerialPort.DATABITS_8).toString()));
        PORT_STOPBITS = Integer.parseInt(config.getProperty("stop-bits", new Integer(SerialPort.STOPBITS_1).toString()));
        PORT_PARITY = Integer.parseInt(config.getProperty("parity", new Integer(SerialPort.PARITY_NONE).toString()));
        PORT_NAME = config.getProperty("port", null);

        /*
         * device related data
         */
        POLLING_MESSAGE = config.getProperty("polling-message", "");
        POLLING_TIME = Integer.parseInt(config.getProperty("polling-time", "100"));
        DEVICE_NAME = config.getProperty("device-name", "undefined");
        HELLO_MESSAGE = config.getProperty("hello-message", null);
        HELLO_REPLY = config.getProperty("hello-reply", null);
        /*
         * other data
         */
        MAX_WAIT_RESPONSE_TIME = Long.parseLong(config.getProperty("wait-response-time", "1000000000"));
    }

    private CommPortIdentifier discoverDevice() {
//        //I already know the port id
//        if (currentPortID != null) {
//            return currentPortID;
//        }
//
//        //for every active port
//        Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
//        while (portEnum.hasMoreElements()) {
//            CommPortIdentifier portIdentifier = portEnum.nextElement();
//            System.out.println("Searching the device on serial port " + portIdentifier.getName());
//            //if the port has something listening on it
//            if (isSomethingListeningOn(portIdentifier)) {
//                //identificate the device sending the hello message specified in the contructor. If the response is as espected we have found the device
//                if (isTheDeviceWeAreLookingFor(portIdentifier)) {
//                    return portIdentifier;
//                }
//            }
//        }
//        //no device found
        return null;
    }

    private CommPortIdentifier discoverDevice(String portName) {
        //i already know the port id
        if (currentPortID != null) {
            return currentPortID;
        }

        CommPortIdentifier portIdentifier = null;
        try {
            portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException ex) {
            System.err.println("No serial device connected to port " + portName);
        }
        //no port found
        return portIdentifier;
    }

    private boolean isSomethingListeningOn(CommPortIdentifier port) {
        SerialPort sPort = openPort(port);
        //is possible to communicate with this port. close test connection.
        if (sPort != null) {
            sPort.close();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public synchronized void serialEvent(SerialPortEvent spe) {
        switch (spe.getEventType()) {
            case SerialPortEvent.DATA_AVAILABLE:
                String readed = read();
                for (SerialDataConsumer handler : consumers) {
                    handler.onDataAvailable(readed);
                }
                break;
            // If break event append BREAK RECEIVED message.
            case SerialPortEvent.BI:
                System.out.println("\n--- BREAK RECEIVED ---\n");
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
        CommPortIdentifier portId = null;
//        if (PORT_NAME == null) { //port undefined in xml, try to discover using hello message on all ports
//            //discover using hello message
//            portId = discoverDevice();
//            if (portId == null) {
//                System.err.println("No device reply to hello-message=" + HELLO_MESSAGE);
//                isConnected = false;
//                return;
//            }
//        } else {
        //use the port name in xml to get a reference
        portId = discoverDevice(PORT_NAME); //with a given port name like COM1
//        }
        currentPort = openPort(portId);
        if (currentPort == null) {
            isConnected = false;
        } else {
            System.out.println("Connected to serial port " + portId.getName());
            // Add this object as an event listener for the serial port.
            try {
                currentPort.addEventListener(this);
            } catch (TooManyListenersException e) {
                currentPort.close();

                System.err.println("too many listeners added");
            }
            // Set notifyOnDataAvailable to true to allow event driven input.
            currentPort.notifyOnDataAvailable(true);
            // Set notifyOnBreakInterrup to allow event driven break handling.
            currentPort.notifyOnBreakInterrupt(true);
            // Set receive timeout to allow breaking out of polling loop during
            // input handling.
            try {
                currentPort.enableReceiveTimeout(30);
            } catch (UnsupportedCommOperationException e) {
            }
            // Add ownership listener to allow ownership event handling.
//            portId.addPortOwnershipListener(this);

//            if (POLLING_MESSAGE.length() != 0) { //needs polling
//                Thread t = new Thread(this);
//                t.start();
//            }
            isConnected = true;
        }
    }

    /**
     * Chiude la connessione e libera la porta alla quale è collegato il PMix35.
     * Nel caso non sia colelgato o avvengano errori nella chiusura della porta
     * restituisce false.
     *
     * @return true se la connessione viene chiusa correttamente.
     */
    public void disconnect() {
        // E' gia disconnesso
        if (currentPort != null) {
            System.out.println("Disconnecting from serial port " + currentPort.getName());
            try {
                in.close();
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
            }
            currentPort = null;
            isConnected = false;
        }
    }

    public String getPortName() {
        if (currentPort != null) {
            return currentPort.getName();
        } else {
            return "undefined-port";
        }
    }

    private static void updateJavaLibraryPath(String pathToAdd) throws NoSuchFieldException, IllegalAccessException {
        // Resetto il salvataggio della variabile nella classe ClassLoader
        Class loaderClass = ClassLoader.class;
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
        //Freedomotic.logger.info("Adding to classpath '" + pathToAdd + "'");
        File dynLibraryFile = new File(pathToAdd);
        if (!dynLibraryFile.exists() || (dynLibraryFile.isDirectory() && dynLibraryFile.list().length < 1)) {
            Freedomotic.logger.warning("Missing serial port (RXTX) dynamic library for this architecture in " + pathToAdd.toString());
        }
        String libraryPath =
                System.getProperty("java.library.path")
                .concat(System.getProperty("path.separator")
                + pathToAdd)
                .concat(System.getProperty("path.separator")
                + new File(Info.getApplicationPath() + "/config/serial/").toString());
        System.setProperty("java.library.path", libraryPath);
        //Freedomotic.logger.info("java.library.path: " + libraryPath);
    }

    //synchronous write to serial device
    public synchronized String send(String message) throws IOException {
        // Nessuna porta è stata aperta
        if (currentPort == null) {
            //lazy init of serial connection
            connect();
        }
        if (isConnected) {
            // Scrivo i dati
//            String readed = "";
            try {
                this.out.write(message.getBytes());
//                readed = read();
//                System.out.println("readed reply: " + readed);
            } catch (IOException e) {
                System.err.println("Device connected to USB port but error on writing. Unable to send '" + message + "'");
                disconnect();
                throw new IOException();
            }
//            return readed.trim();
        } //device not connected, throw exception
        return "";
    }

    //asynchronous write to serial device
//    public void asyncSend(String message) throws IOException {
////        try {
////            Thread.sleep(100); //to ensure the message is arrived
////        } catch (InterruptedException ex) {
////            Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        // Nessuna porta è stata aperta
//        if (currentPort == null) {
//            //lazy init of serial connection
//            connect();
//        }
//        if (isConnected) {
//            // Scrivo i dati
//            try {
//                this.out.write(message.getBytes());
//            } catch (IOException e) {
//                System.err.println("Device connected to USB port but error on writing. Unable to send '" + message + "'");
//                disconnect();
//                throw new IOException();
//            }
//        } else {
//            disconnect();
//            throw new IOException();
//        }
//    }
//    @Override
//    public void run() {
//        while (isConnected) {
//            //polling
//            if (POLLING_MESSAGE != null) {
//                //force data request (needed by some type of devices)
//                try {
//                    asyncSend(POLLING_MESSAGE);
//                } catch (IOException ex) {
//                    Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//            try {
//                Thread.sleep(POLLING_TIME);
//            } catch (InterruptedException ex) {
//                Logger.getLogger(SerialConnectionProvider.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
    private boolean isTheDeviceWeAreLookingFor(CommPortIdentifier portIdentifier) {
        //invia un hello message e riceve un reply. se hello e reply corrispondono a quelle dell xml allora è il device che stiamo cercando
//        SerialPort tmpPort = openPort(portIdentifier);
//        if (tmpPort != null) {
//            // opening streams
//            InputStream inStream = null;
//            OutputStream outStream = null;
//            try {
//                synchronized (tmpPort) {
//                    this.in = tmpPort.getInputStream();
//                    this.out = tmpPort.getOutputStream();
//                    try {
//                        System.out.println("Sending hello message " + HELLO_MESSAGE + " on port " + portIdentifier.getName());
//                        this.out.write(HELLO_MESSAGE.getBytes());
//                    } catch (IOException e) {
//                        tmpPort.close();
//                    }
//                    //read reply
//                    byte[] buff = new byte[1024];
//                    in.read(buff);
//                    String ret = new String(buff);
//                    System.out.println("Readed from serial " + portIdentifier.getName() + ": " + ret);
//
//                    if (ret.trim().startsWith(HELLO_REPLY.trim())) {
//                        tmpPort.close();
//                        return true;
//                    }
//                }
//            } catch (IOException e) {
//                tmpPort.close();
//                return false;
//            }
//        }
//        tmpPort.close();
//        return false;
        throw new UnsupportedOperationException();
    }

    private synchronized SerialPort openPort(CommPortIdentifier port) {
        //check port reference
        if (port == null) {
            return null;
        } // is port already in use?
        if (port.isCurrentlyOwned()) {
            System.out.println("Port \"" + port.getName() + "\" in use.");
            return null;
        } // open the port
        SerialPort serialPort = null;
        try {
            CommPort cPort = port.open(APP_ID, PORT_OPENTIME);//2 seconds wait
            if (cPort instanceof SerialPort) {
                serialPort = (SerialPort) cPort;
            } else {
                System.out.println("Port \"" + port.getName() + "\" is not a serial port.");
                cPort.close();
                return null;
            }
        } catch (PortInUseException e) {
            System.out.println("Port \"" + port.getName() + "\" in use.");
            return null;
        }

        //setting connection parameters
        try {
            serialPort.setSerialPortParams(PORT_BAUDRATE, PORT_DATABITS, PORT_STOPBITS, PORT_PARITY);
            serialPort.enableReceiveTimeout(PORT_IN_TIMEOUT);
            serialPort.enableReceiveThreshold(PORT_IN_THRESHOLD);
        } catch (UnsupportedCommOperationException e) {
            System.out.println("Parameters not allowed for port \"" + port.getName() + "\".");
            serialPort.close();
            return null;
        }

        // opening streams
        InputStream inStream = null;
        OutputStream outStream = null;
        try {
            this.in = serialPort.getInputStream();
            this.out = serialPort.getOutputStream();
        } catch (IOException e) {
            System.out.println("Can't open streams of the port \"" + port.getName() + "\".");
            serialPort.close();
            return null;
        }
        isConnected = true;
        return serialPort;
    }

    private synchronized boolean closePort(SerialPort port) {
        try {
            this.in.close();
            this.out.close();
            port.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void setDeviceName(String DEVICE_NAME) {
        this.DEVICE_NAME = DEVICE_NAME;
    }

    public void setConnectionName(String APP_ID) {
        this.APP_ID = APP_ID;
    }

    public void setAutodiscover(String HELLO_MESSAGE, String HELLO_REPLY) {
        this.HELLO_MESSAGE = HELLO_MESSAGE;
        this.HELLO_REPLY = HELLO_REPLY;
    }

    public void setPollingMode(String POLLING_MESSAGE, int POLLING_TIME) {
        this.POLLING_MESSAGE = POLLING_MESSAGE;
        this.POLLING_TIME = POLLING_TIME;
    }

    public void setPortBaudrate(int PORT_BAUDRATE) {
        this.PORT_BAUDRATE = PORT_BAUDRATE;
    }

    public void setPortDatabits(int PORT_DATABITS) {
        this.PORT_DATABITS = PORT_DATABITS;
    }

    public void setPortName(String PORT_NAME) {
        this.PORT_NAME = PORT_NAME;
    }

    public void setPortParity(int PORT_PARITY) {
        this.PORT_PARITY = PORT_PARITY;
    }

    public void setPortStopbits(int PORT_STOPBITS) {
        this.PORT_STOPBITS = PORT_STOPBITS;
    }

    private synchronized String read() {
        // Create a StringBuffer and int to receive input data.
        StringBuilder inputBuffer = new StringBuilder();
        byte[] readBuffer = new byte[400];
        int newData = 0;
        try {
            int availableBytes = in.available();
            if (availableBytes > 0) {
                // Read the serial port
                in.read(readBuffer, 0, availableBytes);
                return new String(readBuffer, 0, availableBytes);
            }
        } catch (IOException e) {
            System.err.println(e);
        }
        return "";
    }
}
