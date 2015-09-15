/**
*
* Copyright (c) 2009-2015 Freedomotic team
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
* along with Freedomotic; see the file COPYING. If not, see
* <http://www.gnu.org/licenses/>.
*/

package com.freedomotic.plugins.devices.openwebnet;

import com.myhome.fcrisciani.connector.MyHomeJavaConnector;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MonitorSessionThread extends Thread {

    private static OpenWebNet pluginReference = null;
    private String ipAddress = null;
    private Integer port = 0;

    public void run() {
        //connect to own gateway
        pluginReference.myPlant = new MyHomeJavaConnector(ipAddress, port);
        try {
            OpenWebNet.myPlant.startMonitoring();
            while (true) {
                try {
                    String readFrame = pluginReference.myPlant.readMonitoring();
                    System.out.println("Comando: " + readFrame);
                    buildEventFromFrame(readFrame);
                } catch (InterruptedException ex) {
                    Logger.getLogger(OpenWebNet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (IOException ex) {
        }
    }

    public MonitorSessionThread(OpenWebNet pluginReference, String ipAddress, Integer port) {
        // eventuali azioni per il costruttore 
        this.pluginReference = pluginReference;
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public static String getDateTime() {
        Calendar calendar = new GregorianCalendar();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return (sdf.format(calendar.getTime()));
    }

     public static ProtocolRead buildEventFromFrame(String frame) {
        String who = null;
        String what = null;
        String where = null;
        String objectClass = null;
        String objectName = null;
        String messageType = null;
        String messageDescription = null;
        String[] frameParts = null;
        ProtocolRead event = null;

        int length = frame.length();
        if (frame.isEmpty() || !frame.endsWith("##")) {
            OpenWebNet.LOG.severe("Malformed frame " + frame + " " + frame.substring(length - 2, length));
            OWNFrame.writeAreaLog(getDateTime() + " Mon: Malformed frame " + frame);
            return null;
        }

        if (frame.equals(OpenWebNet.MSG_OPEN_ACK)) {
            messageType = "ack";
            return null;
        }

        if (frame.equals(OpenWebNet.MSG_OPEN_NACK)) {
            messageType = "nack";
            return null;
        }


        if (frame.substring(0, 2).equalsIgnoreCase("*#")) { // remove *# and ## 
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped 
            who = frameParts[0];
            where = frameParts[1];
            objectClass = null;
            objectName = who + "*" + where;
            event = new ProtocolRead(pluginReference, "openwebnet", who + "*" + where); // LIGHTING if (who.equalsIgnoreCase("1")) {
            if (who.equalsIgnoreCase("1")) {
                if (frameParts[2].equalsIgnoreCase("1")) {
                    String level = frameParts[3];
                    String speed = frameParts[4];
                    messageDescription = "Luminous intensity change";
                    if (level != null) {
                        event.addProperty("level", level);
                    }
                    if (speed != null) {
                        event.addProperty("speed", speed);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("2")) {
                    String hour = frameParts[3];
                    String min = frameParts[4];
                    String sec = frameParts[5];
                    messageDescription = "Luminous intensity change";
                    if (hour != null) {
                        event.addProperty("hour", hour);
                    }
                    if (min != null) {
                        event.addProperty("min", min);
                    }
                    if (sec != null) {
                        event.addProperty("sec", sec);
                    }
                }
            }
            // POWER MANAGEMENT
            if (who.equalsIgnoreCase("3")) {
                //objectClass = "Powermeter";
                //objectName = who + "*" + where;
                String voltage = null;
                String current = null;
                String power = null;
                String energy = null;
                if (frameParts[3].equalsIgnoreCase("0")) {
                    voltage = frameParts[3];
                    current = frameParts[4];
                    power = frameParts[5];
                    energy = frameParts[6];
                    messageDescription = "Load control status";
                    if (voltage != null) {
                        event.addProperty("voltage", voltage);
                    }
                    if (current != null) {
                        event.addProperty("current", current);
                    }
                    if (power != null) {
                        event.addProperty("power", power);
                    }
                    if (energy != null) {
                        event.addProperty("energy", energy);
                    }
                }
                if (frameParts[3].equalsIgnoreCase("1")) {
                    voltage = frameParts[3];
                    if (voltage != null) {
                        event.addProperty("voltage", voltage);
                    }
                    messageDescription = "Voltage status";
                }
                if (frameParts[3].equalsIgnoreCase("2")) {
                    current = frameParts[3];
                    if (current != null) {
                        event.addProperty("current", current);
                    }
                    messageDescription = "Current status";
                }
                if (frameParts[3].equalsIgnoreCase("3")) {
                    power = frameParts[3];
                    if (power != null) {
                        event.addProperty("power", power);
                    }
                    messageDescription = "Power status";
                }
                if (frameParts[3].equalsIgnoreCase("4")) {
                    energy = frameParts[3];
                    if (energy != null) {
                        event.addProperty("energy", energy);
                    }
                    messageDescription = "Energy status";
                }
            }
            // TERMOREGULATION 
            if (who.equalsIgnoreCase("4")) {
                String temperature = null;
                if (frameParts[3].equalsIgnoreCase("0")) {
                    temperature = frameParts[3];
                    temperature = OWNUtilities.convertTemperature(temperature);
                    messageDescription = "Temperature value";
                    if (temperature != null) {
                        event.addProperty("temperature", temperature);
                    }
                }
            }
            // GATEWAY CONTROL
            if (who.equalsIgnoreCase("13")) {
                String hour = null;
                String minute = null;
                String second = null;
                String timeZone = null;
                String dayWeek = null;
                String day = null;
                String month = null;
                String year = null;
                String version = null;
                String release = null;
                String build = null;
                if (frameParts[2].equalsIgnoreCase("0")) {
                    hour = frameParts[3];
                    minute = frameParts[4];
                    second = frameParts[5];
                    timeZone = frameParts[6]; // aggiungere funzione conversione
                    messageType = "gatewayControl";
                    messageDescription = "Time request";
                    if (hour != null) {
                        event.addProperty("hour", hour);
                    }
                    if (minute != null) {
                        event.addProperty("minute", minute);
                    }
                    if (second != null) {
                        event.addProperty("second", second);
                    }
                    if (timeZone != null) {
                        event.addProperty("timeZone", timeZone);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("1")) {
                    dayWeek = OWNUtilities.dayName(frameParts[3]);
                    day = frameParts[4];
                    month = frameParts[5];
                    year = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Date request";
                    if (dayWeek != null) {
                        event.addProperty("dayWeek", dayWeek);
                    }
                    if (day != null) {
                        event.addProperty("day", day);
                    }
                    if (month != null) {
                        event.addProperty("month", month);
                    }
                    if (year != null) {
                        event.addProperty("year", year);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("10")) {
                    String ip1 = frameParts[3];
                    String ip2 = frameParts[4];
                    String ip3 = frameParts[5];
                    String ip4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "IP request";
                    event.addProperty("ip-address", ip1 + "." + ip2 + "." + ip3 + "." + ip4);
                }
                if (frameParts[2].equalsIgnoreCase("11")) {
                    String netmask1 = frameParts[3];
                    String netmask2 = frameParts[4];
                    String netmask3 = frameParts[5];
                    String netmask4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Netmask request";
                    event.addProperty("netmask", netmask1 + "." + netmask2 + "." + netmask3 + "." + netmask4);
                }
                if (frameParts[2].equalsIgnoreCase("12")) {
                    String mac1 = frameParts[3];
                    String mac2 = frameParts[4];
                    String mac3 = frameParts[5];
                    String mac4 = frameParts[6];
                    String mac5 = frameParts[7];
                    String mac6 = frameParts[8];
                    messageType = "gatewayControl";
                    messageDescription = "MAC request";
                    event.addProperty("mac-address", mac1 + ":" + mac2 + ":" + mac3 + ":" + mac4 + ":" + mac5 + ":" + mac6);
                }
                if (frameParts[2].equalsIgnoreCase("15")) {
                    String model = OWNUtilities.gatewayModel(frameParts[3]);
                    messageType = "gatewayControl";
                    messageDescription = "Model request";
                    event.addProperty("model", model);
                }
                if (frameParts[2].equalsIgnoreCase("16")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Firmware version request";
                    event.addProperty("firmware - version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("17")) {
                    String days = frameParts[3];
                    String hours = frameParts[4];
                    String minutes = frameParts[5];
                    String seconds = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Uptime request";
                    event.addProperty("uptime", days + "D:" + hours + "H:" + minutes + "m:" + seconds + "s");
                }
                if (frameParts[2].equalsIgnoreCase("22")) {
                    hour = frameParts[3];
                    minute = frameParts[4];
                    second = frameParts[5];
                    timeZone = frameParts[6];
                    String weekDay = OWNUtilities.dayName(frameParts[7]);
                    day = frameParts[8];
                    month = frameParts[9];
                    year = frameParts[10];
                    messageType = "gatewayControl";
                    messageDescription = "Date&Time request";
                    event.addProperty("date", weekDay + " " + day + "/" + month + "/" + year);
                    event.addProperty("time", hour + ":" + minute + ":" + second);
                }
                if (frameParts[2].equalsIgnoreCase("23")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Kernel version request";
                    event.addProperty("kernel - version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("24")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Distribution version request";
                    event.addProperty("distribution - version", version + "." + release + "." + build);
                }
            }

            event.addProperty("who", who);
            if (where != null) {
                event.addProperty("where", where);
            }
            if (messageDescription != null) {
                event.addProperty("messageDescription", messageDescription);
            }
            if (messageType != null) {
                event.addProperty("messageType", messageType);
            }
            // notify event
            pluginReference.notifyEvent(event);
            OWNFrame.writeAreaLog(getDateTime() + " Rx: " + frame + " " + "(" + messageDescription + ")");

        }


        if (!(frame.substring(0, 2).equalsIgnoreCase("*#"))) {
            // remove delimiter chars * and ##
            frame = frame.substring(1, length - 2);
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped
            who = frameParts[0];
            what = frameParts[1];
            where = frameParts[2];
            event = new ProtocolRead(pluginReference, "openwebnet", who + "*" + where);
            objectName = who + "*" + where;


            switch (Integer.parseInt(who)) {
                //LIGHTING
                case 1:
                    messageType = "Lighting";
                    if ((where.length() > 1) && (!where.substring(1, 1).equalsIgnoreCase("#"))) {
                        event.addProperty("object.class", objectClass);
                    }

                    switch (Integer.parseInt(what)) {
                        // Light OFF
                        case 0:
                            messageDescription = "Light OFF";
                            break;
                        // Light ON        
                        case 1:
                            messageDescription = "Light ON";
                            break;
                        default:
                            if (Integer.parseInt(what) >= 2 && Integer.parseInt(what) <= 10) {
                                messageDescription = "Light Dimmer";
                            }
                            break;
                    }
                    break; // close LIGHTING switch

                // AUTOMATION
                case 2:
                    messageType = "Automation";
                    switch (Integer.parseInt(what)) {
                        case 0:
                            messageDescription = "Automation STOP";
                            break;
                        case 1:
                            messageDescription = "Automation UP";
                            break;
                        case 2:
                            messageDescription = "Automation DOWN";
                            break;
                    }
                    break; // close AUTOMATION switch

                // POWER MANAGEMENT      
                case 3:
                    objectClass = "Powermeter";
                    messageType = "Power management";
                    switch (Integer.parseInt(what)) {
                        case 0:
                            messageDescription = "Load disable";
                            break;
                        case 1:
                            messageDescription = "Load enable";
                            break;
                        case 2:
                            messageDescription = "Load forced";
                            break;
                        case 3:
                            messageDescription = "Stop load forced";
                            break;
                    }
                    break; // close POWER MANAGEMENT switch


                // TERMOREGULATION
                case 4:
                    messageType = "termoregulation";
                    switch (Integer.parseInt(what)) {
                        case 0:
                            messageDescription = "Conditioning";
                            break;
                        case 1:
                            messageDescription = "Heating";
                            break;
                        case 20:
                            messageDescription = "Remote Control disabled";
                            break;
                        case 21:
                            messageDescription = "Remote Control enabled";
                            break;
                        case 22:
                            messageDescription = "At least one Probe OFF";
                            break;
                        case 23:
                            messageDescription = "At least one Probe in protection";
                            break;
                        case 24:
                            messageDescription = "At least one Probe in manual";
                            break;
                        case 30:
                            messageDescription = "Failure discovered";
                            break;
                        case 31:
                            messageDescription = "Central Unit battery KO";
                            break;
                        case 103:
                            messageDescription = "OFF Heating";
                            break;
                        case 110:
                            messageDescription = "Manual Heating";
                            break;
                        case 111:
                            messageDescription = "Automatic Heating";
                            break;
                        case 202:
                            messageDescription = "AntiFreeze";
                            break;
                        case 203:
                            messageDescription = "OFF Conditioning";
                            break;
                        case 210:
                            messageDescription = "Manual Conditioning";
                            break;
                        case 211:
                            messageDescription = "Automatic Conditioning";
                            break;
                        case 302:
                            messageDescription = "Thermal Protection";
                            break;
                        case 303:
                            messageDescription = "Generic OFF";
                            break;
                        case 311:
                            messageDescription = "Automatic Generic";
                            break;
                    }
                    break; // close TERMOREGULATION switch

                // BURGLAR ALARM    
                case 5:
                    messageType = "alarm";
                    switch (Integer.parseInt(what)) {
                        case 0:
                            messageDescription = "System on maintenance";
                            break;
                        case 4:
                            messageDescription = "Battery fault";
                            break;
                        case 5:
                            messageDescription = "Battery OK";
                            break;
                        case 6:
                            messageDescription = "No Network";
                            break;
                        case 7:
                            messageDescription = "Network OK";
                            break;
                        case 8:
                            messageDescription = "System engaged";
                            break;
                        case 9:
                            messageDescription = "System disengaged";
                            break;
                        case 10:
                            messageDescription = "Battery KO";
                            break;
                        case 11: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Zone " + where + " engaged";
                        }
                        break;
                        case 12: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Aux " + where + " in Technical alarm ON";
                        }
                        break;
                        case 13: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Aux " + where + " in Technical alarm RESET";
                        }
                        break;
                        case 15: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Zone " + where + " in Intrusion alarm";
                        }
                        break;
                        case 16: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Zone " + where + " in Tampering alarm";
                        }
                        break;
                        case 17: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Zone " + where + " in Anti-panic alarm";
                        }
                        break;
                        case 18: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Zone " + where + " divided";
                        }
                        break;
                        case 31: // prelevare la sottostringa di where #N 
                        {
                            messageDescription = "Silent alarm from aux " + where;
                        }
                        break;
                    }
                    break; // close BURGLAR ALARM switch

                // SOUND SYSTEM
                case 16:
                    messageType = "Sound System";
                    switch (Integer.parseInt(what)) {
                        case 0:
                            messageDescription = "ON Baseband";
                            break;
                        case 3:
                            messageDescription = "ON Stereo channel";
                            break;
                        case 10:
                            messageDescription = "OFF Baseband";
                            break;
                        case 13:
                            messageDescription = "OFF Stereo channel";
                            break;
                        case 30:
                            messageDescription = "Sleep on baseband";
                            break;
                        case 33:
                            messageDescription = "Sleep on stereo channel";
                            break;
                    }
                    break; // close SOUND SYSTEM switch


            } // close switch(who)


            if (who != null) {
                event.addProperty("who", who);
            }
            if (what != null) {
                event.addProperty("what", what);
            }
            if (where != null) {
                event.addProperty("where", where);
            }
            if (messageType != null) {
                event.addProperty("messageType", messageType);
            }
            if (messageDescription != null) {
                event.addProperty("messageDescription", messageDescription);
            }
            //  if (objectClass != null) {
            //      event.addProperty("object.class", objectClass);
            //  }
            if (objectName != null) {
                event.addProperty("object.name", objectName);
            }
            //Freedomotic.logger.info("Frame " + frame + " " + "is " + messageType + " message. Notify it as Freedomotic event " + messageDescription); // for debug
            OWNFrame.writeAreaLog(getDateTime() + " Rx: " + frame + " " + "(" + messageDescription + ")");
            pluginReference.notifyEvent(event);
        }
        return null;
    }
}