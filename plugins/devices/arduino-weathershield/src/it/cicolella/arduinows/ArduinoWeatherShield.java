package it.cicolella.arduinows;

import it.freedomotic.api.Sensor;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A sensor for the Arduino Weather Shield developed by www.ethermania.com
 * author Mauro Cicolella - www.emmecilab.net
 * For details please refer to http://www.ethermania.com/shop/index.php?main_page=product_info&cPath=91_104&products_id=612
 */
public class ArduinoWeatherShield extends Sensor {

    private static int POLLING_TIME = 10000;
    private int SOCKET_TIMEOUT = 10000;
    private static String IP_TO_QUERY;
    private static String DELIMITER;
    private static int PORT_TO_QUERY;
    private static int BOARD_NUMBER;
    private static ArrayList<Board> boards = null;
    private String response = null;
    private boolean connected = false;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;

    public ArduinoWeatherShield() {
        super("Arduino WeatherShield", "/it.cicolella.arduinows/arduinows-sensors.xml");
        this.setAsPollingSensor(); //onRun in a loop
        BOARD_NUMBER = configuration.getTuples().size();
        loadBoards();
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        for (int i = 0; i < BOARD_NUMBER; i++) {
            IP_TO_QUERY = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            PORT_TO_QUERY = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            POLLING_TIME = configuration.getTuples().getIntProperty(i, "polling-time", 1000);
            SOCKET_TIMEOUT = configuration.getTuples().getIntProperty(i, "socket-timeout", 1000);
            DELIMITER = configuration.getTuples().getStringProperty(i, "delimiter", "|");
            Board board = new Board(IP_TO_QUERY, PORT_TO_QUERY, POLLING_TIME, SOCKET_TIMEOUT, DELIMITER);
            boards.add(board);
        }

    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            try {
                getParametersValue(board);
            } catch (UnableToExecuteException ex) {
                Logger.getLogger(ArduinoWeatherShield.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(ArduinoWeatherShield.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(ArduinoWeatherShield.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void connect(String address, int port) {
        connected = false;
        Freedomotic.logger.info("Trying to connect to Arduino WeatherShield on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
        } catch (IOException e) {
            connected = false;
            Freedomotic.logger.severe("Unable to connect to host " + address + " on port " + port);
        }
        connected = true;
    }

    private void disconnect() {
        // close streams and socket
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
            connected = false;
        } catch (Exception ex) {
            //do nothing. Best effort
        }
    }

    private void getParametersValue(Board board) throws UnableToExecuteException, IOException {
        String parametersValue = null;
        //connect to Arduino
        try {
            connect(board.getIpAddress(), board.getPort());
        } catch (ArrayIndexOutOfBoundsException outEx) {
            Freedomotic.logger.severe("The address '" + board.getIpAddress() + "' is not properly formatted. Check it!");
            throw new UnableToExecuteException();
        } catch (NumberFormatException numberFormatException) {
            Freedomotic.logger.severe(board.getIpAddress() + " is not a valid ethernet port to connect to");
            throw new UnableToExecuteException();
        }

        if (connected) {
            //Freedom.logger.severe("CONNECTED"); //FOR DEBUG USE
            // request for sensors values
            String message = "GET / HTTP 1.1\r\n\r\n";
            //Freedom.logger.severe("Sending message" + message); //FOR DEBUG USE
            if (outputStream != null) {
                outputStream.writeBytes(message);
                outputStream.flush();
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                try {
                    parametersValue = inputStream.readLine(); // read device reply
                    //Freedom.logger.severe("Read string " + parametersValue + ""); // FOR DEBUG USE
                    sendChanges(board, parametersValue);
                } catch (IOException iOException) {
                    throw new IOException();
                } finally {
                    disconnect();
                    //Freedom.logger.severe("DISCONNECTED"); //FOR DEBUG USE
                }
            } else {
                throw new UnableToExecuteException();
            }
        }
    }

    private void sendChanges(Board board, String parametersValue) {
        String address = board.getIpAddress() + ":" + board.getPort();
        Freedomotic.logger.severe("Sending Arduino WeatherShield protocol read event for board '" + address + "'");
        String values[] = parametersValue.split(board.getDelimiter());
        //building the event
        ProtocolRead event = new ProtocolRead(this, "ArduinoWeatherShield", address); //IP:PORT
        //adding some optional information to the event
        event.addProperty("boardIP", board.getIpAddress());
        event.addProperty("boardPort", new Integer(board.getPort()).toString());
        event.addProperty("temperature", values[0]);
        event.addProperty("pressure", values[1]);
        event.addProperty("humidity", values[2]);
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }
}
