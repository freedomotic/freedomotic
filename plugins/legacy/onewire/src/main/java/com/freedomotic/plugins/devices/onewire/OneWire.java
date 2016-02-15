/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.plugins.devices.onewire;

import com.dalsemi.onewire.OneWireAccessProvider;
import com.dalsemi.onewire.OneWireException;
import com.dalsemi.onewire.adapter.DSPortAdapter;
import com.dalsemi.onewire.adapter.OneWireIOException;
import com.dalsemi.onewire.container.OneWireContainer;
import com.dalsemi.onewire.container.TemperatureContainer;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.w3c.dom.DOMException;

/**
 *
 * @author ciro.barbone
 */

public class OneWire extends Protocol {
    //private static devices;
    private static final Logger LOG = Logger.getLogger(OneWire.class.getName());
    private static ArrayList<PortAdapter> portAdapters = null;
    //private static DSPortAdapter[] dsDevice;
    private static PortAdapter portAdattatore = null;
    private static int DEVICES_NUMBER = 1;
    private int POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
    //private int DEVICE = configuration.getIntProperty("address-devices", 0);
    boolean usedefault = false;
    String adapter_name = null;
    String port_name = null;
    final int POLLING_WAIT;
    private byte[] state;
    //private PortAdapter portAdapter;

    public OneWire() {
        //every plugin needs a name and a manifest XML file
        super("onewire", "/onewire/onewire-manifest.xml");
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

        loadDevicesAndConnect();
        String adapterName;
        String portName;
        //connect(0);
        //connect(1);
        //adapterName = "{DS9490}";
        //portName = "USB1";
        //portAdattatore = new PortAdapter(adapterName, portName, 0);



    }

    static int parseInt(BufferedReader in, int def) {
        try {
            return Integer.parseInt(in.readLine());
        } catch (Exception e) {
            return def;
        }
    }

    private void loadDevicesAndConnect() {

        if (portAdapters == null) {
            portAdapters = new ArrayList<PortAdapter>();
        }

        setDescription("Reading status changes from"); //empty description

        for (int i = 0; i < DEVICES_NUMBER; i++) {
            String adapterName;
            String portName;

            double value;
            adapterName = configuration.getTuples().getStringProperty(i, "adapter-name", "{DS9490}");
            portName = configuration.getTuples().getStringProperty(i, "port-name", "USB1");
            value = configuration.getTuples().getDoubleProperty(i, "value", 0.0);

            PortAdapter portAdapter = new PortAdapter(adapterName, portName, value);

            //portAdapter.connect();
            portAdapters.add(portAdapter);


            setDescription(getDescription() + " " + adapterName + ":" + portName + ":" + value + ";");

        }
    }

    /**
     * Connection to 1wire don't used yet
     */
    private boolean connect(String adapterName, String portName) {

        return true;
    }

