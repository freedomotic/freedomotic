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
package com.freedomotic.nlp;

import com.freedomotic.api.AbstractConsumer;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.Resolver;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.reactions.Command;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;

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
 * It replies back if the received command cannot be interpreted, so you can
 * send the command and expect a reply in a given timeout. Read the see section
 * for more info.
 *
 * @see BusService#send(com.freedomotic.reactions.Command)
 * @author Enrico Nicoletti
 */
public class CommandsNlpService extends AbstractConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(CommandsNlpService.class.getName());
    private static final String MESSAGING_CHANNEL = "app.commands.interpreter.nlp";
    // Messaging related parameters
    public static final String PARAM_NLP_TEXT = "text";
    // Dependencies
    private final NlpCommand nlpCommands;

    @Inject
    public CommandsNlpService(NlpCommand nlpCommands, BusService busService) {
        super(busService);
        this.nlpCommands = nlpCommands;
    }

    @Override
    public void onCommand(final Command command) throws UnableToExecuteException {
        String text = command.getProperty(PARAM_NLP_TEXT);
        // Use NLP to find the most similar Command using the given free-form text
        Command mostSimilar;
        try {
            mostSimilar = findMostSimilarCommand(text);
            // Generate an almost empty event used to resolve commands properites (eg: current time and date)
            GenericEvent event = new GenericEvent(this);
            Resolver resolver = new Resolver();
            resolver.addContext("event.", event.getPayload());
            // Try to resolve the commands properties against the created event
            try {
                mostSimilar = resolver.resolve(mostSimilar);
                mostSimilar.setReplyTimeout(-1);
            } catch (CloneNotSupportedException | VariableResolutionException ex) {
                LOG.error(ex.getMessage());
            }
            // Schedule the command for execution
            getBusService().send(mostSimilar);
            // Report back which cammand was executed
            command.setProperty("result", mostSimilar.getName());
        } catch (NoResultsException ex) {
            throw new UnableToExecuteException("The given natural language text '"
                    + text + "' cannot be recognized as a valid framework command");
        }
    }

    /**
     * Creates a similarity ranking between the string in input and the Commands
     * registered in the system. Elements are ordered from the most similar
     * (index zero) to the less similar. It allows elements with similarity
     * equals to zero, so the first element may be not similar at all
     *
     * @param phrase The text representing a natural language command
     * @return a ranking of of commands similarity
     * @throws NoResultsException
     */
    public Command findMostSimilarCommand(String phrase) throws NoResultsException {
        // Compute the commands ranking
        List<Nlp.Rank<Command>> ranking = nlpCommands.computeSimilarity(phrase, 10);
        // Avoid returning a command with zero similarity
        if (ranking.isEmpty() || ranking.get(0).getSimilarity() <= 0) {
            throw new NoResultsException("No command is similar enough to '" + phrase + "'");
        }
        // Get the most similar command (it is in the top of the list)
        return ranking.get(0).getElement();
    }

    @Override
    public void onEvent(EventTemplate eventTemplate) {
        throw new UnsupportedOperationException("This modules doesn't handle events");
    }

    @Override
    public String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

}
