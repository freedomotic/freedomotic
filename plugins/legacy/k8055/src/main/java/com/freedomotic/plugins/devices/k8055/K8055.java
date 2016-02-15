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
package com.freedomotic.plugins.devices.k8055;

/**
 *
 * @author barbone
 */
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import net.sf.libk8055.jk8055.JK8055;
import net.sf.libk8055.jk8055.JK8055Exception;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.DOMException;

public class K8055 extends Protocol {

    private static final Logger LOG = Logger.getLogger(K8055.class.getName());
    private static ArrayList<Board> boards = null;
    private static int BOARD_NUMBER = 1;
    private int POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
    private int DEVICE = configuration.getIntProperty("address-devices", 0);
    private JK8055 jk8055;
    final int POLLING_WAIT;

    public K8055() {
        //every plugin needs a name and a manifest XML file
        super("k8055", "/k8055/k8055-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/com.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
        /**
         * uncomment the line below to add a GUI to this plugin the GUI can be
         * started with a right-click on plugin list on the desktop frontend
         * (com.freedomotic.jfrontend plugin)
         */
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        //connect(DEVICE);
        loadBoards();
    }

    private void loadBoards() {
        if (boards == null) {
            boards = new ArrayList<Board>();
        }
        setDescription("Reading status changes from");
        for (int i = 0; i < BOARD_NUMBER; i++) {
            String deviceToQuery;
            int digitalOutputNumber;
            int analogOutputNumber;
            int analogInputNumber;
            int digitalInputNumber;
            int startingValue;
            deviceToQuery = configuration.getTuples().getStringProperty(i, "deviceToQuery", "0");
            digitalOutputNumber = configuration.getTuples().getIntProperty(i, "digital-output-number", 8);
            analogOutputNumber = configuration.getTuples().getIntProperty(i, "analog-output-number", 2);
            analogInputNumber = configuration.getTuples().getIntProperty(i, "analog-input-number", 2);
            digitalInputNumber = configuration.getTuples().getIntProperty(i, "digital-input-number", 5);
            startingValue = configuration.getTuples().getIntProperty(i, "starting-value", 1);
            Board board = new Board(deviceToQuery, digitalOutputNumber, analogOutputNumber, analogInputNumber,
                    digitalInputNumber, startingValue);
            boards.add(board);
            setDescription(getDescription() + " " + deviceToQuery + ";");
        }
    }

    /**
     * Connection to K8055 don't used yet
     */
    private boolean connect(int addressdevice) {

        LOG.info("Trying to connect to k8055 board on address-device " + addressdevice);
        try {
            jk8055 = JK8055.getInstance();
            jk8055.OpenDevice(addressdevice);
            //jk8055.ClearAllDigital();

            return true;
        } catch (JK8055Exception ex) {
            Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, null, ex);
            LOG.severe("Unable to connect to device on address-device " + addressdevice);
            return false;
        }
    }

    /**
     * Disconnect K8055
     */
    private void disconnect() {
        // close streams and socket
        LOG.info("k8055 disconnect");
        try {
            jk8055.CloseDevice();

        } catch (JK8055Exception ex) {
            Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, null, ex);
            LOG.severe("Problem to disconnect device k8055");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        //boards.clear();
        //boards = null;
        disconnect();
        setPollingWait(-1); //disable polling
        //display the default description
        //setDescription(configuration.getStringProperty("description", "Ipx800"));
    }

