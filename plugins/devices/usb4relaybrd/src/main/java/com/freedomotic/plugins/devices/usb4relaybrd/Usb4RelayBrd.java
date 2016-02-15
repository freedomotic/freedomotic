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

package com.freedomotic.plugins.devices.usb4relaybrd;

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

public class Usb4RelayBrd extends Protocol {

    private static final Logger LOG = Logger.getLogger(Usb4RelayBrd.class.getName()); 
    final int POLLING_WAIT;
    private static Map<String, FTDevice> boards;
    private int relayStatus = 0;
    private int relayBitStatus[] = {-1, -1, -1, -1};
    List<FTDevice> fTDevices;
    Map<String, String> boardsAddressAlias = new HashMap<String, String>();

    public Usb4RelayBrd() {
        //every plugin needs a name and a manifest XML file
        super("USB4RelayBrd", "/usb4relaybrd/usb4relaybrd-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 1000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
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
        int result = 0;
        String relayStatusValue = null;
        //at the end of this method the system waits POLLINGTIME 
        //before calling it again. The result is this log message is printed
        //every 2 seconds (2000 millisecs)
        for (FTDevice fTDevice : fTDevices) {
            result = readFromFTDevice(fTDevice);
            //System.out.println("Received status " + result); //FOR DEBUG
            // if relays status changed
            if (!(result == relayStatus)) {
                //System.out.println("Relay status changed"); //FOR DEBUG
                relayStatus = result; // update the stored relays status
                if ((result & 2) == 0) {
                    //System.out.println("Relay 1 is OFF"); //FOR DEBUG
                    relayBitStatus[0] = 0;
                    sendEvent("R1", "relay.status", "0");
                } else {
                    //System.out.println("Relay 1 is ON"); //FOR DEBUG
                    relayBitStatus[0] = 1;
                    sendEvent("R1", "relay.status", "1");
                }
                if ((result & 8) == 0) {
                    //System.out.println("Relay 2 is OFF"); //FOR DEBUG
                    relayBitStatus[1] = 0;
                    sendEvent("R2", "relay.status", "0");
                } else {
                    //System.out.println("Relay 2 is ON"); //FOR DEBUG
                    relayBitStatus[1] = 1;
                    sendEvent("R2", "relay.status", "1");
                }
                if ((result & 32) == 0) {
                    //System.out.println("Relay 3 is OFF"); // FOR DEBUG
                    relayBitStatus[2] = 0;
                    sendEvent("R3", "relay.status", "0");
                } else {
                    //System.out.println("Relay 3 is ON"); // FOR DEBUG
                    relayBitStatus[2] = 1;
                    sendEvent("R3", "relay.status", "1");
                }
                if ((result & 128) == 0) {
                    //System.out.println("Relay 4 is OFF"); //FOR DEBUG
                    relayBitStatus[3] = 0;
                    sendEvent("R4", "relay.status", "0");
                } else {
                    //System.out.println("Relay 4 is ON"); //FOR DEBUG
                    relayBitStatus[3] = 1;
                    sendEvent("R4", "relay.status", "1");
                }
            }
        }
    }

    @Override
    protected void onStart() {
        loadDevices();
        LOG.info("Usb4RelayBrd plugin started");
        setDescription("Usb4RelayBrd started");
    }

    @Override
    protected void onStop() {
        for (FTDevice fTDevice : fTDevices) {
            try {
                fTDevice.close();
            } catch (FTD2XXException ex) {
                LOG.severe("Usb4RelayBrd FTD2XX exception " + ex.toString());
            }
            // clear the boards list
            fTDevices = null;
            LOG.info("Usb4RelayBrd plugin stopped");
            setDescription("Usb4RelayBrd stopped");
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        Integer newStatus = 0;
        String binaryString = null;
        Integer address = 0;
        Integer pos = 0;
        char status;
        FTDevice usb4Relay = fTDevices.get(0);
        if (c.getProperty("control").equalsIgnoreCase("ALLON")) {
            usb4Relay.write(255); // switch ON all relays
            System.out.println("All Relays ON");
        } else if (c.getProperty("control").equalsIgnoreCase("ALLOFF")) {
            usb4Relay.write(0); // switch OFF all relays
            System.out.println("All Relays OFF");
        } else {
            // retrieve the relay number 
            address = Integer.valueOf(c.getProperty("address").substring(1, c.getProperty("address").length()));
            newStatus = readFromFTDevice(usb4Relay);
            // convert the integer relayStatus into a binary string 
            binaryString = Integer.toBinaryString(relayStatus);
            // Integer.toBinaryString removes leading 0 so the first is added if needed 
            if (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }
            //System.out.println("Stringa binaria " + binaryString);
            // revert the string 
            StringBuffer reverse = new StringBuffer(binaryString).reverse();
            if (c.getProperty("control").equalsIgnoreCase("ON")) {
                status = '1';
            } else {
                status = '0';
            }
            // detect from the relay number which bit must be changed 
            switch (address) {
                case 1:
                    pos = 1;
                    break;
                case 2:
                    pos = 3;
                    break;
                case 3:
                    pos = 5;
                    break;
                case 4:
                    pos = 7;
                    break;
            }
            // change the bit mapped to the relay
            reverse.setCharAt(pos, status);
            // revert the string for decimal convertion
            String reverseModified = new StringBuffer(reverse.toString()).reverse().toString();
            // write on the serial the new relays status 
            usb4Relay.write(Integer.parseInt(reverseModified, 2));
        }
        usb4Relay = null;
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

    // load devices connected
    private void loadDevices() {
        //boards = new HashMap(); // not used now
        try {
            fTDevices = FTDevice.getDevices();
            for (FTDevice fTDevice : fTDevices) {
                //boards.put(fTDevice.getDevSerialNumber(), fTDevice);
                // open the device
                fTDevice.open();
                // 170 represents in decimal the pins mask 1,3,5,7 
                int mask = 170;
                // set Asynchronous BitBangMode and output pins mask
                fTDevice.setBitMode((byte) mask, BitModes.BITMODE_ASYNC_BITBANG);
                // set baudate
                fTDevice.setBaudRate(921600);
                // set serial line parameters
                fTDevice.setDataCharacteristics(WordLength.BITS_8, StopBits.STOP_BITS_1, Parity.PARITY_NONE);
            }
        } catch (FTD2XXException ex) {
            LOG.severe("Usb4RelayBrd FTD2XX exception " + ex.toString());
        }
        if (fTDevices.size() == 0) {
            this.stop();
            setDescription("Usb4RelayBrd stopped. No board detected");
        }
    }

// this method sends a freedomotic event every time a relay status changes
    private void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, "usb4relaybrd", objectAddress);
        event.addProperty(eventProperty, eventValue);
        event.addProperty("object.class", "Light");
        event.addProperty("object.name", objectAddress);
        //publish the event on the messaging bus
        this.notifyEvent(event);
    }

    // this method reads a byte from an FTDevice and converts it to integer
    private int readFromFTDevice(FTDevice usb4Relay) {
        int readValue = 0;
        byte[] received = new byte[1];
        try {
            usb4Relay.purgeBuffer(true, true);
            usb4Relay.purgeBuffer(true, true);
            usb4Relay.read(received, 0, 1);
            readValue = (int) received[0] & 0xff; //mask the sign bit
        } catch (FTD2XXException ex) {
            LOG.severe("Usb4RelayBrd FTD2XX exception " + ex.toString());
        }
        return (readValue);
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
