/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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

import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.AppConfig;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.Profiler;
import com.freedomotic.reactions.Command;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import org.apache.activemq.command.ActiveMQQueue;

/**
 * Bus services implementation.
 * <p>
 * It is life cycle managed, see {@link LifeCycle}
 *
 * @author Freedomotic Team
 *
 */
class BusServiceImpl extends LifeCycle implements BusService {

    private static final Logger LOG = Logger.getLogger(BusServiceImpl.class.getName());

    //private AppConfig config;
    private BusBroker brokerHolder;
    private BusConnection connectionHolder;
    private DestinationRegistry destination;
    private Session receiveSession;
    private Session sendSession;
    private Session unlistenedSession;
    protected MessageProducer messageProducer;
    
    @Inject
    public BusServiceImpl(){
        //this.config = config;
        if (BootStatus.getCurrentStatus() == BootStatus.STOPPED) {
           init();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void start() throws Exception {

        BootStatus.setCurrentStatus(BootStatus.BOOTING);

        //config = Freedomotic.INJECTOR.getInstance(AppConfig.class);

        brokerHolder = new BusBroker();
        brokerHolder.init();

        connectionHolder = new BusConnection();
        connectionHolder.init();

        destination = new DestinationRegistry(this);

        receiveSession = createSession();
        // an unlistened session
        unlistenedSession = createSession();

        sendSession = createSession();
        // null parameter creates a producer with no specified destination
        messageProducer = createMessageProducer();
        
        if (sendSession == null){
            throw new IllegalStateException("Messaging bus has not yet a valid send session");
        }

        BootStatus.setCurrentStatus(BootStatus.STARTED);
    }

    private MessageProducer createMessageProducer() throws JMSException {

        // null parameter creates a producer with no specified destination
        final MessageProducer createdProducer = sendSession.createProducer(null);

        // configure
        createdProducer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        //final int tiemToLive = config.getIntProperty("KEY_MESSAGES_TTL", 1000);
        //createProducer.setTimeToLive(tiemToLive);

        return createdProducer;
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    protected void stop() throws Exception {

        BootStatus.setCurrentStatus(BootStatus.STOPPING);

        messageProducer.close();
        closeSession(sendSession);

        closeSession(unlistenedSession);
        closeSession(receiveSession);

        connectionHolder.destroy();
        brokerHolder.destroy();

        BootStatus.setCurrentStatus(BootStatus.STOPPED);
    }

    /**
     * {@inheritDoc}
     */
    // TODO Freedomotic.java needs this method publicly visible. A whole repackage is needed.  
    @Override
    public void destroy() {
        super.destroy();
    }

    /**
     * {@inheritDoc}
     */
    // TODO Freedomotic.java needs this method publicly visible. A whole repackage is needed.  
    @Override
    public void init() {
        super.init();
    }

    private void closeSession(final Session session) throws Exception {

        session.close();
    }

    private Session createSession() throws Exception {

        return connectionHolder.createSession();
    }

    private MessageProducer getMessageProducer() {

        return messageProducer;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public BusDestination registerCommandQueue(String queueName)
            throws JMSException {

        return destination.registerCommandQueue(queueName);

    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public BusDestination registerEventQueue(String queueName)
            throws JMSException {

        return destination.registerEventQueue(queueName);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public BusDestination registerTopic(String queueName) throws JMSException {

        return destination.registerTopic(queueName);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Session getReceiveSession() {
        return receiveSession;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Session getSendSession() {
        return sendSession;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    public Session getUnlistenedSession() {
        return unlistenedSession;
    }

    private ObjectMessage createObjectMessage() throws JMSException {
        return getSendSession().createObjectMessage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reply(Command command, Destination destination, String correlationID) {
        try {
            ObjectMessage msg = createObjectMessage();
            msg.setObject(command);
            msg.setJMSCorrelationID(correlationID);
            msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);
            getMessageProducer().send(destination, msg);
            Profiler.incrementSentReplies();
        } catch (JMSException jmse) {
            LOG.severe(Freedomotic.getStackTraceInfo(jmse));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Command send(final Command command) {
        if (command == null) {
            throw new IllegalArgumentException("Cannot send a null command");
        }

        try {

            ObjectMessage msg = createObjectMessage();

            msg.setObject(command);
            msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);

            if (command.getReceiver() == null || command.getReceiver().isEmpty()) {
                throw new IllegalArgumentException("Cannot send command '" + command + "', the receiver channel is not specified");
            }
            Queue destination = new ActiveMQQueue(command.getReceiver());

            if (command.getReplyTimeout() > 0) {

                // we have to wait an execution reply for an hardware device or
                // an external client
                final Session unlistenedSession = this.getUnlistenedSession();
                TemporaryQueue temporaryQueue = unlistenedSession
                        .createTemporaryQueue();

                msg.setJMSReplyTo(temporaryQueue);

                // a temporary consumer on a temporary queue
                MessageConsumer temporaryConsumer = unlistenedSession
                        .createConsumer(temporaryQueue);

                final MessageProducer messageProducer = this.getMessageProducer();
                messageProducer.send(destination, msg);

                Profiler.incrementSentCommands();

                // the receive() call is blocking
                LOG.config("Send and await reply to command '"
                        + command.getName() + "' for "
                        + command.getReplyTimeout() + "ms");

                Message jmsResponse = temporaryConsumer.receive(command.getReplyTimeout());

                //cleanup after receiving
                temporaryConsumer.close();
                //TODO: commented as sometimes genenerates a "cannot publish on deleted queue" exception
                //check n the documentation if a temporary queue with no consumers
                //is automatically deleted
                //TODO: enable this: temporaryQueue.delete();

                if (jmsResponse != null) {
                    // TODO unchecked cast!
                    ObjectMessage objMessage = (ObjectMessage) jmsResponse;

                    // a command is sent, we expect a command as reply
                    // TODO unchecked cast!
                    Command reply = (Command) objMessage.getObject();

                    LOG.config("Reply to command '"
                            + command.getName() + "' is received. Result property inside this command is "
                            + reply.getProperty("result")
                            + ". It is used to pass data to the next command, can be empty or even null.");

                    Profiler.incrementReceivedReplies();

                    return reply;

                } else {

                    LOG.config("Command '" + command.getName()
                            + "' timed out after " + command.getReplyTimeout()
                            + "ms");

                    Profiler.incrementTimeoutedReplies();
                }

                // mark as failed
                command.setExecuted(false);

                // returns back the original inaltered command
                return command;

            } else {

                // send the message immediately without creating temporary
                // queues and consumers on it
                // this increments perfornances if no reply is expected
                final MessageProducer messageProducer = this.getMessageProducer();
                LOG.log(Level.CONFIG, "Send command ''{0}'' (no reply expected)", command.getName());
                messageProducer.send(destination, msg);

                Profiler.incrementSentCommands();

                command.setExecuted(true);

                // always say it is executed (it's not sure but the caller is
                // not interested: best effort)
                return command;
            }
        } catch (JMSException ex) {

            LOG.severe(Freedomotic.getStackTraceInfo(ex));

            command.setExecuted(false);

            return command;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(EventTemplate ev) {

        send(ev, ev.getDefaultDestination());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void send(final EventTemplate ev, final String to) {

        // TODO should this null check be here?
        if (ev != null) {

            try {

                ObjectMessage msg = createObjectMessage();

                msg.setObject(ev);
                msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);

                // a consumer consumes on
                // Consumer.A_PROGRESSIVE_INTEGER_ID.VirtualTopic.
                BusDestination busDestination = this.registerTopic(to);
                Destination tmpTopic = busDestination.getDestination();

                final MessageProducer messageProducer = this.getMessageProducer();
                messageProducer.send(tmpTopic, msg);

                Profiler.incrementSentEvents();

            } catch (JMSException ex) {

                LOG.severe(Freedomotic.getStackTraceInfo(ex));
            }
        }
    }
}
