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

import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.Profiler;
import com.freedomotic.reactions.Command;
import com.freedomotic.settings.AppConfig;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import javax.jms.Topic;
import org.apache.activemq.command.ActiveMQQueue;

/**
 * Bus services implementation.
 * <p>
 * It is life cycle managed, see {@link LifeCycle}
 *
 * @author Freedomotic Team
 *
 */
final class BusServiceImpl extends LifeCycle implements BusService {

    private static final Logger LOG = LoggerFactory.getLogger(BusServiceImpl.class.getName());

    //private AppConfig config;
    private BusBroker brokerHolder;
    private BusConnection connectionHolder;
    private Session receiveSession;
    private Session sendSession;
    private Session unlistenedSession;
    private AppConfig conf;
    private Injector injector;
    protected MessageProducer messageProducer;

    @Inject
    public BusServiceImpl(AppConfig config, Injector inj) {
        //this.config = config;
        if (BootStatus.getCurrentStatus() == BootStatus.STOPPED) {
            conf = config;
            injector = inj;
            init();
            if (sendSession == null) {
                throw new IllegalStateException("Messaging bus has not yet a valid send session");
            }
        }
        LOG.info("Messaging bus is " + BootStatus.getCurrentStatus().name());
    }

    /**
     * {@inheritDoc}
     *
     * @throws java.lang.Exception
     */
    @Override
    protected void start() throws Exception {

        BootStatus.setCurrentStatus(BootStatus.BOOTING);

        brokerHolder = new BusBroker();
        brokerHolder.init();

        connectionHolder = injector.getInstance(BusConnection.class);
        connectionHolder.init();

        receiveSession = createSession();
        // an unlistened session
        unlistenedSession = createSession();

        sendSession = createSession();
        // null parameter creates a producer with no specified destination
        messageProducer = createMessageProducer();

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
    @Override
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

    @Override
    public Session createSession() throws Exception {
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
        if (destination == null) {
            throw new IllegalArgumentException("Null reply destination "
                    + "for command " + command.getName() + " "
                    + "(reply timeout: " + command.getReplyTimeout() + ")");
        }
        try {
            ObjectMessage msg = createObjectMessage();
            msg.setObject(command);
            msg.setJMSDestination(destination);
            msg.setJMSCorrelationID(correlationID);
            msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);
            LOG.info("Sending reply to command ''{}'' on {}", new Object[]{command.getName(), msg.getJMSDestination()});
            getMessageProducer().send(destination, msg); //Always pass the destination, otherwise it complains
            Profiler.incrementSentReplies();
        } catch (JMSException jmse) {
            LOG.error(Freedomotic.getStackTraceInfo(jmse));
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
        if (command.getReceiver() == null || command.getReceiver().isEmpty()) {
            throw new IllegalArgumentException("Cannot send command '" + command + "', the receiver channel is not specified");
        }

        LOG.info("Sending command ''{}'' to destination ''{}'' with reply timeout {}", new Object[]{command.getName(), command.getReceiver(), command.getReplyTimeout()});

        try {
            ObjectMessage msg = createObjectMessage();
            msg.setObject(command);
            msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);

            Queue currDestination = new ActiveMQQueue(command.getReceiver());
            if (command.getReplyTimeout() > 0) {
                return sendAndWaitReply(command, currDestination, msg);
            } else {
                return sendAndForget(command, currDestination, msg);
            }
        } catch (JMSException ex) {
            LOG.error(Freedomotic.getStackTraceInfo(ex));
            command.setExecuted(false);
            return command;
        }
    }

    private Command sendAndForget(final Command command, Queue currDestination, ObjectMessage msg) throws JMSException {
        // send the message immediately without creating temporary
        // queues and consumers on it
        // this increments perfornances if no reply is expected
        final MessageProducer messageProducer = this.getMessageProducer();
        messageProducer.send(currDestination, msg);

        Profiler.incrementSentCommands();

        command.setExecuted(true);

        // always say it is executed (it's not sure but the caller is
        // not interested: best effort)
        return command;
    }

    private Command sendAndWaitReply(final Command command, Queue currDestination, ObjectMessage msg) throws JMSException {
        // we have to wait an execution reply for an hardware device or
        // an external client
        final Session currUnlistenedSession = this.getUnlistenedSession();
        TemporaryQueue temporaryQueue = currUnlistenedSession
                .createTemporaryQueue();

        msg.setJMSReplyTo(temporaryQueue);

        // a temporary consumer on a temporary queue
        MessageConsumer temporaryConsumer = currUnlistenedSession
                .createConsumer(temporaryQueue);

        final MessageProducer currMessageProducer = this.getMessageProducer();
        currMessageProducer.send(currDestination, msg);

        Profiler.incrementSentCommands();

        // the receive() call is blocking
        LOG.info("Send and await reply to command ''{}'' for {}ms",
                new Object[]{command.getName(), command.getReplyTimeout()});

        Message jmsResponse = temporaryConsumer.receive(command.getReplyTimeout());

        //cleanup after receiving
        //temporaryConsumer.close();
        //temporaryQueue.delete();
        //TODO: commented as sometimes genenerates a "cannot publish on deleted queue" exception
        //check n the documentation if a temporary queue with no consumers
        //is automatically deleted
        if (jmsResponse != null) {
            // TODO unchecked cast!
            ObjectMessage objMessage = (ObjectMessage) jmsResponse;

            // a command is sent, we expect a command as reply
            // TODO unchecked cast!
            Command reply = (Command) objMessage.getObject();

            LOG.info("Reply to command '"
                    + command.getName() + "' is received. Result property inside this command is "
                    + reply.getProperty("result")
                    + ". It is used to pass data to the next command, can be empty or even null.");

            Profiler.incrementReceivedReplies();

            return reply;

        } else {

            LOG.info("Command '" + command.getName()
                    + "' timed out after " + command.getReplyTimeout()
                    + "ms");

            Profiler.incrementTimeoutedReplies();
        }

        // mark as failed
        command.setExecuted(false);

        // returns back the original inaltered command
        return command;
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
        //LOG.log(Level.INFO, "Sending event ''{}'' to destination ''{}''", new Object[]{ev.toString(), to});
        if (ev == null) {
            throw new IllegalArgumentException("Cannot send a null event");
        }

        try {

            ObjectMessage msg = createObjectMessage();

            msg.setObject(ev);
            msg.setStringProperty("provenance", Freedomotic.INSTANCE_ID);

            // Generate a new topic if not already exists, otherwire returns the old topic instance
            Topic topic = getReceiveSession().createTopic("VirtualTopic." + to);
            getMessageProducer().send(topic, msg);
            Profiler.incrementSentEvents();

        } catch (JMSException ex) {

            LOG.error(Freedomotic.getStackTraceInfo(ex));
        }
    }
}
