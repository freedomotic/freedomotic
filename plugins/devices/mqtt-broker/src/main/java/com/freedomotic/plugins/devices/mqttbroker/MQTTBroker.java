/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
/**
 * @author Mauro Cicolella <mcicolella@libero.it>
 */
package com.freedomotic.plugins.devices.mqttbroker;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.settings.Info;
import java.io.File;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dna.mqtt.moquette.server.Server;

public class MQTTBroker
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTBroker.class.getName());
    private static Server serverMqtt;

    public MQTTBroker() {
        super("MQTT broker", "/mqtt-broker/mqtt-broker-manifest.xml");
        setPollingWait(-1); // onRun() disabled
    }

    @Override
    protected void onShowGui() {
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() throws PluginStartupException {
        serverMqtt = new Server();
        try {
            serverMqtt.startServer(new File(Info.PATHS.PATH_DEVICES_FOLDER + "/mqtt-broker/config/moquette.conf"));
        } catch (IOException ex) {
            throw new PluginStartupException("Plugin can't start for an IOException.", ex);
           }
        setDescription("MQTT broker started");
        LOG.info("MQTT broker started");
    }

    @Override
    protected void onStop() {
        serverMqtt.stopServer();
        LOG.info("MQTT broker stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
