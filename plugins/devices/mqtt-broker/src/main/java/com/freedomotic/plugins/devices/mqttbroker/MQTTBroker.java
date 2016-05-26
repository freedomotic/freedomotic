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
/**
 * @author Mauro Cicolella
 */
package com.freedomotic.plugins.devices.mqttbroker;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.settings.Info;
import io.moquette.BrokerConstants;
import io.moquette.interception.AbstractInterceptHandler;
import io.moquette.interception.InterceptHandler;
import io.moquette.interception.messages.InterceptPublishMessage;
import io.moquette.server.Server;
import io.moquette.server.config.ClasspathConfig;
import io.moquette.server.config.IConfig;
import io.moquette.server.config.MemoryConfig;
import java.io.File;
import java.io.IOException;
import static java.util.Arrays.asList;
import java.util.List;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Mauro Cicolella
 */
public class MQTTBroker
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(MQTTBroker.class.getName());
    private final String BROKER_HOST = configuration.getStringProperty("broker-host", "0.0.0.0");
    private final Integer BROKER_PORT = configuration.getIntProperty("broker-port", 1883);
    private Server mqttBroker;
    private IConfig config;
    private List<? extends InterceptHandler> userHandlers;

    /**
     *
     */
    public MQTTBroker() {
        super("MQTT broker", "/mqtt-broker/mqtt-broker-manifest.xml");
        setPollingWait(-1); // onRun() disabled
    }

    @Override
    protected void onShowGui() {
    }

    @Override
    protected void onHideGui() {
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() throws PluginStartupException {

        mqttBroker = new Server();
        Properties props = new Properties();

        // get properties from manifest file
        props.setProperty(BrokerConstants.PORT_PROPERTY_NAME, Integer.toString(BROKER_PORT));
        props.setProperty(BrokerConstants.HOST_PROPERTY_NAME, BROKER_HOST);

        props.setProperty(BrokerConstants.PASSWORD_FILE_PROPERTY_NAME, Info.PATHS.PATH_DEVICES_FOLDER + "/mqtt-broker/config/password_file.conf");
        props.setProperty(BrokerConstants.PERSISTENT_STORE_PROPERTY_NAME, Info.PATHS.PATH_DEVICES_FOLDER + "/mqtt-broker/config/moquette_store.mapdb");

        config = new MemoryConfig(props);
        userHandlers = asList(new PublisherListener());

        try {
            mqttBroker.startServer(config, userHandlers);
        } catch (IOException ex) {
            throw new PluginStartupException("Plugin can't start for an IOException.", ex);
        }
        setDescription("MQTT broker listening to " + config.getProperty(BrokerConstants.HOST_PROPERTY_NAME) + ":" + config.getProperty(BrokerConstants.PORT_PROPERTY_NAME));
        LOG.info("MQTT broker started");

        // bind a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                LOG.info("Stopping MQTT broker");
                mqttBroker.stopServer();
                LOG.info("MQTT broker stopped");
            }
        });
    }

    @Override
    protected void onStop() {
        mqttBroker.stopServer();
        setDescription("MQTT broker stopped");
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

    /**
     *  Sends a Freedomotic event.
     * @param topic mqtt publishing topic
     * @param value  message payload
     */
    private void sendEvent(String topic, String value) {
        ProtocolRead event = new ProtocolRead(this, "mqtt-broker", topic);
        event.addProperty("mqtt.topic", topic);
        event.addProperty("mqtt.value", value);
        notifyEvent(event);
    }

    /**
     * This class is used to listen to messages on topics and manage them.
     *
     */
    class PublisherListener extends AbstractInterceptHandler {

        @Override
        public void onPublish(InterceptPublishMessage msg) {
            String topic = msg.getTopicName();
            String value = new String(msg.getPayload().array());
            LOG.info("Received on topic: [{}] content: [{}]", topic, value);
            sendEvent(topic, value);
        }
    }
}
