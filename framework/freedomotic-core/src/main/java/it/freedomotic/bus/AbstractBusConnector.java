/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.bus;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.Info;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;

/**
 *
 * @author enrico
 */
public class AbstractBusConnector {

    protected static final String DEFAULT_USER = "user";
    protected static final String DEFAULT_PASSWORD = "password";
    protected static Connection connection;
    protected static Session sendSession;
    protected static Session receiveSession;
    private static final BrokerService BROKER = new BrokerService();
    protected static Session unlistenedSession;

    public AbstractBusConnector() {

        connect(Info.BROKER_DEFAULT, DEFAULT_USER, DEFAULT_PASSWORD);
    }

    private void connect(String brokerString, String username, String password) {
        //create a connection
        if (connection == null) { //not already connected to the bus
            try {
                Freedomotic.logger.info("Creating new messaging broker");
                //create an embedded messaging broker
                //BROKER.setBrokerName("freedomotic");
                //use always 0.0.0.0 not localhost. localhost allows connections 
                //only on the local machine not from LAN IPs
                try {
                    BROKER.addConnector(Info.BROKER_STOMP);
//                //websocket connector for javascript apps
                    BROKER.addConnector(Info.BROKER_WEBSOCKET);
                } catch (Exception exception) {
                    Freedomotic.logger.warning("Broker connector not started due to " + exception.getLocalizedMessage());
                }
                BROKER.setPersistent(false); //we don't need to save messages on disk
                BROKER.setUseJmx(false);
                //start the broker
                BROKER.start();

                //connect to the embedded broker defined above
                ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(Info.BROKER_DEFAULT);
                //tuned for performances http://activemq.apache.org/performance-tuning.html
                factory.setUseAsyncSend(true);
                factory.setOptimizeAcknowledge(true);
                factory.setAlwaysSessionAsync(true);
                factory.setObjectMessageSerializationDefered(true);
                factory.setCopyMessageOnSend(false);

                connection = factory.createConnection(username, password);
                sendSession = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
                receiveSession = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
                unlistenedSession = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE); //an unlistened session
                new StompDispatcher(); //just for testing, don't mind it
                connection.start();
            } catch (JMSException jMSException) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jMSException));
            } catch (Exception ex) {
                Logger.getLogger(AbstractBusConnector.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void disconnect() {
        try {
            BROKER.stop();
        } catch (Exception ex) {
            Logger.getLogger(AbstractBusConnector.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
