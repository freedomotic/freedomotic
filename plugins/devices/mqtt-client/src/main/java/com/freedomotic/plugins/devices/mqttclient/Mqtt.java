/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.plugins.devices.mqttclient;

import com.freedomotic.app.Freedomotic;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomotic.events.ProtocolRead;

/**
 * @author Mauro Cicolella
 */
public class Mqtt implements MqttCallback {

    private static final Logger LOG = LoggerFactory.getLogger(Mqtt.class.getName());

    MqttClient4FD pluginRef = null;
    MqttClient myClient;
    MqttConnectOptions connectionOptions;
    MemoryPersistence persistence = new MemoryPersistence();

    Mqtt(MqttClient4FD pluginRef) {
        this.pluginRef = pluginRef;
    }

    /**
     *
     * Class entry point
     *
     * @param brokerUrl
     * @param clientID
     * @param setCleanSession
     * @param setKeepAliveInterval
     * @param authenticationEnabled
     * @param username
     * @param password
     * @return
     *
     */
    public boolean startClient(String brokerUrl, String clientID, String setCleanSession, Integer setKeepAliveInterval, String authenticationEnabled, String username, String password) {

        MqttConnectOptions connectionOptions = new MqttConnectOptions();

        connectionOptions.setCleanSession(Boolean.parseBoolean(setCleanSession));
        connectionOptions.setKeepAliveInterval(setKeepAliveInterval);
        // authentication requires username and password
        if (authenticationEnabled.equalsIgnoreCase("true")) {
            connectionOptions.setUserName(username);
            connectionOptions.setPassword(password.toCharArray());
        }

        // Connection to Broker
        try {
            myClient = new MqttClient(brokerUrl, clientID, persistence);
            myClient.setCallback(this);
            myClient.connect(connectionOptions);
            return true;
        } catch (MqttException e) {
            LOG.error("Unable to connect to MQTT broker {} for {}", brokerUrl, e.getMessage());
            return false;
        }
    }

    /**
     * Subscribes to a topic.
     * 
     * @param topic
     */
    public void subscribeTopic(String topic) {
        try {
            myClient.subscribe(topic, 0);
            LOG.info("Subscribed MQTT topic \"{}\"", topic);
        } catch (MqttException ex) {
            LOG.error("Unable to subscribe MQTT topic \"{}\" for {}", topic, ex);
        }
    }

    /**
     * Publishes a message on a topic.
     * 
     * @param topic
     * @param message
     * @param subQoS
     * @param pubQoS
     */
    public void publish(String topic, String message, int subQoS, int pubQoS) {

        MqttMessage messageToPublish = new MqttMessage(message.getBytes());
        messageToPublish.setQos(pubQoS);
        messageToPublish.setRetained(false);
        try {
            myClient.publish(topic, messageToPublish);
        } catch (MqttException ex) {
            LOG.error("Unable to publish message: \"{}\" to \"{}\"", message, topic);
        }
    }

    /**
     * Disconnects from MQTT broker.
     * 
     */
    public void disconnect() {
        try {
            myClient.disconnect();
        } catch (MqttException e) {
            LOG.error(Freedomotic.getStackTraceInfo(e));
        }
    }

    /**
     * This callback is invoked when a message has been publish on the topic.
     * 
     * @param topic
     * @param message
     * @throws Exception
     */
    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String protocol = "mqtt-client";
        String address = topic;
        String payload = new String(message.getPayload());

        LOG.info("Received message \"{}\" on topic \"{}\"", payload, topic);
        // create and notify a Freedomotic event 
        // the object address is equal to "topic"
        ProtocolRead event = new ProtocolRead(this, protocol, topic);
        event.addProperty("mqtt.message", payload);
        //publish the event on the messaging bus
        pluginRef.notifyEvent(event);
    }

    /**
     * This callback is invoked when a message delivery is completed.
     * 
     * @param imdt
     */
    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        try {
            LOG.info("Message \"{}\" published on {} topic", imdt.getMessage().toString(), imdt.getTopics());
        } catch (MqttException ex) {
            LOG.error(ex.getMessage());
        }
    }

    /**
     *
     * This callback is invoked upon loosing the MQTT connection. The client
     * tries to reconnect.
     *
     * @param cause
     */
    @Override
    public void connectionLost(Throwable cause) {
        LOG.error("Connection to MQTT broker lost for {}", cause.getCause());
        while (!myClient.isConnected()) {
            try {
                LOG.info("Reconnecting to MQTT broker in progress ...");
                myClient.connect(connectionOptions);
            } catch (MqttException e) {
                LOG.error("Unable to connect to MQTT broker {}", myClient.getServerURI());
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                LOG.error(ex.getMessage(), ex);
                Thread.currentThread().interrupt();
            }
        }
        LOG.info("Reconnected to MQTT broker");
        pluginRef.subscribeTopics();
    }
}
