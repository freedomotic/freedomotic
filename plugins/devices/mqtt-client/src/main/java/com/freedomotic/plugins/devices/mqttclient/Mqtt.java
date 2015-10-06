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
package com.freedomotic.plugins.devices.mqttclient;

/**
 *
 * @author Mauro Cicolella <mcicolella@libero.it>
 */
import com.freedomotic.events.ProtocolRead;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class Mqtt implements MqttCallback {

    MqttClient4FD pluginRef = null;
    MqttClient myClient;
    MqttConnectOptions connectionOptions;
    MemoryPersistence persistence = new MemoryPersistence();

    Mqtt(MqttClient4FD pluginRef) {
        this.pluginRef = pluginRef;
    }

    /**
     *
     * startClient Class entry point
     *
     * @param brokeUrl
     * @param clientID
     * @param setCleanSession
     * @param setKeepAliveInterval
     * @param authenticationEnabled
     * @param username
     * @param password
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
            MqttClient4FD.LOG.severe("Unable to connect to broker " + brokerUrl + " for " + e.getMessage());
            return false;
        }
    }

    public void subscribeTopic(String topic) {
        try {
            myClient.subscribe(topic, 0);
        } catch (MqttException ex) {
            MqttClient4FD.LOG.severe("Unable to subscribe topic + " + topic + " for reason " + ex.getLocalizedMessage());
        }
    }

    public void publish(String topic, String message, int subQoS, int pubQoS) {

        MqttMessage messageToPublish = new MqttMessage(message.getBytes());
        messageToPublish.setQos(pubQoS);
        messageToPublish.setRetained(false);
        try {
            myClient.publish(topic, messageToPublish);
        } catch (MqttException ex) {
            MqttClient4FD.LOG.severe("Unable to publish message: " + message + " to " + topic + " for " + ex.getMessage());
        }
    }

    public void disconnect() {
        try {
            myClient.disconnect();
        } catch (Exception e) {
            MqttClient4FD.LOG.severe(e.getLocalizedMessage());
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String protocol = "mqtt-client";
        String address = null;
        String value = null;

        System.out.println("-------------------------------------------------");
        System.out.println("| Topic:" + topic);
        System.out.println("| Message: " + new String(message.getPayload()));
        System.out.println("-------------------------------------------------");
        // create and notify a freedomotic event based on application logic extracting data from mqtt message
        // the event must contain some info as freedomotic object address, value
        // in this example we consider a message in the format objaddress:value
        String adr[] = new String(message.getPayload()).split(":");
        address = adr[0];
        value = adr[1];
        ProtocolRead event = new ProtocolRead(this, protocol, address);
        event.addProperty("value", value);
        //publish the event on the messaging bus
        pluginRef.notifyEvent(event);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt) {
        MqttClient4FD.LOG.info("Message published");
    }

    /**
     *
     * connectionLost This callback is invoked upon losing the MQTT connection.
     *
     */
    @Override
    public void connectionLost(Throwable cause) {
        MqttClient4FD.LOG.severe("Connection to Mqtt broker lost for " + cause.getCause());
        MqttClient4FD.LOG.severe("Reconnecting in progress ...");
        while (!myClient.isConnected()) {
            try {
                myClient.connect(connectionOptions);
            } catch (MqttException e) {
                MqttClient4FD.LOG.severe("Unable to connect to broker " + myClient.getServerURI() + " for " + e.getMessage());
            }
            // set a delay before retrying
        }

    }
}
