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
/*
 * @author Mauro Cicolella 
 */
package com.freedomotic.plugins.devices.mqttclient;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.util.HashSet;
import java.util.Set;

public class MqttClient4FD extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(MqttClient4FD.class.getName());

    private final String BROKER_URL = configuration.getStringProperty("broker-url", "tcp://test.mosquitto.org:1883");
    private final String CLIENT_ID = configuration.getStringProperty("client-id", "freedomotic");
    private final String AUTHENTICATION_ENABLED = configuration.getStringProperty("authentication-enabled", "false");
    private final String USERNAME = configuration.getStringProperty("username", "admin");
    private final String PASSWORD = configuration.getStringProperty("password", "admin");
    private final String SET_CLEAN_SESSION = configuration.getStringProperty("set-clean-session", "true");
    private final Integer SET_KEEP_ALIVE_INTERVAL = configuration.getIntProperty("set-keep-alive-interval", 600);

    // events publishing configuration
    private final String ENABLE_EVENTS_PUBLISHING = configuration.getStringProperty("enable-events-publishing", "true");
    private final Integer MQTT_PUBLISH_QOS = configuration.getIntProperty("mqtt-publish-qos", 0);
    private final Integer MQTT_SUBSCRIBE_QOS = configuration.getIntProperty("mqtt-subscribe-qos", 0);
    private final String DATA_FORMAT = configuration.getStringProperty("data-format", "raw");
    private final String OBJECT_CHANGED_EVENT_TOPIC = "/freedomotic/events/objectChanged";
    private final String ZONE_CHANGED_EVENT_TOPIC = "/freedomotic/events/zoneChanged";
    private final String PLUGIN_CHANGED_EVENT_TOPIC = "/freedomotic/events/pluginChanged";
    private final String MESSAGE_EVENT_TOPIC = "/freedomotic/events/message";

    private Boolean connected = false;
    private final Set<String> topics;
    private Mqtt mqttClient = null;

    public MqttClient4FD() {
        super("MQTT Client", "/mqtt-client/mqtt-client-manifest.xml");
        this.topics = new HashSet<>();
        setPollingWait(-1); //onRun() disabled
    }

    @Override
    protected void onShowGui() {
        // no GUI supported
    }

    @Override
    protected void onHideGui() {
        // no GUI supported
    }

    @Override
    protected void onRun() {
        // polling disabled
    }

    @Override
    protected void onStart() throws PluginStartupException {
        loadTopicsToSubscribe();
        mqttClient = new Mqtt(this);
        connected = mqttClient.startClient(BROKER_URL, CLIENT_ID, SET_CLEAN_SESSION, SET_KEEP_ALIVE_INTERVAL, AUTHENTICATION_ENABLED, USERNAME, PASSWORD);
        if (connected) {
            setDescription("Connected to " + BROKER_URL);
            LOG.info("Connected to MQTT broker {}", BROKER_URL);
            subscribeTopics();
        } else {
            throw new PluginStartupException("Not connected. Please check");
        }

        // events listeners
        if ("true".equalsIgnoreCase(ENABLE_EVENTS_PUBLISHING)) {
            addEventListener("app.event.sensor.object.behavior.change");
            addEventListener("app.event.sensor.environment.zone.change");
            addEventListener("app.event.sensor.plugin.change");
            addEventListener("app.event.sensor.messages.callout");
        }

    }

    @Override
    protected void onStop() {
        if (connected) {
            mqttClient.disconnect();
            setDescription("Mqtt Client disconnected");
        }
        LOG.info("Mqtt Client plugin stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        String topic = c.getProperty("mqtt.topic");
        String message = c.getProperty("mqtt.message");
        Integer subQoS = Integer.parseInt(c.getProperty("mqtt.sub-qos"));
        Integer pubQoS = Integer.parseInt(c.getProperty("mqtt.pub-qos"));
        mqttClient.publish(topic, message, subQoS, pubQoS);
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        String message = "";

        if ("true".equalsIgnoreCase(ENABLE_EVENTS_PUBLISHING)) {
           
            switch (DATA_FORMAT) {

                case "json":
                    break;

                default:
                    message = event.getPayload().getStatements().toString();
            }

            //publish events on specific topics
            if (event instanceof ObjectHasChangedBehavior) {
                mqttClient.publish(OBJECT_CHANGED_EVENT_TOPIC, message, MQTT_SUBSCRIBE_QOS, MQTT_PUBLISH_QOS);
            } else if (event instanceof ZoneHasChanged) {
                mqttClient.publish(ZONE_CHANGED_EVENT_TOPIC, message, MQTT_SUBSCRIBE_QOS, MQTT_PUBLISH_QOS);
            } else if (event instanceof PluginHasChanged) {
                mqttClient.publish(PLUGIN_CHANGED_EVENT_TOPIC, message, MQTT_SUBSCRIBE_QOS, MQTT_PUBLISH_QOS);
            } else if (event instanceof MessageEvent) {
                mqttClient.publish(MESSAGE_EVENT_TOPIC, message, MQTT_SUBSCRIBE_QOS, MQTT_PUBLISH_QOS);
            }

        }
    }

    /**
     * Load all topics in <tuples></tuples> section of plugin manifest file.
     *
     */
    private void loadTopicsToSubscribe() {
        for (int i = 0; i < configuration.getTuples().size(); i++) {
            topics.add(configuration.getTuples().getProperty(i, "topic-name"));
        }
    }

    /**
     * Subscribes to all loaded topics.
     *
     */
    public void subscribeTopics() {
        topics.forEach((topic) -> {
            mqttClient.subscribeTopic(topic);
        });
    }
}
