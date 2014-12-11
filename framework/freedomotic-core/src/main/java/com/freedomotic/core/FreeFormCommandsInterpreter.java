/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.nlp.Nlp;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.Command;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Listen for free-form (natural language) text commands on channel
 * <strong>"app.commands.interpreter.nlp"</strong> and using NLP tools execute
 * the most similar command that the framework has in memory. For example a
 * speech recognition utility may return a free-form text that can be
 * interpreted by this module as an executable command. Another example is a
 * chat bot that executes text commands.
 *
 * It expects a command with the following properties
 * <ul>
 * <li>text = A_STRING</li>
 * </ul>
 * 
 * It replies back if the received command cannot be interpreted, so you can send
 * the command and expect a reply in a given timeout. Read the see section for more info.
 *
 * @see BusService#send(com.freedomotic.reactions.Command)
 * @author enrico
 */
public class FreeFormCommandsInterpreter implements BusConsumer {

    private static final Logger LOG = Logger.getLogger(FreeFormCommandsInterpreter.class.getName());
    private static final String MESSAGING_CHANNEL = "app.commands.interpreter.nlp";
    private BusMessagesListener listener;
    // Dependencies
    private final NlpCommand nlpCommands;
    private final BusService busService;

    @Inject
    public FreeFormCommandsInterpreter(NlpCommand nlpCommands, BusService busService) {
        this.nlpCommands = nlpCommands;
        this.busService = busService;
        register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        listener = new BusMessagesListener(this, busService);
        listener.consumeCommandFrom(getMessagingChannel());
    }

    public String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

    @Override
    public void onMessage(ObjectMessage message) {
        Object jmsObject = null;
        Destination replyChannel = null;
        String correlationId = null;
        
        try {
            jmsObject = message.getObject();
            // Save some data for reply
            replyChannel = message.getJMSReplyTo();
            correlationId = message.getJMSCorrelationID();
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        if (jmsObject instanceof Command) {
            // Get the free-form text form the received command
            Command command = (Command) jmsObject;
            String text = command.getProperty("text");
            // Use NLP to find the most similar Command using the given free-form text
            Command mostSimilar = null;
            try {
                mostSimilar = findMostSimilarCommand(text);
            } catch (NoResultsException ex) {
                LOG.log(Level.WARNING, "The given natural language text ''{0}'' cannot be recognized as a valid framework command", text);
                command.setExecuted(false);
                busService.reply(command, replyChannel, correlationId);
            }
            // Generate an almost empty event used to resolve commands properites (eg: current time and date)
            GenericEvent event = new GenericEvent(this);
            Resolver resolver = new Resolver();
            resolver.addContext("event.", event.getPayload());
            // Try to resolve the commands properties against the created event
            Command resolvedCommand = null;
            try {
                resolvedCommand = resolver.resolve(mostSimilar);
            } catch (CloneNotSupportedException | VariableResolutionException ex) {
                Logger.getLogger(FreeFormCommandsInterpreter.class.getName()).log(Level.SEVERE, null, ex);
            }
            // Schedule the command for execution
            busService.send(resolvedCommand);
        }
    }

    protected Command findMostSimilarCommand(String phrase) throws NoResultsException {
        // Compute the commands ranking
        List<Nlp.Rank<Command>> ranking = nlpCommands.computeSimilarity(phrase, 10);
        // Avoid returning a command with zero similarity
        if (ranking.isEmpty() || ranking.get(0).getSimilarity() <= 0) {
            throw new NoResultsException("No command is similar enough to '" + phrase + "'");
        }
        // Get the most similar command (it is in the top of the list)
        return ranking.get(0).getElement();
    }

}
