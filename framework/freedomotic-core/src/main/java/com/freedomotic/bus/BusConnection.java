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
package com.freedomotic.bus;

import com.freedomotic.settings.AppConfig;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Bus connection holder.
 * <p>
 * It is life cycle managed, see {@link LifeCycle}
 *
 * @author Freedomotic Team
 *
 */
class BusConnection extends LifeCycle {

    private static final Logger LOG = LoggerFactory.getLogger(BusConnection.class.getName());

    // FIXME LCG get out from here!
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_USER = "user";

    private Connection connection;

    @Inject
    private AppConfig config;

    private ActiveMQConnectionFactory factory;

    private ActiveMQConnectionFactory createFactory() {

        // connect to the embedded broker defined above
        String P2P_BROKER_URL = config.getStringProperty("BROKER_PROTOCOL", Info.MESSAGING.BROKER_DEFAULT_PROTOCOL)
                + "://"
                + config.getStringProperty("P2P_CLUSTER_NAME", Info.MESSAGING.BROKER_DEFAULT_CLUSTER_NAME)
                + "/" + Info.MESSAGING.BROKER_DEFAULT_UUID;
        LOG.info("P2P Connection on " + P2P_BROKER_URL);
        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(P2P_BROKER_URL);

        // tuned for performances
        // http://activemq.apache.org/performance-tuning.html
        factory.setUseAsyncSend(true);
        factory.setAlwaysSessionAsync(true);
        factory.setObjectMessageSerializationDefered(true);
        factory.setCopyMessageOnSend(false);

        return factory;
    }

    /**
     * Creates a Session object.
     *
     * @return the Session
     *
     * @throws JMSException
     */
    protected Session createSession() throws JMSException {

        return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void start() throws Exception {

        LOG.info("Creating connection factory");
        factory = createFactory();

        LOG.info("Creating connection");
        connection = factory.createConnection(DEFAULT_USER, DEFAULT_PASSWORD);

        LOG.info("Starting connection");
        connection.start();

    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void stop() throws Exception {

        LOG.info("Stopping and closing connection");
        connection.stop();
        connection.close();
    }
}