    @Override
    protected void onRun() {
        for (Board board : boards) {
            evaluateDiffs(board); //parses the xml and crosscheck the data with the previous read
            try {
                Thread.sleep(POLLING_TIME);
            } catch (InterruptedException ex) {
                Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        //at the end of this method the system waits POLLINGTIME 
        //before calling it again. The result is this log message is printed
        //every 2 seconds (2000 millisecs)


    }

    private void evaluateDiffs(Board board) {
        if (board != null) {
            LOG.info("k8055 onRun() logs this message every "
                    + "POLLINGWAIT=" + POLLING_WAIT + "milliseconds");
            boolean statusDigitalInput;
            int statusAnalogInput;
            try {

                // Open Interface K8055 
                jk8055 = JK8055.getInstance();
                jk8055.OpenDevice(DEVICE);

                int startingValue = board.getStartingValue();
                int linesNumber = board.getDigitalInputNumber();

                // Read all digital input
                for (int i = startingValue; i <= linesNumber; i++) {
                    statusDigitalInput = jk8055.ReadDigitalChannel(i);
                    //LOG.severe("k8055 status line " + Boolean.toString(status)); 
                    //LOG.info("k8055 change digital Input "+ i + " value: " + statusDigitalInput);

                    if (statusDigitalInput != board.getDigitalValue(i - 1)) {
                        LOG.info("k8055 change digital Input ");
                        board.setDigitalValue(i - 1, statusDigitalInput);
                        if (statusDigitalInput == true) {
                            sendChanges(i, DEVICE, "ID", "1");
                        } else {
                            sendChanges(i, DEVICE, "ID", "0");
                        }
                    }
                }

                linesNumber = board.getAnalogInputNumber();

                // Read all Analog input
                for (int i = startingValue; i <= linesNumber; i++) {
                    statusAnalogInput = jk8055.ReadAnalogChannel(i);
                    //LOG.severe("k8055 status line " + Boolean.toString(status)); 
                    if (statusAnalogInput != board.getAnalogValue(i - 1)) {
                        LOG.info("k8055 change analog Input " + i + " value: " + statusAnalogInput);
                        board.setAnalogValue(i - 1, statusAnalogInput);
                        sendChanges(i, DEVICE, "IA", Integer.toString(statusAnalogInput));
                    }
                }
                jk8055.CloseDevice();
            } catch (DOMException dOMException) {
                //do nothing
            } catch (NumberFormatException numberFormatException) {
                //do nothing
            } catch (NullPointerException ex) {
                //do nothing
            } catch (JK8055Exception ex) {
                Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, "Exception k8055", ex);
            }

        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        LOG.info("k8055 plugin receives a command called " + c.getName()
                + " with parameters " + c.getProperties().toString());
        String[] address = null;
        int brightness = 0;
        if (c.getProperty("command").equals("RELAY")) {

            String delimiter = configuration.getProperty("address-delimiter");
            address = c.getProperty("address").split(delimiter);

            if (c.getProperty("behavior").equals("on")) {
                setLineDevice(Integer.parseInt(address[2]), address[1], 100);
            } else {
                resetLineDevice(Integer.parseInt(address[2]), address[1], 100);
            }

        }

    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void sendChanges(int relayLine, int device, String typeLine, String status) {
        //first parameter in the constructor is the reference for the source of the event (typically the sensor plugin class)
        //second parameter is the protocol of the object we want to change
        //third parameter must be the exact address of the object we want to change
        String address = Integer.toString(device) + ":" + typeLine + ":" + relayLine;
        ProtocolRead event = new ProtocolRead(this, "k8055", address);
        //LOG.severe("k8055 address " + address);
        //add a property that defines the status readed from hardware
        //event.addProperty("relay.number", new Integer(relayLine).toString());
        if (typeLine.equals("ID")) {
            if (status.equals("0")) {
                event.addProperty("isOn", "false");
            } else {
                event.addProperty("isOn", "true");
            }
        } else if (typeLine.equals("IA")) {
            if (status.equals("0")) {
                event.addProperty("isOn", "false");
                event.addProperty("valueLine", status);
            } else {
                event.addProperty("isOn", "true");
                event.addProperty("valueLine", status);
            }
        }

        //others additional optional info
        //event.addProperty("status", status);
        //event.addProperty("boardPort", new Integer(device).toString());
        //event.addProperty("relayLine", new Integer(relayLine).toString());
        //publish the event on the messaging bus


        this.notifyEvent(event);
    }

    private boolean setLineDevice(int line, String typeLine, int currentLine) {
        try {
            jk8055 = JK8055.getInstance();
            jk8055.OpenDevice(DEVICE);
            LOG.info("k8055 plugin setLineDevice line:" + Integer.toString(line) + " typeLine: " + typeLine + " currentLine:" + currentLine);
            if (typeLine.equals("OD")) {
                jk8055.SetDigitalChannel(line);
            } else if (typeLine.equals("OA")) {
                jk8055.SetAnalogChannel(line);
            }
            jk8055.CloseDevice();
        } catch (DOMException dOMException) {
            //do nothing
            return false;
        } catch (NumberFormatException numberFormatException) {
            //do nothing
            return false;
        } catch (NullPointerException ex) {
            //do nothing
            return false;
        } catch (JK8055Exception ex) {
            Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, "Exception k8055", ex);
            return false;
        }
        return true;
    }

    private boolean resetLineDevice(int line, String typeLine, int brightness) {
        try {
            jk8055 = JK8055.getInstance();
            jk8055.OpenDevice(DEVICE);
            if (typeLine.equals("OD")) {
                jk8055.ClearDigitalChannel(line);
            } else if (typeLine.equals("OA")) {
                jk8055.ClearAnalogChannel(line);
            }
            jk8055.CloseDevice();
        } catch (DOMException dOMException) {
            //do nothing
            return false;
        } catch (NumberFormatException numberFormatException) {
            //do nothing
            return false;
        } catch (NullPointerException ex) {
            //do nothing
            return false;
        } catch (JK8055Exception ex) {
            Logger.getLogger(K8055.class.getName()).log(Level.SEVERE, "Exception k8055", ex);
            return false;
        }
        return true;
    }
}
