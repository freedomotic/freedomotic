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
package com.freedomotic.plugins.devices.tcw122bcm;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.codec.binary.Base64;

public class Tcw122bcm extends Protocol {

    private static final Logger LOG = Logger.getLogger(Tcw122bcm.class.getName());
    Map<String, Board> devices = new HashMap<String, Board>();
    private static int BOARD_NUMBER = 1;
    private static int POLLING_TIME = 1000;
    private Socket socket = null;
    private DataOutputStream outputStream = null;
    private BufferedReader inputStream = null;
    private String[] address = null;
    private int SOCKET_TIMEOUT = configuration.getIntProperty("socket-timeout", 1000);
    private int SNMP_PORT = configuration.getIntProperty("snmp-port", 161);
    private String SNMP_COMMUNITY = configuration.getStringProperty("snmp-community", "public");
    private String SNMP_OID = configuration.getStringProperty("snmp-oid", "1.3.6.1.4.38783");
    private String D1_VALUE = configuration.getStringProperty("digital-input1-value", "3.1.0");
    private String D2_VALUE = configuration.getStringProperty("digital-input2-value", "3.2.0");
    private String R1_STATE = configuration.getStringProperty("r1", "3.3.0");
    private String R2_STATE = configuration.getStringProperty("r2", "3.5.0");
    private String A1_VALUE = configuration.getStringProperty("analog-input1-value", "3.7.0");
    private String A2_VALUE = configuration.getStringProperty("analog2-input2-value", "3.8.0");
    private String T1_VALUE = configuration.getStringProperty("temperature1-value", "3.9.0");
    private String T2_VALUE = configuration.getStringProperty("temperature2-value", "3.10.0");
    private String H1_VALUE = configuration.getStringProperty("humidity1-value", "3.11.0");
    private String H2_VALUE = configuration.getStringProperty("humidity2-value", "3.12.0");
    private String HTTP_AUTHENTICATION = configuration.getStringProperty("http-authentication", "true");
    private String USERNAME = configuration.getStringProperty("username", "admin");
    private String PASSWORD = configuration.getStringProperty("password", "admin");

