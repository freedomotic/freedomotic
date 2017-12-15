/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
/**
 * This plugin uses JavaFTD2XX-0.2.5 library not available in Maven Central. You
 * can find it in this repository (third-party-libs folder).
 *
 * You need to add it to a local Maven repository with mvn install:install-file
 * -Dfile=JavaFTD2XX-0.2.5.jar -DgroupId=com.ftdi -DartifactId=java-ftdi
 * -Dversion=0.2.5 -Dpackaging=jar
 *
 * @author Mauro Cicolella
 */
package com.freedomotic.plugins.devices.usb4relaybrd;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import com.ftdi.*;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Usb4RelayBrd extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(Usb4RelayBrd.class.getName());
    private final int POLLING_WAIT;
    private static Map<String, FTDevice> boards;
    private int relayStatus = 0;
    private int relayBitStatus[] = {-1, -1, -1, -1};
    private List<FTDevice> fTDevices;

    public Usb4RelayBrd() {
        super("USB4RelayBrd", "/usb4relaybrd/usb4relaybrd-manifest.xml");
        POLLING_WAIT = configuration.getIntProperty("polling-time", 1000);
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
        // no GUI available
    }

    @Override
    protected void onHideGui() {
        // no GUI available
    }

    @Override
    protected void onRun() {
        int result;
        if (fTDevices != null) {
            for (FTDevice fTDevice : fTDevices) {
                result = readFromFTDevice(fTDevice);
                LOG.debug("Received status {}", result);
                // if relays status changed
                if (result != relayStatus) {
                    LOG.debug("Relay status changed");
                    relayStatus = result; // update the stored relays status
                    if ((result & 2) == 0) {
                        LOG.debug("Relay 1 is OFF");
                        relayBitStatus[0] = 0;
                        sendEvent("R1", "relay.status", "0");
                    } else {
                        LOG.debug("Relay 1 is ON");
                        relayBitStatus[0] = 1;
                        sendEvent("R1", "relay.status", "1");
                    }
                    if ((result & 8) == 0) {
                        LOG.debug("Relay 2 is OFF");
                        relayBitStatus[1] = 0;
                        sendEvent("R2", "relay.status", "0");
                    } else {
                        LOG.debug("Relay 2 is ON");
                        relayBitStatus[1] = 1;
                        sendEvent("R2", "relay.status", "1");
                    }
                    if ((result & 32) == 0) {
                        LOG.debug("Relay 3 is OFF");
                        relayBitStatus[2] = 0;
                        sendEvent("R3", "relay.status", "0");
                    } else {
                        LOG.debug("Relay 3 is ON");
                        relayBitStatus[2] = 1;
                        sendEvent("R3", "relay.status", "1");
                    }
                    if ((result & 128) == 0) {
                        LOG.debug("Relay 4 is OFF");
                        relayBitStatus[3] = 0;
                        sendEvent("R4", "relay.status", "0");
                    } else {
                        LOG.debug("Relay 4 is ON");
                        relayBitStatus[3] = 1;
                        sendEvent("R4", "relay.status", "1");
                    }
                }
            }
        }
    }

    @Override
    protected void onStart() throws PluginStartupException {
        if (loadDevices() == 0) {
            throw new PluginStartupException("No board available");
        } else {
            LOG.info("Usb4RelayBrd plugin started");
        }
        setDescription("Usb4RelayBrd started");
    }

    @Override
    protected void onStop() {
        setPollingWait(-1);
        for (FTDevice fTDevice : fTDevices) {
            try {
                fTDevice.close();
            } catch (FTD2XXException ex) {
                LOG.error("Usb4RelayBrd FTD2XX exception {}", ex.toString());
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
            LOG.debug("All Relays ON");
        } else if (c.getProperty("control").equalsIgnoreCase("ALLOFF")) {
            usb4Relay.write(0); // switch OFF all relays
            LOG.debug("All Relays OFF");
        } else {
            // retrieve the relay number 
            address = Integer.valueOf(c.getProperty("address").substring(1, c.getProperty("address").length()));
            newStatus = readFromFTDevice(usb4Relay);
            // convert the integer relayStatus into a binary string 
            binaryString = Integer.toBinaryString(relayStatus);
            if (binaryString.length() < 8) {
                binaryString = "0" + binaryString;
            }
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
            String reverseModified = new StringBuilder(reverse.toString()).reverse().toString();
            // write on the serial the new relays status 
            usb4Relay.write(Integer.parseInt(reverseModified, 2));
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

    /**
     * Loads all connected devices.
     *
     */
    private int loadDevices() {
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
            LOG.error("Usb4RelayBrd FTD2XX exception {}", ex.toString());
        }
        return fTDevices.size();
    }

    /**
     * Sends a Freedomotic event every time a relay status changes.
     *
     * @param objectAddress
     * @param eventProperty
     * @param eventValue
     */
    private void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, "usb4relaybrd", objectAddress);
        event.addProperty(eventProperty, eventValue);
        event.addProperty("object.class", "Light");
        event.addProperty("object.name", objectAddress);
        //publish the event on the messaging bus
        notifyEvent(event);
    }

    /**
     * Reads a byte from an FTDevice and converts it to integer.
     *
     * @param usb4Relay FTdevice to read from
     * @return an integer representation of read byte
     */
    private int readFromFTDevice(FTDevice usb4Relay) {
        int readValue = 0;
        byte[] received = new byte[1];
        try {
            usb4Relay.purgeBuffer(true, true);
            usb4Relay.purgeBuffer(true, true);
            usb4Relay.read(received, 0, 1);
            readValue = (int) received[0] & 0xff; //mask the sign bit
        } catch (FTD2XXException ex) {
            LOG.error("Usb4RelayBrd FTD2XX exception {}", ex.toString());
        }
        return (readValue);
    }
}
