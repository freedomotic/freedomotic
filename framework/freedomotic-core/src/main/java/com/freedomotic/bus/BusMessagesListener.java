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

import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.Profiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * {@link MessageListener} implementation (former AbstractBusConnector class)
 * <p>
 * Receives an {@link ObjectMessage} (it can be an event or a command) and sends
 * it to his {@link BusConsumer}
 * <p>
 * This is the bus hook for any {@link BusConsumer} that should register itself
 * in this listener.
 *
 * @author Freedomotic Team
 *
 * @see BusConsumer
 */
public class BusMessagesListener implements MessageListener {

    private static final Logger LOG = Logger.getLogger(BusMessagesListener.class.getName());

    private BusService busService;

    private BusConsumer busConsumer;

    private MessageConsumer messageConsumer;

    private final HashMap<String, MessageConsumer> registeredEventQueues;
    private final HashMap<String, MessageConsumer> registeredCommandQueues;

    /**
     * Constructor
     *
     * @param busConsumer
     * @param busService
     */
    @Inject
    public BusMessagesListener(BusConsumer busConsumer, BusService busService) {
        if (busService == null) {
            throw new IllegalArgumentException("Bus service cannot be not null");
        }
        this.busConsumer = busConsumer;
        this.busService = busService;
        this.registeredEventQueues = new HashMap<>();
        this.registeredCommandQueues = new HashMap<>();
        if (busService == null) {
            throw new IllegalStateException("A message listener must have a working bus link");
        }
        if (busConsumer == null) {
            throw new IllegalStateException("A message listener must have an attached consumer");
        }
    }

    /**
     * Passes a message to the listener
     *
     * @param message
     */
    @Override
    public final void onMessage(Message message) {

        Profiler.incrementReceivedEvents();

        if (message instanceof ObjectMessage) {
            final ObjectMessage objectMessage = (ObjectMessage) message;
            busConsumer.onMessage(objectMessage);
        } else {

            LOG.severe("Message received by " + this.getClass().getSimpleName()
                    + " is not an object message, is a "
                    + message.getClass().getCanonicalName());
            if (message instanceof TextMessage) {
                TextMessage text = (TextMessage) message;
                try {
                    LOG.severe(text.getText());
                } catch (JMSException ex) {
                    LOG.log(Level.SEVERE, "Error while receiving a text message", ex);
                }
            }
        }
    }

    /**
     * Registers on a command queue
     *
     * @param queueName Queue name
     */
    public void consumeCommandFrom(String queueName) {

        try {

            BusDestination busDestination = busService
                    .registerCommandQueue(queueName);

            registeredCommandQueues.put(busDestination.getDestinationName(), registerOnQueue(busDestination));
        } catch (JMSException e) {

            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    /**
     * Registers on a event queue
     *
     * @param queueName Queue name
     */
    public void consumeEventFrom(String queueName) {

        try {

            BusDestination busDestination = busService
                    .registerEventQueue(queueName);

            registeredEventQueues.put(busDestination.getDestinationName(), registerOnQueue(busDestination));

        } catch (JMSException e) {

            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private MessageConsumer registerOnQueue(BusDestination destination)
            throws JMSException {

        final Session receiveSession = busService.getReceiveSession();
        MessageConsumer messageConsumer = receiveSession.createConsumer(destination
                .getDestination());
        messageConsumer.setMessageListener(this);
        LOG.info(busConsumer.getClass().getSimpleName() + " listen on "
                + destination.getDestinationName());
        return messageConsumer;
    }

    /**
     * Unsubscribes from all topics queues
     * <br>
     * (invocations should be life cycle managed)
     */
    public void unsubscribe() {

        unsubscribeCommands();
        unsubscribeEvents();

    }

    /**
     * Unsubscribes from events queues
     * <br>
     * (invocations should be life cycle managed)
     */
    public void unsubscribeEvents() {
        final Session receiveSession = busService.getReceiveSession();
        for (String queueName : registeredEventQueues.keySet()) {
            try {
                MessageConsumer mc = registeredEventQueues.get(queueName);
                mc.setMessageListener(null);
                mc.close();
            } catch (JMSException ex) {
                LOG.severe("Unable to unsubscribe from event channel " + queueName + " for reason: " + ex.getLocalizedMessage());
            }
        }
        registeredEventQueues.clear();
    }

    /**
     * Unsubscribes from commands queues
     * <br>
     * (invocations should be life cycle managed)
     */
    public void unsubscribeCommands() {
        final Session receiveSession = busService.getReceiveSession();
        for (String queueName : registeredCommandQueues.keySet()) {
            try {
                MessageConsumer mc = registeredCommandQueues.get(queueName);
                mc.setMessageListener(null);
                mc.close();
            } catch (JMSException ex) {
                LOG.severe("Unable to unsubscribe from event channel " + queueName + " for reason: " + ex.getLocalizedMessage());
            }
        }
        registeredCommandQueues.clear();
    }

    /**
     * FIXME LCG is really unused?
     */
//	private ObjectMessage createObjectMessage() throws JMSException {
//
//		final Session sendSession = busService.getSendSession();
//		ObjectMessage msg = sendSession.createObjectMessage();
//
//		return msg;
//	}
}
