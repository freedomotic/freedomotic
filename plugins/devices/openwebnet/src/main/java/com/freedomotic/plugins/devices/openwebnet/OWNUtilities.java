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
package com.freedomotic.plugins.devices.openwebnet;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 *
 * @author Mauro Cicolella
 */
public class OWNUtilities {

    /**
     * Frame validation
     *
     */
    private static boolean isValidFrame(String frameOpen) {
        int MAX_LENGTH_OPEN = 30;
        int lengthFrameOpen = frameOpen.length();
        String frameType = null;
        int j = 0;
        int i = 0;
        String sup = null;
        String campo = null;

        // frame ACK -->
        if (frameOpen.equals(OpenWebNet.MSG_OPEN_ACK)) {
            frameType = "OK_FRAME";
            return (true);
        }

        //  frame NACK -->
        if (frameOpen.equals(OpenWebNet.MSG_OPEN_NACK)) {
            frameType = "KO_FRAME";
            return (true);
        }


        //se il primo carattere non è *
        //oppure la frame è¨ troppo lunga
        //oppure se non termina con due '#'
        if ((frameOpen.charAt(0) != '*')
                || (lengthFrameOpen > MAX_LENGTH_OPEN)
                || (frameOpen.charAt(lengthFrameOpen - 1) != '#')
                || (frameOpen.charAt(lengthFrameOpen - 2) != '#')) {
            frameType = "ERROR_FRAME";
            return (false);
        }

        //Controllo se sono stati digitati dei caratteri
        for (j = 0; j < lengthFrameOpen; j++) {
            if (!Character.isDigit(frameOpen.charAt(j))) {
                if ((frameOpen.charAt(j) != '*') && (frameOpen.charAt(j) != '#')) {
                    System.out.println("Â°Â°Â° FRAME ERROR Â°Â°Â°");
                    frameType = "ERROR_FRAME";
                    return (false);
                }
            }
        }

        // frame normale ...	
        //*chi#indirizzo*cosa*dove#livello#indirizzo*quando##
        if (frameOpen.charAt(1) != '#') {
            //System.out.println("frame normale");
            frameType = "NORMAL_FRAME";
            //estraggo i vari campi della frame open nella prima modalitÃ  (chi+indirizzo e dove+livello+interfaccia)
            //assegnaChiCosaDoveQuando();
            //estraggo gli eventuali valori di indirizzo
            //assegnaIndirizzo();
            //estraggo gli eventuali valori di livello ed interfaccia
            //assegnaLivelloInterfaccia();
            return (true);
        }



        //per le altre tipologie di frame
        sup = null;
        sup = frameOpen.substring(2, frameOpen.length());
        //sprintf(sup, "%s", frameOpen+2);
        campo = null;
        i = 0;
        while (sup.charAt(i) != '*') {
            i++;
        }
        campo = sup.substring(0, i);
        //sprintf(campo, "%s", strtok(sup, "*"));
        sup = null;
        sup = frameOpen.substring(2 + campo.length() + 1, frameOpen.length());
        //sprintf(sup, "%s", frameOpen+2+strlen(campo)+1);
        if (sup.charAt(0) != '*') {
            i = 0;
            boolean trovato = false;
            while (i < sup.length()) {
                if (sup.charAt(i) == '*') {
                    trovato = true;
                    break;
                }
                i++;
            }
            if (trovato) {
                campo = campo.concat(sup.substring(0, i));
            }

            //sprintf(campo, "%s%s", campo, strtok(sup, "*"));
            sup = null;
            sup = frameOpen.substring(2 + campo.length() + 1, frameOpen.length());
            //sprintf(sup, "%s", frameOpen+2+strlen(campo)+1);
        }

        //frame richiesta stato ...
        //*#chi*dove##
        if (sup.charAt(0) != '*') {
            //System.out.println("frame stato");
            frameType = "STATE_FRAME";
            return (true);
        } else {
            //frame di richiesta misura ...
            //*#chi*dove*grandezza## o *#chi*dove*#grandezza*valore_Nï¿½##
            if (sup.charAt(1) != '#') {
                //System.out.println("Measure state");
                frameType = "MEASURE_FRAME";
                return (true);
            } //frame di scrittura grandezza ...
            //*#chi*dove*#grandezza*valore_Nï¿½##
            else {
                //System.out.println("frame write");
                frameType = "WRITE_FRAME";
                return (true);
            }
        }
    }

    // create the frame to send to the own gateway

    /**
     *
     * @param c
     * @return
     */
        public static String createFrame(Command c) {
        String frameToSend = null;
        String address[] = null;
        String who = null;
        String what = null;
        String where = null;
        String commandType = null;
        String dimension = null;
        String frame = null;

        commandType = c.getProperty("command-type");

        switch (commandType) {
            case "command": {
                who = c.getProperty("who");
                what = c.getProperty("what");
                address = c.getProperty("address").split("\\*");
                where = address[1];
                frameToSend = "*" + who + "*" + what + "*" + where + "##";
                break;
            }
            case "request": {
                where = c.getProperty("address");
                dimension = c.getProperty("dimension");
                frameToSend = "#" + where + "*" + dimension + "##";
                break;
            }
            case "custom": {
                frame = c.getProperty("frame");
                frameToSend = frame;
                break;
            }
        }
        return (frameToSend);
    }

    /**
     *
     * @return
     */
    public static String getDateTime() {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return (sdf.format(calendar.getTime()));
    }

    /**
     *
     * @param temperature
     * @return
     */
    public static String convertTemperature(String temperature) {
        String temp = null;
        if (!temperature.substring(0).equalsIgnoreCase("0")) {
            temp = "-";
        }
        temp += temperature.substring(1, 2);
        temp += ".";
        temp += temperature.substring(3);
        return (temp);
    }

    /**
     *
     * @param dayNumber
     * @return
     */
    public static String dayName(String dayNumber) {
        String dayName = null;
        switch (new Integer(Integer.parseInt(dayNumber))) {
            case (0):
                dayNumber = "Sunday";
            case (1):
                dayNumber = "Monday";
            case (2):
                dayNumber = "Tuesday";
            case (3):
                dayNumber = "Wednesday";
            case (4):
                dayNumber = "Thursday";
            case (5):
                dayNumber = "Friday";
            case (6):
                dayNumber = "Saturday";
        }
        return (dayName);
    }

    /**
     *
     * @param modelNumber
     * @return
     */
    public static String gatewayModel(String modelNumber) {
        String model = null;
        switch (new Integer(Integer.parseInt(modelNumber))) {
            case (2):
                model = "MHServer";
            case (4):
                model = "MH20F0";
            case (6):
                model = "F452V";
            case (11):
                model = "MHServer2";
            case (12):
                model = "F453AV";
            case (13):
                model = "H4684";
            default:
                model = "Unknown";
        }
        return (model);
    }
}
