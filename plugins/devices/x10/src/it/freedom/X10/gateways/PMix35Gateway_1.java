/*Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedom.
 *
 * Freedom is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedom; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedom.X10.gateways;

import it.freedom.X10.X10AbstractGateway;
import it.nicoletti.serial.SerialConnectionProvider;
import java.io.IOException;
import java.util.Properties;

/**
 * An implementation of a reader/writer to an X10 transciever named Xannura Home PMIX35
 * You can fine the technical manual at http://freedomotic.googlecode.com/files/PMIX35.pdf
 * @author Enrico Nicoletti (enrico.nicoletti84@gmail.com)
 */
public class PMix35Gateway_1 implements X10AbstractGateway {

    private static SerialConnectionProvider usb = null;
    //PMIX strings for ack, nack and null messages
    private static final String PMIX_ACK = "$<9000!4A#".trim();
    private static final String PMIX_NACK = "$<9000?4A#".trim();
    private static final String PMIX_NULL = "$<900029#".trim();

    public PMix35Gateway_1() {
    }

    /**
     * A PMIX35 message is comepose by a start string + the x10 message + a checksum + PMIX end of line (the # character)
     * For example:
     * <ul>
     * <li> A01A01 AONAON   ->  $>9000LW A01A01 AONAON**#
     * </ul>
     * ** corresponds to the checksum (2 characters).
     * LW means line write in PMIX35 lingo
     * @param the string to translate in PMIX35 format
     * @return the encoded string ready to be sent to the PMIX35
     */
    private String composeMessage(String message) {
        //Adding the standard prefix
        message = "$>9000LW " + message;

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
        return message + cs + "#";
    }

    /**
     * Connection with the PMIX35 over serial port
     */
    public void connect() {
        if (usb == null) {
            Properties config = new Properties();
            config.setProperty("hello-message", "$>9000PXD3#"); //hello message defined by pmix15 comm protocol
            config.setProperty("hello-reply", "$<9000VP"); //expected reply to hello-message starts with this string
            config.setProperty("polling-message", "$>9000RQCE#"); //$>9000RQcs# forces read data using this string
            //The computer always take the initiative to read, the PMIX35 is a passive gateway
            config.setProperty("polling-time", "600"); //millisecs between reads.
            usb = new SerialConnectionProvider(config); //instantiating a new serial connection with the previous parameters
        }
    }

    public String send(String message) throws IOException {
        String reply = usb.send(composeMessage(message));
        return reply;
    }

    /**
     * Returns true if the string is an X10 address (a letter from A to P and a number from 01 to 16)
     * eg: A01 or B13 or A8 and so on
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
     * (a letter from A to P and a valid command code)
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
     * Checks if the string is an X10 commands that needs an X10 address
     * @param cmd the string to check
     * @return true if the string is a command needing an address
     */
    public static boolean thisCommandNeedsAddres(String cmd) {
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

    /**
     * Parse the PMIX35 readed line to find X10 standard codes
     * @param readed
     * @return
     */
    public String parseReaded(String readed) {

        //if an empty message is readed
        if (readed.compareToIgnoreCase(PMIX_NULL) == 0) {
            return "";
        }

        //if nothing is readed. Likely the PMIX35 is no longer connected to usb
        if (readed.isEmpty()) {
            usb.disconnect();
            return "";
        }

        //if readed is a line echo
        if (readed.startsWith("LE")) {
            return "";
        }

        //Parsing the readed string
        //removing the prifix ($<9000) and the suffix (CS#)
        //CS is the checksum (2 characters)
        readed = readed.substring(6, readed.length() - 3);

        //if the message is a network noise detection value
        if (readed.startsWith("ND")) {
            boolean isNoisy = (new Integer(readed.substring(2, 4)) == 1); //noise on the network
            System.err.println("To much noise on the X10 network, unable to read");
            //if the message is a network impedance value
        } else if (readed.startsWith("NI")) {
            int impedance = new Integer(readed.substring(2, 6)); //Network inmpedance
            System.out.println("X10 network impedance: " + impedance);
            readed = readed.substring(6);
        }

        //if we have analyzed the whole string
        if (readed.isEmpty()) {
            return "";
        }

        //otherwise remains an X10 standard string (EG: A01 AON)
        readed = readed.substring(10).trim();
        return readed;
    }
}
