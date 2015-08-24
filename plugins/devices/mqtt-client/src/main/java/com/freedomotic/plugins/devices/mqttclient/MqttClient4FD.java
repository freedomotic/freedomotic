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
/*
 * @author Mauro Cicolella <mcicolella@libero.it>
 */
package com.freedomotic.plugins.devices.mqttclient;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Logger;

public class MqttClient4FD
        extends Protocol {

    public static final Logger LOG = Logger.getLogger(MqttClient4FD.class.getName());
    private String BROKER_URL = configuration.getStringProperty("broker-url", "tcp://test.mosquitto.org:1883");
    private String CLIENT_ID = configuration.getStringProperty("client-id", "freedomotic");
    private String AUTHENTICATION_ENABLED = configuration.getStringProperty("authentication-enabled", "false");
    private String USERNAME = configuration.getStringProperty("username", "admin");
    private String PASSWORD = configuration.getStringProperty("password", "admin");
    private String SET_CLEAN_SESSION = configuration.getStringProperty("set-clean-session", "true");
    private Integer SET_KEEP_ALIVE_INTERVAL = configuration.getIntProperty("set-keep-alive-interval", 600);
    private Boolean connected = false;
    private Mqtt mqttClient = null;

    public MqttClient4FD() {
        //every plugin needs a name and a manifest XML file
        super("MQTT Client", "/mqtt-client/mqtt-client-manifest.xml");
        setPollingWait(-1); //onRun() disabled
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
    protected void onStart() {
        mqttClient = new Mqtt(this);
        connected = mqttClient.startClient(BROKER_URL, CLIENT_ID, SET_CLEAN_SESSION, SET_KEEP_ALIVE_INTERVAL, AUTHENTICATION_ENABLED, USERNAME, PASSWORD);
        if (connected) {
            setDescription("Connected to " + BROKER_URL);
            mqttClient.subscribeTopic(configuration.getStringProperty("topic", "true"));
            // this message is used in debug mode - please remove
            mqttClient.publish(configuration.getStringProperty("topic", "true"), "obj1:1", 0, 0);
        } else {
            // plugin stopped
            this.stop();
        }
    }

    @Override
    protected void onStop() {
        if (connected) {
            mqttClient.disconnect();
            setDescription("Mqtt Client disconnected");
        }
        LOG.info("Mqtt Client stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        String topic = c.getProperty("topic");
        String message = c.getProperty("message");
        Integer subQoS = Integer.parseInt(c.getProperty("sub-qos"));
        Integer pubQoS = Integer.parseInt(c.getProperty("pub-qos"));
        mqttClient.publish(topic, message, subQoS, pubQoS);
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
