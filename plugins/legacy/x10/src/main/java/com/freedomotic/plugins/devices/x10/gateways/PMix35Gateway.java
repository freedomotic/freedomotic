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

package com.freedomotic.plugins.devices.x10.gateways;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.plugins.devices.x10.X10;
import com.freedomotic.plugins.devices.x10.X10AbstractGateway;
import com.freedomotic.plugins.devices.x10.X10Event;
import com.freedomotic.serial.SerialConnectionProvider;
import com.freedomotic.serial.SerialDataConsumer;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of a reader/writer for an X10 transciever named Xannura
 * Home PMIX35 You can find the technical manual at
 * http://freedomotic.googlecode.com/files/PMIX35.pdf
 *
 * @author Enrico Nicoletti (enrico.nicoletti84@gmail.com)
 */
public final class PMix35Gateway implements X10AbstractGateway, SerialDataConsumer {

    private static SerialConnectionProvider usb = null;
    //PMIX strings for ack, nack and null messages
    private static final String PMIX_ACK = "$<9000!4A#".trim();
    private static final String PMIX_NACK = "$<9000?4A#".trim();
    private static final String PMIX_NULL = "$<900029#".trim();
    private String lastReceived = "";
    private X10Event x10Event = new X10Event();
    private X10 plugin;

    public PMix35Gateway(X10 plugin) {
        this.plugin = plugin;
        connect();
    }

    /**
     * Connection with the PMIX35 over serial port
     */
    @Override
    public void connect() {
        if (usb == null) {
            Properties config = new Properties();
            config.setProperty("hello-message", "$>9000PXD3#"); //hello message defined by pmix15 comm protocol
            config.setProperty("hello-reply", "$<9000VP"); //expected reply to hello-message starts with this string
            config.setProperty("polling-message", "$>9000RQCE#"); //$>9000RQcs# forces read data using this string
            //The computer always take the initiative to read, the PMIX35 is a passive gateway
            config.setProperty("polling-time", "1000"); //millisecs between reads.
            config.setProperty("port", "/dev/ttyUSB0");
            usb = new SerialConnectionProvider(config); //instantiating a new serial connection with the previous parameters
            usb.addListener(this);
            usb.connect();
            if (usb.isConnected()) {
                plugin.setDescription("Connected to " + usb.getPortName());
            } else {
                plugin.setDescription("Unable to connect to " + config.getProperty("port"));
                plugin.stop();
            }
        }
    }

    @Override
    public void disconnect() {
        if (usb != null) {
            usb.disconnect();
            if (!usb.isConnected()) {
                plugin.setDescription("Disconnected");
            }
        }
    }

    @Override
    public String send(String message) throws IOException {
        String reply = usb.send(message);
        return reply;
    }

    /**
     * Parse the PMIX35 readed line to find X10 standard codes
     *
     * @param readed
     * @return
     */
    @Override
    public String parseReaded(String readed) {

        //if nothing is readed. Likely the PMIX35 is no longer connected to usb
        if (readed.isEmpty()) {
            usb.disconnect();
            return "";
        }

        //if readed is a noise detection/network impedance value discard this line
        if (readed.contains(PMIX_NULL)
                || readed.contains(PMIX_ACK)
                || readed.contains(PMIX_NACK)) {
            return "";
        }

        //removing the prifix ($<9000) and the suffix (CS#)
        //CS is the checksum (2 characters)
        readed = readed.substring(6, readed.length() - 3);
        //it's a command echo
        if (readed.startsWith("LE")) {
            System.out.println(readed);
            return "";
        }

        //if the message is a network noise detection value
        if (readed.startsWith("ND")) {
            boolean isNoisy = (new Integer(readed.substring(2, 4)) == 1); //noise on the network
            //System.err.println("Powerline network is really noisy. Possible loose of data");
            //if the message is a network impedance value
            readed = readed.substring(4);
        } else if (readed.startsWith("NI")) {
            int impedance = new Integer(readed.substring(2, 6)); //Network inmpedance
            readed = readed.substring(6);
        }

        //check again for command echo as we can have ND00LE A01... or NI0000LE A01...
        if (readed.startsWith("LE")) {
            System.out.println(readed);
            return "";
        }

        //otherwise remains an X10 standard string (EG: A01 AON)
        readed = readed.substring(10).trim();
        return readed;
    }

    /**
     * A PMIX35 message is composed by a start string + the x10 message + a
     * checksum + PMIX end of line (the # character) For example: <ul> <li>
     * A01A01 AONAON -> $>9000LW A01A01 AONAON**# </ul> ** corresponds to the
     * checksum (2 characters). LW means line write in PMIX35 lingo
     *
     * @param the string to translate in PMIX35 format
     * @return the encoded string ready to be sent to the PMIX35
     */
    @Override
    public String composeMessage(String housecode, String address, String command) {
        //Adding the standard prefix
        String message =
                "$>9000LW "
                + housecode + address
                + housecode + address
                + " "
                + housecode + command
                + housecode + command;

        //Calculate the chacksum
        int sum = 0;
        for (int i = 0; i < message.length(); i++) {
            sum += message.charAt(i);
        }
        //we need only the last to char of the checksum
        String cs = Integer.toHexString(sum);
        cs = cs.substring(cs.length() - 2);
        cs = cs.toUpperCase();

        //Adding the end of line character and construction the whole encoded string
        return message + cs + "#\r\n";
    }

    @Override
    public String getName() {
        return "PMIX35";
    }

    @Override
    public void onDataAvailable(String readed) {
        String[] tokens = readed.split("#");
        for (int i = 0; i < tokens.length; i++) {
            String line = parseReaded(tokens[i] + "#");
            if (!line.isEmpty() && !line.equals(lastReceived)) {
                lastReceived = line;
                System.out.println(lastReceived);
                if (X10.isAnX10Address(line)) {
                    x10Event.setAddress(line);
                } else {
                    if (X10.isAnX10Command(line)) {
                        x10Event.setFunction(line);
                        if (x10Event.isEventComplete()) {
                            System.out.println("send x10 event " + x10Event);
                            x10Event.send();
                        }
                    }
                }
            }
        }
    }

    @Override
    public void read() {
        try {
            send("$>9000RQCE#");
        } catch (IOException ex) {
            Logger.getLogger(PMix35Gateway.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
