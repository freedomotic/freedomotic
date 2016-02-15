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

package com.freedomotic.plugins.devices.kmtronicusbrelay;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.ds.Tuples;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.ftdi.*;
import com.freedomotic.events.ProtocolRead;
import java.util.logging.Logger;

public class KMTronicUsbRelay extends Protocol {

    private static final Logger LOG = Logger.getLogger(KMTronicUsbRelay.class.getName()); 
    final int POLLING_WAIT;
    //final int RELAY_NUMBER;
    private static Map<String, FTDevice> boards;
    //private int relayStatus = 0;
    private int relayBitStatus[] = null;
    private static int[] ONE_CHANNEL_STATUS_REQUEST = {255, 1, 3};
    private static int[] TWO_CHANNEL_STATUS_REQUEST = {255, 2, 3};
    private static int[] MORE_CHANNEL_STATUS_REQUEST = {255, 9, 0};
    List<FTDevice> fTDevices;
    Map<String, String> boardsAddressAlias = new HashMap<String, String>();
    private int RELAY_NUMBER = configuration.getIntProperty("relay-number", 1);

    public KMTronicUsbRelay() {
        //every plugin needs a name and a manifest XML file
        super("KMTronicUsbRelay", "/kmtronicusbrelay/kmtronicusbrelay-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 1000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
        //RELAY_NUMBER = configuration.getIntProperty("relay-number", 1);

        // load the list of boards address alias
        boardsAddressAlias = configuration.getTuples().getTuple(0);
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

    @Override
    protected void onRun() {
        byte[] result = null;
        for (FTDevice fTDevice : fTDevices) {
            if (RELAY_NUMBER == 1) {
                result = new byte[3];
                result = readRelayStatus(fTDevice, ONE_CHANNEL_STATUS_REQUEST, RELAY_NUMBER);
                sendEvent((int) result[1], "relay.status", (int) result[2]);
            }
            if (RELAY_NUMBER == 2) {
                result = new byte[3];
                //send request for the first relay 
                result = readRelayStatus(fTDevice, ONE_CHANNEL_STATUS_REQUEST, RELAY_NUMBER);
                sendEvent((int) result[1], "relay.status", (int) result[2]);
                //send request for the second relay
                result = readRelayStatus(fTDevice, TWO_CHANNEL_STATUS_REQUEST, RELAY_NUMBER);
                sendEvent((int) result[1], "relay.status", (int) result[2]);
            }
            if (RELAY_NUMBER == 4) {
                result = new byte[4];
                //send request for the first relay 
                result = readRelayStatus(fTDevice, MORE_CHANNEL_STATUS_REQUEST, RELAY_NUMBER);
                for (int i = 0; i < RELAY_NUMBER; i++) {
                    sendEvent(i + 1, "relay.status", (int) result[i]);
                }
            }
            if (RELAY_NUMBER == 8) {
                result = new byte[8];
                //send request for the first relay 
                result = readRelayStatus(fTDevice, MORE_CHANNEL_STATUS_REQUEST, RELAY_NUMBER);
                for (int i = 0; i < RELAY_NUMBER; i++) {
                    sendEvent(i + 1, "relay.status", (int) result[i]);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        initializeRelaysBitStatus(RELAY_NUMBER);
        loadDevices();
        LOG.info("KMTronicUsbRelay plugin started");
        setDescription("KMTronicUsbRelay started");
    }

    @Override
    protected void onStop() {
        for (FTDevice fTDevice : fTDevices) {
            try {
                fTDevice.close();
            } catch (FTD2XXException ex) {
                LOG.severe("KMTronicUsbRelay FTD2XX exception " + ex.toString());
            }
            // clear the boards list
            fTDevices = null;
            LOG.info("KMTronicUsbRelay plugin stopped");
            setDescription("KMTronicUsbRelay stopped");
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        int startCommand = 255;
        int address = 0;
        int status = 0;
        byte[] command = new byte[3];
        FTDevice kmtronic = fTDevices.get(0);
        // retrieve the relay number 
        address = Integer.valueOf(c.getProperty("address"));
        // convert relay status
        if (c.getProperty("control").equalsIgnoreCase("ON")) {
            status = 1;
        } else {
            status = 0;
        }
        command[0] = (byte) startCommand;
        command[1] = (byte) address;
        command[2] = (byte) status;
        kmtronic.write(command);
        kmtronic = null;
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

    private void initializeRelaysBitStatus(int relayNumber) {
        relayBitStatus = new int[relayNumber];
        for (int i = 0; i < relayNumber; i++) {
            relayBitStatus[i] = -1;
        }
    }

    // load devices connected
    private void loadDevices() {
        //boards = new HashMap(); // not used now
        try {
            fTDevices = FTDevice.getDevices();
            for (FTDevice fTDevice : fTDevices) {
                //boards.put(fTDevice.getDevSerialNumber(), fTDevice);
                // open the device
                fTDevice.open();
                // set baudate
                fTDevice.setBaudRate(9600);
                // set serial line parameters
                fTDevice.setDataCharacteristics(WordLength.BITS_8, StopBits.STOP_BITS_1, Parity.PARITY_NONE);
            }
        } catch (FTD2XXException ex) {
            LOG.severe("KMTronicUsbRelay FTD2XX exception " + ex.toString());
        }
        if (fTDevices.size() == 0) {
            this.stop();
            setDescription("KMTronicUsbRelay stopped. No board detected");
        }
    }

// this method sends a freedomotic event every time a relay status changes
    private void sendEvent(int objectAddress, String eventProperty, int eventValue) {
        if (relayBitStatus[objectAddress - 1] != eventValue) {
            relayBitStatus[objectAddress - 1] = eventValue;
            ProtocolRead event = new ProtocolRead(this, "kmtronicusbrelay", String.valueOf(objectAddress));
            event.addProperty(eventProperty, String.valueOf(eventValue));
            event.addProperty("object.class", "Light");
            event.addProperty("object.name", String.valueOf(objectAddress));
            //publish the event on the messaging bus
            this.notifyEvent(event);
        }
    }

    // this method reads the relays status
    private byte[] readRelayStatus(FTDevice kmtronic, int[] requestArray, int relayNumber) {
        byte[] received = null;
        byte[] request = new byte[3];
        // define received array dimension
        if (relayNumber < 4) {
            received = new byte[3];
        }
        if (relayNumber == 4) {
            received = new byte[4];
        }
        if (relayNumber == 8) {
            received = new byte[8];
        }
        try {
            kmtronic.purgeBuffer(true, true);
            kmtronic.purgeBuffer(true, true);
            for (int i = 0; i < requestArray.length; i++) {
                request[i] = (byte) requestArray[i];
            }
            kmtronic.write(request);
            kmtronic.read(received);
        } catch (FTD2XXException ex) {
            LOG.severe("KMTronicUsbRelay FTD2XX exception " + ex.toString());
        }
        return (received);
    }

    // this method retrieve a key from value in a hashmap
    // NOT USED
    public static Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }
}