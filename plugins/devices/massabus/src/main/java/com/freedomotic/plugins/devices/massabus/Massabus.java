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
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package com.freedomotic.plugins.devices.massabus;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.serial.SerialConnectionProvider;
import com.freedomotic.serial.SerialDataConsumer;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Massabus extends Protocol implements SerialDataConsumer {
    private static final Logger LOG = Logger.getLogger(Massabus.class.getName()); 
    SerialConnectionProvider serial;

    public Massabus() {
        super("Massabus", "/massabus/massabus-manifest.xml");
        setPollingWait(2000); //waits 2000ms in onRun method before call onRun() again
    }

    @Override
    public void onStart() {
        //called when the user starts the plugin from UI
        if (serial == null) {
            serial = new SerialConnectionProvider();
            //UNCOMMENT IF NEEDED:serial.setPortName(configuration.getStringProperty("serial.port", "dev/usb0"));
            //instead of specify a port name is also possible to search for 
            //the right port using an hello message and an expected reply
            //the hello message will be broadcasted to all usb connected devices
            serial.setAutodiscover(
                    configuration.getStringProperty("serial.hello", "hello"),
                    configuration.getStringProperty("serial.hello-reply", "hello-reply"));
            //connection parameters
            serial.setPortBaudrate(configuration.getIntProperty("serial.boudrate", 9600));
            serial.setPortDatabits(configuration.getIntProperty("serial.databits", 8));
            serial.setPortParity(configuration.getIntProperty("serial.parity", 0));
            serial.setPortStopbits(configuration.getIntProperty("serial.stopbits", 1));

        }
    }

    @Override
    public void onStop() {
        //called when the user stops the plugin from UI
        if (serial != null) {
            serial.disconnect();
        }
    }

    @Override
    protected void onRun() {
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        try {
            //sends the string to serialport and waits the amount of time
            //written in setPollingWait() method [you can found it in Massabus constructor]
            String message = "A_STRING_MESSAGE";
            String reply = serial.send(message);
            LOG.info("Massabus replies " + reply + " to message " + message);
        } catch (IOException ex) {
            setDescription("Stopped for IOException in onRun"); //write here a better error message for the user
            stop();

        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //this method receives freedomotic commands send on channel app.actuators.protocol.massabus.in
        String message = c.getProperty("massabus.message");
        String reply = null;
        try {
            reply = serial.send(message);
        } catch (IOException iOException) {
            setDescription("Stopped for IOException in onCommand"); //write here a better error message for the user
            stop();
        }
        LOG.info("Massabus replies " + reply + " after executing command " + c.getName());
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
    public void onDataAvailable(String data) {
        //called when something is readed from the serial port
        LOG.info("Massabus reads '" + data + "'");
    }
}
