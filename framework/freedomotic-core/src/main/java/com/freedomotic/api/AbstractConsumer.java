/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * TODO: THIS CLASS IT'S NOT FINISHED IT NEEDS TO IMPLEMENT THE REPLY FEATURE
 * Convenience root class for all bus message consumers
 *
 * @author enrico
 */
public abstract class AbstractConsumer implements BusConsumer {

    private static final Logger LOG = Logger.getLogger(AbstractConsumer.class.getName());
    private final BusMessagesListener listener;
    private final BusService busService;
    private boolean automaticReply;

    /**
     *
     * @param c
     * @throws IOException
     * @throws UnableToExecuteException
     */
    protected abstract void onCommand(Command c) throws IOException, UnableToExecuteException;

    /**
     *
     * @param event
     */
    protected abstract void onEvent(EventTemplate event);

    protected abstract String getMessagingChannel();

    public AbstractConsumer(BusService busService) {
        this.busService = busService;
        listener = new BusMessagesListener(this, busService);
        listener.consumeCommandFrom(getMessagingChannel());
        setAutomaticReply(true);
    }

    @Override
    public void onMessage(ObjectMessage message) {
        Object jmsObject;
        try {
            jmsObject = message.getObject();

            if (jmsObject instanceof Command) {
                final Command command = (Command) jmsObject;
                LOG.log(Level.CONFIG, "{0} receives command {1} with parametes '{''{'{2}'}''}'",
                        new Object[]{
                            this.getClass().getCanonicalName(),
                            command.getName(),
                            command.getProperties()});
                ActuatorPerforms task = new ActuatorPerforms(command,
                        message.getJMSReplyTo(),
                        message.getJMSCorrelationID());
                task.start();
            } else {
                if (jmsObject instanceof EventTemplate) {
                    final EventTemplate event = (EventTemplate) jmsObject;
                    onEvent(event);
                } else {
                    throw new RuntimeException("Unrecognized type in JMS message, is neither a Command or an Event");
                }
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, "Error while receiving a JMS message", ex);
        }
    }

    public boolean isAutomaticReply() {
        return automaticReply;
    }

    public void setAutomaticReply(boolean automaticReply) {
        this.automaticReply = automaticReply;
    }

    public BusService getBusService() {
        return busService;
    }


    //public void reply(Command command) {
        // sends back the command
    //    final String defaultCorrelationID = "-1";
    //    getBusService().reply(command, lastDestination, defaultCorrelationID);
    //}

    private class ActuatorPerforms extends Thread {

        private final Command command;
        private final Destination reply;
        private final String correlationID;

        ActuatorPerforms(Command c, Destination reply, String correlationID) {
            this.command = c;
            this.reply = reply;
            this.correlationID = correlationID;
            this.setName("command-executor");
        }

        @Override
        public void run() {
            try {
                // a command is supposed executed if the plugin doesen't say the contrary
                command.setExecuted(true);
                onCommand(command);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                command.setExecuted(false);
            } catch (UnableToExecuteException ex) {
                command.setExecuted(false);
                LOG.log(Level.INFO, "{0} failed to execute command {1}: {2}", new Object[]{getName(), command.getName(), ex.getMessage()});
            }

            // automatic-reply-to-command is used when the plugin executes the command in a
            // separate thread. In this cases the onCommand() returns immediately (as execution is forked in a thread)
            // and sometimes this is not the intended behavior. Take a look at the Delayer plugin configuration
            // it has to call reply(...) explicitely
            if ((isAutomaticReply()) && (command.getReplyTimeout() > 0)) {
                getBusService().reply(command, reply, correlationID); //sends back the command marked as executed or not
            }
        }
    }

}
