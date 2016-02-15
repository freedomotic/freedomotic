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


package com.freedomotic.plugins.devices.x10;

import com.freedomotic.plugins.devices.x10.gateways.PMix35Gateway;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author roby
 */
public class X10 extends Protocol {

    ArrayList<X10AbstractGateway> gateways = new ArrayList<X10AbstractGateway>();

    public X10() {
        super("X10", "/x10/x10-manifest.xml");
    }

    @Override
    public boolean canExecute(Command c) {
        return false;
    }

    /* this plugin can accept this values:
     * address: in form of X10 address (HOUSECODE+UNITCODE)of the target device. eg: P01, P02, A01, A02, ...
     * HOUSECODE is a letter from A to P
     * UNITCODE is a numner from 01 to 16
     * X10 supported commands:
     *      HOUSECODE+ON. eg: PON
     *      HOUSECODE+OFF eg: POFF
     *      HOUSECODE+BGT: 4% brighter
     *      HOUSECODE+DIM: 4% darker
     * A complete message to the device will be
     * A01A01AONAON (X10 requires double repetition of address and commans)
     */
    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        String housecode = c.getProperty("x10.address").substring(0, 1);
        String address = c.getProperty("x10.address").substring(1, 3);
        String command = c.getProperty("x10.function");
        X10AbstractGateway dev = getGateway();
        //if we have to set brightness we turn of the light and increase brightness one step at time
        //this is done because X10 protocol its hard to synch as it haven't good status request features
        if (command.equalsIgnoreCase("BGT")) {
            int value = Integer.parseInt(c.getProperty("x10.brightness.value"));
            int loops = value / 5;
            System.out.println("set brightness " + value + " in " + loops + " steps");
            dev.send(dev.composeMessage(housecode, address, "OFF"));
            for (int i = 0; i < loops; i++) {
                dev.send(dev.composeMessage(housecode, address, "BGT"));
            }
        } else {
            dev.send(dev.composeMessage(housecode, address, command));
        }
    }

    private X10AbstractGateway getGateway() {
        for (X10AbstractGateway gateway : gateways) {
            if (gateway.getName().equalsIgnoreCase(configuration.getStringProperty("gateway.name", "PMIX35"))) {
                return gateway;
            }
        }
        return null;
    }

    @Override
    public void onStart() {
        //create tha gateways instances as defined in configuration using tuples
        X10AbstractGateway gateway = new PMix35Gateway(this);
        gateways.add(gateway);
        this.setPollingWait(3000);
    }

    @Override
    public void onStop() {
        for (X10AbstractGateway gateway : gateways) {
            gateway.disconnect();
        }
        gateways.clear();
    }

    @Override
    protected void onRun() {
        try {
            for (X10AbstractGateway gateway : gateways) {
                gateway.read();
            }
        } catch (IOException ex) {
            Logger.getLogger(X10.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Returns true if the string is an X10 address (a letter from A to P and a
     * number from 01 to 16) eg: A01 or B13 or A8 and so on
     *
     * @param str the string to check if it is an X10 address
     * @return true if the string is a valid X10 address.
     */
    public static boolean isAnX10Address(String str) {
        str = str.toUpperCase();
        //if is less then 3 characters
        if (str.length() != 3) {
            return false;
        }
        //If the first char is not a letter from A to P
        char lettera = str.charAt(0);
        if (lettera < 'A' || lettera > 'P') {
            return false;
        }

        //if the second and third char is not an integer from 01 to 16
        try {
            int num = Integer.parseInt(str.substring(1, 3));
            if (num < 1 || num > 16) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Returns true if the string in input is an X10 command
     *
     * @param str string to check if it is an X10 command
     * @return true if the string is a valid X10 command
     */
    public static boolean isAnX10Command(String str) {
        str = str.toUpperCase();

        if (str.length() < 3 || str.length() > 4) {
            return false;
        }

        char lettera = str.charAt(0);
        if (lettera < 'A' || lettera > 'P') {
            return false;
        }

        //A valid X10 command code
        str = str.substring(1);
        if (str.compareTo("ON") == 0) {
            return true;
        }
        if (str.compareTo("OFF") == 0) {
            return true;
        }
        if (str.compareTo("DIM") == 0) {
            return true;
        }
        if (str.compareTo("BGT") == 0) {
            return true;
        }
        if (str.compareTo("AUF") == 0) {
            return true;
        }
        if (str.compareTo("ALN") == 0) {
            return true;
        }
        if (str.compareTo("ALN") == 0) {
            return true;
        }
        if (str.compareTo("HRQ") == 0) {
            return true;
        }
        if (str.compareTo("HAK") == 0) {
            return true;
        }
        if (str.compareTo("PRG") == 0) {
            return true;
        }
        if (str.compareTo("SON") == 0) {
            return true;
        }
        if (str.compareTo("SOF") == 0) {
            return true;
        }
        if (str.compareTo("SRQ") == 0) {
            return true;
        }

        return false;

    }

    /**
     * Checks if the string is an X10 commands that needs an X10 target address
     *
     * @param cmd the string to check
     * @return true if the string is a command needing an address
     */
    public static boolean isAnX10TargetedCommand(String cmd) {
        if (!isAnX10Command(cmd)) {
            return false;
        }
        cmd = cmd.substring(1);
        if (cmd.compareTo("ON") == 0) {
            return true;
        }
        if (cmd.compareTo("OFF") == 0) {
            return true;
        }
        if (cmd.compareTo("DIM") == 0) {
            return true;
        }
        if (cmd.compareTo("BGT") == 0) {
            return true;
        }
        if (cmd.compareTo("AUF") == 0) {
            return false;
        }
        if (cmd.compareTo("ALN") == 0) {
            return false;
        }
        if (cmd.compareTo("ALN") == 0) {
            return false;
        }
        if (cmd.compareTo("HRQ") == 0) {
            return true;
        }
        if (cmd.compareTo("HAK") == 0) {
            return false;
        }
        if (cmd.compareTo("PRG") == 0) {
            return true;
        }
        if (cmd.compareTo("SON") == 0) {
            return true;
        }
        if (cmd.compareTo("SOF") == 0) {
            return true;
        }
        if (cmd.compareTo("SRQ") == 0) {
            return true;
        }
        return false;
    }
}
