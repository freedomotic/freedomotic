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
package com.freedomotic.plugins.devices.PioneerKuro;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.serial.SerialConnectionProvider;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class PioneerKuroProtocol extends Protocol {

    private static final Logger LOG = Logger.getLogger(PioneerKuroProtocol.class.getName());
    static public final char STX = 02;
    static public final char ETX = 03;
    SerialConnectionProvider usb;
    private static int POLLING_TIME = 1000;
    private boolean osd_active = true;
    static public final int OK = 1;
    static public final int ERR = 0;

    public PioneerKuroProtocol() {
        super("Pioneer Kuro", "/pioneer-kuro/PioneerKuro-Protocol.xml");
        usb = new SerialConnectionProvider();
        usb.setPortStopbits(1);
        usb.setPortParity(0);
        usb.setPortDatabits(8);
        String port = configuration.getStringProperty("port", "/dev/ttyUSB10");
        usb.setPortName(port);
        int baudrate = configuration.getIntProperty("baudrate", 19200);
        usb.setPortBaudrate(baudrate);
        setPollingWait(POLLING_TIME);
        start();
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        POLLING_TIME = configuration.getIntProperty("polling-time", 1000);
        setPollingWait(POLLING_TIME);
        usb.connect();
        disableOSD();
    }

    @Override
    public void onStop() {
        super.onStop();
        //set the OSD on
        String output = sendCommand("OSDS01");
        enableOSD();
        //release resources       
        setPollingWait(-1); //disable polling
        usb.disconnect();
        //display the default description
        setDescription(configuration.getStringProperty("description", "Kuro Pioneer"));

    }

    String sendCommand(String command) {
        String output = "";
        try {
            String serialCommand = STX + "**" + command + ETX;
            output = usb.send(serialCommand);
            return output;
        } catch (IOException ex) {
            Logger.getLogger(PioneerTest.class.getName()).log(Level.SEVERE, null, ex);
            return output;
        }
    }

    @Override
    protected void onRun() {

        disableOSD();
        int readedValues = 0;
        readedValues += readHardwareInputValue();
        readedValues += readHardwareBehaviorValue("VOL", "volume-value", 3);
        readedValues += readHardwareBehaviorValue("AMT", "mute-value", 2);
        readedValues += readHardwareBehaviorValue("AVS", "avselection-value", 2);
        readedValues += readHardwareBehaviorValue("SZM", "screenmode-value", 2);

    }

    void disableOSD() {
        if (osd_active) {
            String op = sendCommand("OSDS00");
            System.out.println("DisableOSD: " + op);
            osd_active = false;
        }

    }

    void enableOSD() {
        if (!osd_active) {
            String op = sendCommand("OSDS01");
            System.out.println("EnableOSD: " + op);
            osd_active = true;
        }

    }

    public void close() throws IOException, UnableToExecuteException {
        usb.disconnect();
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        System.out.println("Command receive: " + c.toString());
        String serialCommand = composeSerialCommand(c);
        System.out.println("Command to send to serial: " + serialCommand);
        //pause the polling
        setPollingWait(-1);
        enableOSD();
        String output = usb.send(serialCommand);
        System.out.println("Output from serial: " + output);
        if (output.contains("ERR")) {
            c.setExecuted(false);
        } else if (output.contains("XXX")) {//this means that the TV was already in the state that the command try to send
            c.setExecuted(false);
        }
        setPollingWait(POLLING_TIME);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private String composeSerialCommand(Command c) {

        String command = c.getProperty("command");
        String parameter = c.getProperty("parameter");
        String hardwareString = command;
//        if (command.equals("PON") | command.equals("POF")) {
//        }else 
        if (command.equals("VOL")) {
            if (parameter.equals("UPn") | parameter.equals("DWn")) {
                hardwareString += parameter;
            } else {
                if (parameter.length() < 2) {
                    hardwareString += "00";
                } else if (parameter.length() < 3) {
                    hardwareString += "0";
                }
                hardwareString += parameter;
            }
        } else if (command.equals("INC")) {
            if (parameter.length() < 2) {
                hardwareString += "00";
            } else if (parameter.length() < 3) {
                hardwareString += "0";
            }
            hardwareString += parameter;
        } else if (command.equals("INP")) {
            hardwareString += "S" + parameter;
//            if (parameter.split("Input ")[1].equals("6(PC)"))
//                hardwareString += "S06";
//            else
//                hardwareString += "S0" + parameter.split("Input ")[1];        

        } else if (command.equals("AMT")) {
            hardwareString += parameter;
        } else if (command.equals("CHN")) {
            hardwareString += parameter;
        } else if (command.equals("AVS")) {
            hardwareString += "S" + parameter;
            //TODO: Use enum
//            if (parameter.equals("STANDARD")) {
//                hardwareString += "S" + "01";
//            } else if (parameter.equals("DYNAMIC")) {
//                hardwareString += "S" + "02";
//            } else if (parameter.equals("MOVIE")) {
//                hardwareString += "S" + "03";
//            } else if (parameter.equals("GAME")) {
//                hardwareString += "S" + "04";
//            } else if (parameter.equals("SPORT")) {
//                hardwareString += "S" + "05";
//            } else if (parameter.equals("PURE")) {
//                hardwareString += "S" + "06";
//            } else if (parameter.equals("USER")) {
//                hardwareString += "S" + "07";
//            }
        } else if (command.equals("SZM")) {

            hardwareString += "S" + parameter;
//            if (parameter.equals("DOTbyDOT")) {
//                hardwareString += "S" + "01";
//            } else if (parameter.equals("4:3")) {
//                hardwareString += "S" + "02";
//            } else if (parameter.equals("FULL")) {
//                hardwareString += "S" + "03";
//            } else if (parameter.equals("ZOOM")) {
//                hardwareString += "S" + "04";
//            } else if (parameter.equals("CINEMA")) {
//                hardwareString += "S" + "05";
//            } else if (parameter.equals("WIDE")) {
//                hardwareString += "S" + "06";
//            } else if (parameter.equals("FULL 14:9")) {
//                hardwareString += "S" + "07";
//            } else if (parameter.equals("CINEMA 14:9")) {
//                hardwareString += "S" + "07";
//            } else if (parameter.equals("AUTO")) {
//                hardwareString += "S" + "07";
//            } else if (parameter.equals("WIDE2")) {
//                hardwareString += "S" + "07";
        } else if (command.equals("RMC")) {
            hardwareString += parameter;
//            }
        }
        return STX + "**" + hardwareString + ETX;
    }

    //special Case
    private int readHardwareInputValue() {
        ProtocolRead event;
        try {
            String serialCommand = STX + "**" + "INP" + ETX;
            System.out.println("Command to send to serial: " + serialCommand);
            String output = usb.send(serialCommand);
            System.out.println("Output from serial: " + output);
            if (output.contains("XXX")) //TV is off
            {
                event = new ProtocolRead(this, "pioneer-kuro", "pioneer-kuro");
                event.addProperty("hardware-behavior", "POWER");
                event.addProperty("power-value", "false");
                this.notifyEvent(event);
            } else if (!output.contains("ERR")) {
                event = new ProtocolRead(this, "pioneer-kuro", "pioneer-kuro");
                event.addProperty("hardware-behavior", "POWER");
                event.addProperty("power-value", "true");
                this.notifyEvent(event);
                String value = output.substring(4, 6);
                if (value.equals("81")) {
                    //analog
                } else if (value.equals("83")) {//terrestrial
                } else if (value.equals("84")) {//Digital
                } else {
                    event = new ProtocolRead(this, "pioneer-kuro", "pioneer-kuro");
                    event.addProperty("hardware-behavior", "INP");
                    event.addProperty("input-value", value);
                    this.notifyEvent(event);
                }
            }
            return OK;

        } catch (IOException ex) {
            Logger.getLogger(PioneerKuroProtocol.class.getName()).log(Level.SEVERE, null, ex);
            return ERR;
        }

    }

    //Tries to read the callback of a command with the format INPS01
    private int readHardwareBehaviorValue(String command, String eventPropertyName, int numChars) {
        try {
            ProtocolRead event;
            String serialCommand = STX + "**" + command + ETX;
            System.out.println("Command to send to serial: " + serialCommand);
            String output = usb.send(serialCommand);
            System.out.println("Output from serial: " + output);

            if (output.contains(command)) {
                String behavior = command;
                String value = output.substring(6 - numChars, 6);
                event = new ProtocolRead(this, "pioneer-kuro", "pioneer-kuro");
                event.addProperty("hardware-behavior", behavior);
                event.addProperty(eventPropertyName, value);
                this.notifyEvent(event);
                return OK;
            } else if (output.contains("ERR") || output.contains("XXX")) {
                return ERR;
            }
        } catch (IOException ex) {
            Logger.getLogger(PioneerKuroProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ERR;
    }
}
