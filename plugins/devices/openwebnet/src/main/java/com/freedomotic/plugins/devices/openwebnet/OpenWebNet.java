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

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class OpenWebNet extends Protocol {

    public static final Logger LOG = LoggerFactory.getLogger(OpenWebNet.class.getName());
    private final String host = configuration.getProperty("host");
    private final Integer port = Integer.parseInt(configuration.getProperty("port"));
    private String address = null;
    private String frame = null;
    private ProtocolRead event = null;
    private OWNFrame pluginGui = null;
    public clientjava.connections.ConnectionsManager ownHandler = clientjava.connections.ConnectionsManager.getInstance();

    /*
     *
     * OWN Diagnostic Frames
     *
     */
    final static String LIGHTNING_DIAGNOSTIC_FRAME = "*#1*0##";
    final static String AUTOMATIONS_DIAGNOSTIC_FRAME = "*#2*0##";
    final static String ALARM_DIAGNOSTIC_FRAME = "*#5##";
    final static String POWER_MANAGEMENT_DIAGNOSTIC_FRAME = "*#3##";

    /*
     *
     * OWN Control Messages
     *
     */
    final static String MSG_OPEN_ACK = "*#*1##";
    final static String MSG_OPEN_NACK = "*#*0##";

    /**
     *
     */
    public OpenWebNet() {
        super("OpenWebNet", "/openwebnet/openwebnet-manifest.xml");
        setPollingWait(-1);
    }

    protected void onShowGui() {
        bindGuiToPlugin(pluginGui);
    }

    @Override
    public void onStart() {
        pluginGui = new OWNFrame(this);
        ownHandler.init(host, port, this);
        ownHandler.startMonitoring();
        //System.out.println("PROVA FRAME ");
        //this.buildEventFromFrame("*#4*1*12*0205*3##");
        // this.buildEventFromFrame("*#4*1*0*0205*3##");
    }

    @Override
    protected void onRun() {
        // syncronize the software with the system status
        initSystem();

    }

    @Override
    public void onCommand(Command c) throws IOException, UnableToExecuteException {
        String frameToSend = OWNUtilities.createFrame(c);
        LOG.info("Trying to send frame ''{}'' to OWN gateway", frameToSend);
        ownHandler.inviaComandoOpen(frameToSend);
    }

    /**
     *
     * @return the logger istance
     */
    public Logger getLogger() {
        return LOG;
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onStop() {
        ownHandler.stopMonitoring();
        this.setDescription("Plugin stopped");
    }

    // sends diagnostic frames to syncronize the software with the real system
    private void initSystem() {
        LOG.info("Sending '{}' frame to initialize LIGHTNING", LIGHTNING_DIAGNOSTIC_FRAME);
        OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + LIGHTNING_DIAGNOSTIC_FRAME + " (initialize LIGHTNING)");
        ownHandler.inviaComandoOpen(LIGHTNING_DIAGNOSTIC_FRAME);
        //LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize AUTOMATIONS", AUTOMATIONS_DIAGNOSTIC_FRAME);
        //OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + AUTOMATIONS_DIAGNOSTIC_FRAME + " (initialize AUTOMATIONS)");
        //ownHandler.inviaComandoOpen(AUTOMATIONS_DIAGNOSTIC_FRAME);
        //LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize ALARM", ALARM_DIAGNOSTIC_FRAME);
        //OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + ALARM_DIAGNOSTIC_FRAME + " (initialize ALARM)");
        //ownHandler.inviaComandoOpen(ALARM_DIAGNOSTIC_FRAME);
        //LOG.log(Level.INFO, "Sending ''{0}'' frame to initialize POWER MANAGEMENT", POWER_MANAGEMENT_DIAGNOSTIC_FRAME);
        //OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Act:" + "Sending " + POWER_MANAGEMENT_DIAGNOSTIC_FRAME + " (initialize POWER MANAGEMENT)");
        //ownHandler.inviaComandoOpen(POWER_MANAGEMENT_DIAGNOSTIC_FRAME);
    }

    /**
     * Builds a Freedomotic event from the received frame.
     * 
     * @param frame the frame to build an event from
     */
    public void buildEventFromFrame(String frame) {

        String who = null;
        String what = null;
        String where = null;
        String dimension = null;
        String objectClass = null;
        String objectName = null;
        String messageType = null;
        String messageDescription = null;
        String[] frameParts = null;

        if (frame.isEmpty() || !frame.endsWith("##")) {
            LOG.error("Malformed frame " + frame);
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Mon: Malformed frame " + frame);
            return;
        }

        if (frame.equals(OpenWebNet.MSG_OPEN_ACK)) {
            messageType = "ack";
            return;
        }

        if (frame.equals(OpenWebNet.MSG_OPEN_NACK)) {
            messageType = "nack";
            return;
        }

        if (frame.substring(0, 2).equalsIgnoreCase("*#")) {
            // remove *# and ## 
            frame = frame.substring(2, frame.length() - 2);
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped 
            who = frameParts[0];
            where = frameParts[1];
            dimension = frameParts[2];
            objectClass = null;
            objectName = who + "*" + where;
            event = new ProtocolRead(this, "openwebnet", who + "*" + where); // LIGHTING if (who.equalsIgnoreCase("1")) {
            if (who.equalsIgnoreCase("1")) {
                if (frameParts[2].equalsIgnoreCase("1")) {
                    String level = frameParts[3];
                    String speed = frameParts[4];
                    messageDescription = "Luminous intensity change";
                    if (level != null) {
                        event.getPayload().addStatement("level", level);
                    }
                    if (speed != null) {
                        event.getPayload().addStatement("speed", speed);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("2")) {
                    String hour = frameParts[3];
                    String min = frameParts[4];
                    String sec = frameParts[5];
                    messageDescription = "Luminous intensity change";
                    if (hour != null) {
                        event.getPayload().addStatement("hour", hour);
                    }
                    if (min != null) {
                        event.getPayload().addStatement("min", min);
                    }
                    if (sec != null) {
                        event.getPayload().addStatement("sec", sec);
                    }
                }
            }
            // POWER MANAGEMENT - WHO=3
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
                        event.getPayload().addStatement("voltage", voltage);
                    }
                    if (current != null) {
                        event.getPayload().addStatement("current", current);
                    }
                    if (power != null) {
                        event.getPayload().addStatement("power", power);
                    }
                    if (energy != null) {
                        event.getPayload().addStatement("energy", energy);
                    }
                }
                if (frameParts[3].equalsIgnoreCase("1")) {
                    voltage = frameParts[3];
                    if (voltage != null) {
                        event.getPayload().addStatement("voltage", voltage);
                    }
                    messageDescription = "Voltage status";
                }
                if (frameParts[3].equalsIgnoreCase("2")) {
                    current = frameParts[3];
                    if (current != null) {
                        event.getPayload().addStatement("current", current);
                    }
                    messageDescription = "Current status";
                }
                if (frameParts[3].equalsIgnoreCase("3")) {
                    power = frameParts[3];
                    if (power != null) {
                        event.getPayload().addStatement("power", power);
                    }
                    messageDescription = "Power status";
                }
                if (frameParts[3].equalsIgnoreCase("4")) {
                    energy = frameParts[3];
                    if (energy != null) {
                        event.getPayload().addStatement("energy", energy);
                    }
                    messageDescription = "Energy status";
                }
            }
            // TERMOREGULATION 
            if (who.equalsIgnoreCase("4")) {
                String temperature = null;
                String setpoint = null;

                switch (Integer.parseInt(dimension)) {

                    // temperature read value
                    case 0:
                        objectClass = "Thermostat";
                        temperature = frameParts[3].substring(1, frameParts[3].length());
                        messageDescription = "Temperature read value";
                        event.getPayload().addStatement("openwebnet.temperature", temperature);
                        event.getPayload().addStatement("openwebnet.dimension", dimension);
                        event.getPayload().addStatement("object.class", objectClass);
                        break;

                    // setpoint read value
                    case 12:
                        objectClass = "Thermostat";
                        setpoint = frameParts[3].substring(1, frameParts[3].length());
                        messageDescription = "Setpoint read value";
                        event.getPayload().addStatement("openwebnet.setpoint", setpoint);
                        event.getPayload().addStatement("openwebnet.dimension", dimension);
                        event.getPayload().addStatement("object.class", objectClass);
                        break;
                }

            } // close TERMOREGULATION

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
                        event.getPayload().addStatement("hour", hour);
                    }
                    if (minute != null) {
                        event.getPayload().addStatement("minute", minute);
                    }
                    if (second != null) {
                        event.getPayload().addStatement("second", second);
                    }
                    if (timeZone != null) {
                        event.getPayload().addStatement("timeZone", timeZone);
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
                        event.getPayload().addStatement("dayWeek", dayWeek);
                    }
                    if (day != null) {
                        event.getPayload().addStatement("day", day);
                    }
                    if (month != null) {
                        event.getPayload().addStatement("month", month);
                    }
                    if (year != null) {
                        event.getPayload().addStatement("year", year);
                    }
                }
                if (frameParts[2].equalsIgnoreCase("10")) {
                    String ip1 = frameParts[3];
                    String ip2 = frameParts[4];
                    String ip3 = frameParts[5];
                    String ip4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "IP request";
                    event.getPayload().addStatement("ip-address", ip1 + "." + ip2 + "." + ip3 + "." + ip4);
                }
                if (frameParts[2].equalsIgnoreCase("11")) {
                    String netmask1 = frameParts[3];
                    String netmask2 = frameParts[4];
                    String netmask3 = frameParts[5];
                    String netmask4 = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Netmask request";
                    event.getPayload().addStatement("netmask", netmask1 + "." + netmask2 + "." + netmask3 + "." + netmask4);
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
                    event.getPayload().addStatement("mac-address", mac1 + ":" + mac2 + ":" + mac3 + ":" + mac4 + ":" + mac5 + ":" + mac6);
                }
                if (frameParts[2].equalsIgnoreCase("15")) {
                    String model = OWNUtilities.gatewayModel(frameParts[3]);
                    messageType = "gatewayControl";
                    messageDescription = "Model request";
                    event.getPayload().addStatement("model", model);
                }
                if (frameParts[2].equalsIgnoreCase("16")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Firmware version request";
                    event.getPayload().addStatement("firmware - version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("17")) {
                    String days = frameParts[3];
                    String hours = frameParts[4];
                    String minutes = frameParts[5];
                    String seconds = frameParts[6];
                    messageType = "gatewayControl";
                    messageDescription = "Uptime request";
                    event.getPayload().addStatement("uptime", days + "D:" + hours + "H:" + minutes + "m:" + seconds + "s");
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
                    event.getPayload().addStatement("date", weekDay + " " + day + "/" + month + "/" + year);
                    event.getPayload().addStatement("time", hour + ":" + minute + ":" + second);
                }
                if (frameParts[2].equalsIgnoreCase("23")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Kernel version request";
                    event.getPayload().addStatement("kernel - version", version + "." + release + "." + build);
                }
                if (frameParts[2].equalsIgnoreCase("24")) {
                    version = frameParts[3];
                    release = frameParts[4];
                    build = frameParts[5];
                    messageType = "gatewayControl";
                    messageDescription = "Distribution version request";
                    event.getPayload().addStatement("distribution - version", version + "." + release + "." + build);
                }
            }

            if (who != null) {
                event.getPayload().addStatement("openwebnet.who", who);
            }
            if (where != null) {
                event.getPayload().addStatement("openwebnet.where", where);
            }
            if (messageDescription != null) {
                event.getPayload().addStatement("openwebnet.messageDescription", messageDescription);
            }
            if (messageType != null) {
                event.getPayload().addStatement("openwebnet.messageType", messageType);
            }
            if (objectName != null) {
                event.getPayload().addStatement("object.name", objectName);
                event.getPayload().addStatement("autodiscovery.allow-clones", "false");
            }
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Rx: " + frame + " " + "(" + messageDescription + ")");
            LOG.info("Frame received from OWN gateway: '{}'", frame);
            // notify event
            notifyEvent(event);
            LOG.debug("EVENTO NOTIFICATO: " + event.getPayload().getStatements());

            return;
        }

        if (!(frame.substring(0, 2).equalsIgnoreCase("*#"))) {
            // remove delimiter chars * and ##
            frame = frame.substring(1, frame.length() - 2);
            frameParts = frame.split("\\*"); // * is reserved so it must be escaped
            who = frameParts[0];
            what = frameParts[1];
            where = frameParts[2];
            event = new ProtocolRead(this, "openwebnet", who + "*" + where);
            objectName = who + "*" + where;

            switch (Integer.parseInt(who)) {
                //LIGHTING - WHO=1
                case 1:
                    messageType = "Lighting";
                    objectClass = "Light";
                    if ((where.length() > 1) && (!where.substring(1, 1).equalsIgnoreCase("#"))) {
                        event.getPayload().addStatement("object.class", objectClass);
                    }

                    switch (Integer.parseInt(what)) {
                        // Light OFF - WHAT=0
                        case 0:
                            messageDescription = "Light OFF";
                            break;
                        // Light ON - WHAT=1        
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

                // AUTOMATION - WHO=2
                case 2:
                    messageType = "Automation";
                    switch (Integer.parseInt(what)) {
                        case 0: // Automation STOP - WHAT=0
                            messageDescription = "Automation STOP";
                            break;
                        case 1: // Automation UP - WHAT=1
                            messageDescription = "Automation UP";
                            break;
                        case 2: // Automation DOWN - WHAT=2
                            messageDescription = "Automation DOWN";
                            break;
                    }
                    break; // close AUTOMATION switch

                // POWER MANAGEMENT - WHO=3      
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

                // TERMOREGULATION - WHO=4
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

                // BURGLAR ALARM - WHO=5    
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
                event.getPayload().addStatement("openwebnet.who", who);
            }
            if (what != null) {
                event.getPayload().addStatement("openwebnet.what", what);
            }
            if (where != null) {
                event.getPayload().addStatement("openwebnet.where", where);
            }
            if (messageType != null) {
                event.getPayload().addStatement("openwebnet.messageType", messageType);
            }
            if (messageDescription != null) {
                event.getPayload().addStatement("openwebnet.messageDescription", messageDescription);
            }

            if (objectName != null) {
                event.getPayload().addStatement("object.name", objectName);
                event.getPayload().addStatement("autodiscovery.allow-clones", "false");
            }
            OWNFrame.writeAreaLog(OWNUtilities.getDateTime() + " Rx: " + frame + " " + "(" + messageDescription + ")");
            LOG.info("Frame received from OWN gateway: '{}'", frame);
            // notify event
            notifyEvent(event);
            LOG.debug("EVENTO NOTIFICATO: " + event.getPayload().getStatements());
        }
        return;
    }
}
