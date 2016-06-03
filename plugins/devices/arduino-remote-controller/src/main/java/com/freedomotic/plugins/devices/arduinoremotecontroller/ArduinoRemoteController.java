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
package com.freedomotic.plugins.devices.arduinoremotecontroller;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.UdpHelper;
import com.freedomotic.helpers.UdpListener;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.net.DatagramPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @autor Mauro Cicolella
 */
public class ArduinoRemoteController extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(ArduinoRemoteController.class.getName());
    private final String UDP_SERVER_HOSTNAME = configuration.getStringProperty("udp-server-hostname", "192.168.1.100");
    private final int UDP_SERVER_PORT = configuration.getIntProperty("udp-server-port", 7331);
    private final String DELIMITER = configuration.getStringProperty("delimiter", ":");

    private int udpPort;
    private UdpHelper udpServer;

    public ArduinoRemoteController() {
        super("Arduino Remote Controller", "/arduino-remote-controller/arduino-remote-controller-manifest.xml");
        setPollingWait(-1); // onRun() disabled
    }

    @Override
    public void onStart() {
        udpServer = new UdpHelper();
        udpServer.startServer("0.0.0.0", UDP_SERVER_PORT, new UdpListener() {
            @Override
            public void onDataAvailable(String sourceAddress, Integer sourcePort, String data) {
                LOG.info("Arduino Remote Controller received: '{}'", data);
                extractData(sourceAddress, sourcePort, data);
            }
        });
    }

    @Override
    public void onStop() {
        udpServer.stopServer();
        //display the default description
        setDescription(configuration.getStringProperty("description", "Arduino Remote Controller stopped"));
    }

    @Override
    protected void onRun() {
    }

    /**
     * Extracts data from udp packets.
     *
     * @param sourceAddress the client source address
     * @param sourcePort the client source port
     * @param data the payload
     */
    private void extractData(String sourceAddress, Integer sourcePort, String data) {
        String fields[] = null;
        String sensorConnector = null;
        String pressedButton = null;

        fields = data.split(DELIMITER);
        sensorConnector = fields[0];
        pressedButton = fields[1];
        sendEvent(sourceAddress + ":" + sensorConnector, pressedButton);
    }

    /**
     * Sends a Freedomotic event with the pressed button.
     *
     * @param objectAddress the object address in the form
     * 'sourceAddress:sensorConnector'
     * @param pressedButton the pressed button
     */
    public void sendEvent(String objectAddress, String pressedButton) {
        ProtocolRead event = new ProtocolRead(this, "arduino-remote-controller", objectAddress);
        event.getPayload().addStatement("button.pressed", pressedButton);
        //publish the event on the messaging bus
        LOG.debug("Sending event: '{}'", event.getPayload().getStatements());
        notifyEvent(event);
    }

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
