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


package com.freedomotic.plugins.devices.arduinoremotecontroller;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @autor Mauro Cicolella <mcicolella@libero.it>
 */

public class ArduinoRemoteController extends Protocol {

    private static final Logger LOG = Logger.getLogger(ArduinoRemoteController.class.getName());   
    private static int POLLING_TIME = 1000;
    private static int SOCKET_TIMEOUT = 1000;
    public final String UDP_SERVER_HOSTNAME = configuration.getStringProperty("udp-server-hostname", "192.168.1.100");
    public final int UDP_SERVER_PORT = configuration.getIntProperty("udp-server-port", 7331);
    public final String DELIMITER = configuration.getStringProperty("delimiter", ":");
    private int udpPort;
    private static UDPServer udpServer = null;

    /**
     * Initializations
     */
    public ArduinoRemoteController() {
        super("Arduino Remote Controller", "/arduino-remote-controller/arduino-remote-controller-manifest.xml");
        setPollingWait(POLLING_TIME);
    }

    /**
     * Sensor side
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            udpServer = new UDPServer(this);
            udpServer.start();
        } catch (IOException iOException) {
            LOG.severe("Error during UDP server creation " + iOException.toString());
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //release resources
        udpServer.interrupt();
        udpServer = null;
        setPollingWait(-1); //disable polling
        //display the default description
        setDescription(configuration.getStringProperty("description", "Arduino Remote Controller stopped"));
    }

    @Override
    protected void onRun() {
    }

    public void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, "arduino-remote-controller", objectAddress);
        event.addProperty("button.pressed", eventValue);
        //publish the event on the messaging bus
        this.notifyEvent(event);
        System.out.println("Sending event : " + event.toString());  // FOR DEBUG USE
    }

    /**
     * Actuator side
     */
    @Override
    public void onCommand(Command c) throws UnableToExecuteException {
    }


    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
