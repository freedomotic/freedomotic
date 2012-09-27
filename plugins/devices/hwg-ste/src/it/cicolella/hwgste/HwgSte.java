/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.cicolella.hwgste;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**
 * A sensor for the board IPX800 developed by author Mauro Cicolella -
 * www.emmecilab.net For more details please refer to
 *
 */
public class HwgSte extends Protocol {

    private static ArrayList<Board> boards = null;
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private String SNMP_OID = configuration.getStringProperty("snmp-oid", "1.3.6.1.4.1.21796.4");
    private int SNMP_PORT = configuration.getIntProperty("snmp-port", 161);
    private String SNMP_COMMUNITY = configuration.getStringProperty("snmp-community", "public");
    private String SENSOR_NAME_REQUEST = configuration.getStringProperty("sensor-name-request", "1.3.1.2");
    private String SENSOR_STATE_REQUEST = configuration.getStringProperty("sensor-state-request", "1.3.1.3");
    private String SENSOR_VALUE_REQUEST = configuration.getStringProperty("sensor-value-request", "1.3.1.5");
    private String SENSOR_SN_REQUEST = configuration.getStringProperty("sensor-sn-request", "1.3.1.6");
    private String SENSOR_UNIT_REQUEST = configuration.getStringProperty("sensor-unit-request", "1.3.1.7");
    private String SENSOR_ID_REQUEST = configuration.getStringProperty("sensor-id-request", "1.3.1.8");

    /**
     * Initializations
     */
    public HwgSte() {
        super("HwgSte", "/it.cicolella.hwgste/hwgste.xml");
        setPollingWait(POLLING_TIME);
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            int portToQuery;
            int sensorsNumber;
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portToQuery = configuration.getTuples().getIntProperty(i, "port-to-query", 80);
            sensorsNumber = configuration.getTuples().getIntProperty(i, "sensors-number", 1);
            Board board = new Board(ipToQuery, portToQuery, sensorsNumber);
            boards.add(board);
            setDescription(getDescription() + " " + ipToQuery + ":" + portToQuery + ";");
        }
    }

    /**
     * Connection to boards
     */
    private boolean connect(String address, int port) {

        Freedomotic.logger.info("Trying to connect to HWg-STE device on address " + address + ':' + port);
        try {
            //TimedSocket is a non-blocking socket with timeout on exception
            socket = TimedSocket.getSocket(address, port, SOCKET_TIMEOUT);
            socket.setSoTimeout(SOCKET_TIMEOUT); //SOCKET_TIMEOUT ms of waiting on socket read/write
            BufferedOutputStream buffOut = new BufferedOutputStream(socket.getOutputStream());
            outputStream = new DataOutputStream(buffOut);
            return true;
        } catch (IOException e) {
            Freedomotic.logger.severe("Unable to connect to host " + address + " on port " + port);
            return false;
        }
    }

    private void disconnect() {
        // close streams and socket
        try {
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (Exception ex) {
            //do nothing. Best effort
        }
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        BOARD_NUMBER = configuration.getTuples().size();
        setPollingWait(POLLING_TIME);
        loadBoards();
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        boards.clear();
        boards = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Ipx800"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            SNMPRequest(board);
            //evaluateDiffs(getXMLStatusFile(board), board); //parses the xml and crosscheck the data with the previous read
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(HwgSte.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void SNMPRequest(Board board) {
        final MYSNMP snmpRequest = new MYSNMP();
        for (int i = 1; i <= board.getSensorsNumber(); i++) {
            String sensorName = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_NAME_REQUEST + "." + i, SNMP_COMMUNITY);
            System.out.println("Name =" + sensorName);
            String sensorSN = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_SN_REQUEST + "." + i, SNMP_COMMUNITY);
            System.out.println("SN =" + sensorSN);
            Integer sensorID = Integer.parseInt(snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_ID_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("ID =" + sensorID);
            Integer sensorState = Integer.parseInt(snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_STATE_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("State =" + sensorState);
            Integer sensorValue = Integer.parseInt(snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_VALUE_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("Value =" + sensorValue);
            Integer sensorUnit = Integer.parseInt(snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, "." + SNMP_OID + "." + SENSOR_UNIT_REQUEST + "." + i, SNMP_COMMUNITY));
            System.out.println("Unit =" + sensorUnit);
            String state = null;
            switch (sensorState) {
                case 0:
                    state = "invalid";
                case 1:
                    state = "normal";
                case 2:
                    state = "outOfRangeLow";
                case 3:
                    state = "outOfRangeHigh";
                case 4:
                    state = "alarmLow";
                case 5:
                    state = "alarmHigh";
            }
            String unit = null;
            switch (sensorUnit) {
                case 0:
                    unit = "unknown";
                case 1:
                    unit = "C";
                case 2:
                    unit = "F";
                case 3:
                    unit = "K";
                case 4:
                    unit = "%";
            }

            String address = board.getIpAddress() + ":" + sensorID;
            Freedomotic.logger.info("Sending HWg-STE protocol read event for object address '" + address + "'");
            //building the event
            ProtocolRead event = new ProtocolRead(this, "hwgste", address);
            //adding some optional information to the event
            event.addProperty("sensor.unit", unit);
            event.addProperty("sensor.value", sensorValue.toString());
            event.addProperty("sensor.name", sensorName);
            event.addProperty("sensor.state", state);
            //publish the event on the messaging bus
            this.notifyEvent(event);
        }
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        // do nothing    
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