    /**
     * Disconnect OneWire
     */
    private void disconnect() {
        // close streams and socket
        LOG.info("OnwWire disconnect");

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
        Logger.getLogger(OneWire.class.getName()).log(Level.SEVERE, null, "onewire onRun ");
        /* try
         {
         portAdattatore.checkDeviceListAndEvaluateDiffs();
         }
         catch (Exception e)
         {
         Logger.getLogger(onewire.class.getName()).log(Level.SEVERE, null, e);
         }*/


        for (PortAdapter portAdapter : portAdapters) {
            //evaluateDiffs(portAdapter); //parses the xml and crosscheck the data with the previous read
            //Logger.getLogger(onewire.class.getName()).log(Level.SEVERE, null, "dentro adapter onRun ");
            try {
                Logger.getLogger(OneWire.class.getName()).log(Level.SEVERE, null, "checkDeviceListAndEvaluateDiffs ");

                double value;
                if (portAdapter != null) {
                    LOG.info("OneWire onRun() logs this message every "
                            + "POLLINGWAIT=" + "milliseconds");

                    if (portAdapter.checkDeviceListAndEvaluateDiffs() == true) { // temp device is changed
                        if (portAdapter.devicesOneWire != null) {
                            for (DeviceOneWire device : portAdapter.devicesOneWire) {
                                if (device.getChanged()) {
                                    //building the event
                                    ProtocolRead event = new ProtocolRead(this, "onewire", portAdapter.getAdapterName() + ":" + portAdapter.getPortName() + ":" + device.getAddress());
                                    //adding some optional information to the event
                                    event.addProperty("sensor.unit", "C");
                                    event.addProperty("sensor.value", Double.toString(device.getValue()));
                                    event.addProperty("sensor.name", "");
                                    event.addProperty("sensor.state", "Alarm");
                                    Freedomotic.logger.warning("Object " + device.getAddress() + " is change. New Value " + Double.toString(device.getValue()));
                                    this.notifyEvent(event);
                                    device.setChanged(false);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(OneWire.class.getName()).log(Level.SEVERE, null, e);
            }

        }
        try {
            Thread.sleep(POLLING_TIME);
        } catch (InterruptedException ex) {
            Logger.getLogger(OneWire.class.getName()).log(Level.SEVERE, null, ex);
        }

        //at the end of this method the system waits POLLINGTIME 
        //before calling it again. The result is this log message is printed
        //every 2 seconds (2000 millisecs)


    }

    private void evaluateDiffs(PortAdapter portAdapter) {
        if (portAdapter != null) {
            LOG.info("OneWire onRun() logs this message every "
                    + "POLLINGWAIT=" + POLLING_WAIT + "milliseconds");
            //boolean statusDigitalInput;
            //int statusAnalogInput;
            try {
                // Open Interface K8055 
            /*
                 jk8055 = JK8055.getInstance();
                 jk8055.OpenDevice(DEVICE);
            
                 int startingValue=board.getStartingValue();
                 int linesNumber=board.getDigitalInputNumber();
            
                 // Read all digital input
                 for (int i = startingValue; i <= linesNumber; i++) {
                 statusDigitalInput= jk8055.ReadDigitalChannel(i);
                 //Freedomotic.logger.severe("k8055 status line " + Boolean.toString(status)); 
                 //Freedomotic.logger.info("k8055 change digital Input "+ i + " value: " + statusDigitalInput);
            
                 if (statusDigitalInput!=board.getDigitalValue(i-1)) {
                 Freedomotic.logger.info("k8055 change digital Input ");
                 board.setDigitalValue(i-1, statusDigitalInput);
                 if (statusDigitalInput==true)  {
                 sendChanges(i, DEVICE ,"ID" ,"1");
                 }
                 else {
                 sendChanges(i, DEVICE ,"ID","0");
                 }
                 }
                 }   
            
                 linesNumber=board.getAnalogInputNumber();
            
                 // Read all Analog input
                 for (int i = startingValue; i <= linesNumber; i++) {
                 statusAnalogInput= jk8055.ReadAnalogChannel(i);
                 //Freedomotic.logger.severe("k8055 status line " + Boolean.toString(status)); 
                 if (statusAnalogInput!=board.getAnalogValue(i-1)){
                 Freedomotic.logger.info("k8055 change analog Input "+ i + " value: " + statusAnalogInput);
                 board.setAnalogValue(i-1,statusAnalogInput);
                 sendChanges(i,DEVICE,"IA",Integer.toString(statusAnalogInput));
                 }
                 }
                 jk8055.CloseDevice();
                 * 
                 */
            } catch (DOMException dOMException) {
                //do nothing
            } catch (NumberFormatException numberFormatException) {
                //do nothing
            } catch (NullPointerException ex) {
                //do nothing
            }

        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        LOG.info("onewire plugin receives a command called " + c.getName()
                + " with parameters " + c.getProperties().toString());

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
        //Freedomotic.logger.severe("k8055 address " + address);
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
}
