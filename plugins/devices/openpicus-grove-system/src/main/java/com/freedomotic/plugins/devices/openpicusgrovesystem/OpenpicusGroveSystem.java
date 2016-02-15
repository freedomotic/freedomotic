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
package com.freedomotic.plugins.devices.openpicusgrovesystem;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.helpers.UdpHelper;
import com.freedomotic.helpers.UdpListener;
import com.freedomotic.reactions.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenpicusGroveSystem extends Protocol {

    public static final Logger LOG = LoggerFactory.getLogger(OpenpicusGroveSystem.class.getName());
    private final String UDP_SERVER_IP = configuration.getStringProperty("udp-server-ip", "192.168.1.100");
    private final int UDP_SERVER_PORT = configuration.getIntProperty("udp-server-port", 7331);
    private final String DELIMITER = configuration.getStringProperty("delimiter", ":");
    private UdpHelper udpHelper = null;
    private UdpListener consumer = null;

    /**
     * Initializations
     */
    public OpenpicusGroveSystem() {
        super("Openpicus Grove System", "/openpicus-grove-system/openpicus-grove-system-manifest.xml");
        setPollingWait(-1);
    }

    @Override
    public void onStart() throws PluginStartupException {
        UdpHelper server = new UdpHelper();

        server.startServer(UDP_SERVER_IP, UDP_SERVER_PORT, new UdpListener() {

            @Override
            public void onDataAvailable(String sourceAddress, Integer sourcePort, String data) {
                LOG.info("Received packet from '" + sourceAddress + "' with payload '" + data + "'");
                extractData(sourceAddress, sourcePort, data);
            }
        });
        setDescription("Listening on " + UDP_SERVER_IP + ":" + UDP_SERVER_PORT);
    }

    @Override
    public void onStop() {
        //display the default description
        setDescription(configuration.getStringProperty("description", "Openpicus Grove System stopped"));
    }

    @Override
    protected void onRun() {
    }

    private void extractData(String sourceAddress, Integer sourcePort, String data) {
        String content = null;
        String fields[] = null;
        String sensorConnector = null;
        String sensorType = null;
        String sensorValue = null;

        content = data;
        fields = content.split(DELIMITER);
        sensorConnector = fields[0];
        sensorType = fields[1];
        sensorValue = fields[2];
        sendEvent(sourceAddress + ":" + sensorConnector, sensorType, sensorValue);
    }

    private void sendEvent(String objectAddress, String eventProperty, String eventValue) {
        ProtocolRead event = new ProtocolRead(this, "openpicus-grove-system", objectAddress);
        event.addProperty("openpicus-grove-system.sensor.type", eventProperty);
        event.addProperty("openpicus-grove-system.sensor.value", eventValue);
        if (eventProperty.equalsIgnoreCase("temperature")) {
            event.addProperty("object.class", "Thermometer");
        } else if (eventProperty.equalsIgnoreCase("luminosity")) {
            event.addProperty("object.class", "Light Sensor");
        }
        // EXTEND WITH MORE SENSORS
        event.addProperty("object.name", objectAddress);
        //publish the event on the messaging bus
        this.notifyEvent(event);
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