    /**
     * Initializations
     */
    public Tcw122bcm() {
        super("TCW122B-CM", "/tcw122bcm/tcw122bcm-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    // load boards data from file manifest tcw122bcm.xml
    private void loadBoards() {
        if (devices == null) {
            devices = new HashMap<String, Board>();
        }
        setDescription("Reading status changes from"); //empty description
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String ipToQuery;
            String alias;
            String relayObjectTemplate;
            String temperatureObjectTemplate;
            String humidityObjectTemplate;
            int portNumber;
            alias = configuration.getTuples().getStringProperty(i, "alias", "default");
            relayObjectTemplate = configuration.getTuples().getStringProperty(i, "relay-template", "light");
            temperatureObjectTemplate = configuration.getTuples().getStringProperty(i, "temperature-template", "thermostat");
            humidityObjectTemplate = configuration.getTuples().getStringProperty(i, "relay-humidity", "hygrometer");
            ipToQuery = configuration.getTuples().getStringProperty(i, "ip-to-query", "192.168.1.201");
            portNumber = configuration.getTuples().getIntProperty(i, "port-number", 80);
            Board board = new Board(alias, ipToQuery, portNumber, "2000", "2000", "2000", "2000", "2000", "2000", "2000", "2000", "2000", "2000",
                    relayObjectTemplate, temperatureObjectTemplate, humidityObjectTemplate);
            devices.put(alias, board);
            setDescription(getDescription() + " " + ipToQuery);
        }
    }

    /*
     * Sensor side
     */
    @Override
    public void onStart() {
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        // number of configured boards in manifest file
        BOARD_NUMBER = configuration.getTuples().size();
        setPollingWait(POLLING_TIME);
        loadBoards();
    }

    @Override
    public void onStop() {
        //release resources
        devices.clear();
        devices = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "TCW122B-CM"));
    }

    @Override
    protected void onRun() {
        Set keys = devices.keySet();
        Iterator keyIter = keys.iterator();
        while (keyIter.hasNext()) {
            String alias = (String) keyIter.next();
            Board board = (Board) devices.get(alias);
            evaluateDiff(board);
        }
        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(Tcw122bcm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    // }

    private void sendEvent(String objectAddress, String eventProperty, String eventValue, String objectTemplate) {
        ProtocolRead event = new ProtocolRead(this, "tcw122bcm", objectAddress);
        event.addProperty(eventProperty, eventValue);
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    // this method sends a Freedomotic event only if there is any change in input values or relays 
    // @param board  
    //        board object to evaluate
    // @return
    //        no value 
    private void evaluateDiff(Board board) {
        //String address = board.getIpAddress() + ":" + board.getPortNumber();
        //String address = board.getAlias();
        String value = null;
        MYSNMP snmpRequest = new MYSNMP();
        //temperature1
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + T1_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getTemperature1())) {
            sendEvent(address + ":T1", "sensor.temperature", value, board.getTemperatureObjectTemplate());
            board.setTemperature1(value);
        }
        //temperature2
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + T2_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getTemperature2())) {
            sendEvent(address + ":T2", "sensor.temperature", value, board.getTemperatureObjectTemplate());
            board.setTemperature2(value);
        }
        //humidity1
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + H1_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getHumidity1())) {
            sendEvent(address + ":H1", "sensor.humidity", value, board.getHumidityObjectTemplate());
            board.setHumidity1(value);
        }
        //humidity2
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + H2_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getHumidity2())) {
            sendEvent(address + ":H2", "sensor.humidity", value, board.getHumidityObjectTemplate());
            board.setHumidity2(value);
        }
        //digital input 1
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + D1_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getDigitalInput1())) {
            sendEvent(address + ":D1", "digital.input.value", value, "default");
            board.setDigitalInput1(value);
        }
        //digital input 2
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + D2_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getDigitalInput2())) {
            sendEvent(address + ":D2", "digital.input.value", value, "default");
            board.setDigitalInput2(value);
        }
        //analog input 1
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + A1_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getAnalogInput1())) {
            sendEvent(address + ":A1", "analog.input.value", value, "default");
            board.setAnalogInput1(value);
        }
        //analog input 2
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + A2_VALUE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getAnalogInput2())) {
            sendEvent(address + ":A2", "analog.input.value", value, "default");
            board.setAnalogInput2(value);
        }
        //relay1 state
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + R1_STATE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getRelay1())) {
            sendEvent(address + ":R1", "relay.state", value, board.getRelayObjectTemplate());
            board.setRelay1(value);
        }
        //relay2 state
        value = snmpRequest.SNMP_GET(board.getIpAddress(), SNMP_PORT, SNMP_OID + "." + R2_STATE, SNMP_COMMUNITY);
        if (!value.equalsIgnoreCase(board.getRelay2())) {
            sendEvent(address + ":R2", "relay.state", value, board.getRelayObjectTemplate());
            board.setRelay2(value);
        }
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
        String delimiter = configuration.getProperty("address-delimiter");
        Integer control = 0;
        address = c.getProperty("address").split(delimiter);
        //retrieve the board handle from alias key 
        Board board = (Board) devices.get(address[0]);
        String hostname = board.getIpAddress();
        int hostport = board.getPortNumber();
        String relay = address[1].toLowerCase();
        //convert control 
        if (c.getProperty("control").equalsIgnoreCase("ON")) {
            control = 1;
        }
        if (c.getProperty("control").equalsIgnoreCase("OFF")) {
            control = 0;
        }
        // call the method for changing relay status
        changeRelayStatus(hostname, hostport, relay, control);
    }

    // This method changes the relay status using http commands
    // If set in the configuration file it uses the http authentication 
    // @param hostname  
    //        TCW122B-CM ip address
    // @param hostport  
    //        TCW122B-CM tcp port (default value 80)
    // @param relayNumber  
    //        relay number - allowed values 1,2
    // @param control  
    //        action to perform - allowed value 0 (off), 1 (on)
    // @return
    //        no value 
    private void changeRelayStatus(String hostname, int hostport, String relayNumber, int control) {
        try {
            String authString = USERNAME + ":" + PASSWORD;
            //System.out.println("auth string: " + authString);
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            //System.out.println("Base64 encoded auth string: " + authStringEnc); //FOR DEBUG
            //Create a URL for the desired  page 
            URL url = new URL("http://" + hostname + ":" + hostport + "/?" + relayNumber + "=" + control);
            LOG.info("Freedomotic sends the command " + url);
            URLConnection urlConnection = url.openConnection();
            // if required set the authentication
            if (HTTP_AUTHENTICATION.equalsIgnoreCase("true")) {
                urlConnection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            }
            InputStream is = urlConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            int numCharsRead;
            char[] charArray = new char[1024];
            StringBuffer sb = new StringBuffer();
            while ((numCharsRead = isr.read(charArray)) > 0) {
                sb.append(charArray, 0, numCharsRead);
            }
            String result = sb.toString();
        } catch (MalformedURLException e) {
            LOG.severe("Change relay status malformed URL " + e.toString());
        } catch (IOException e) {
            LOG.severe("Change relay status IOexception" + e.toString());
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

    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
