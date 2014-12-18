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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusService;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class TriggerCheck {

    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    // Dependencies
    private final Autodiscovery autodiscovery;
    private final BusService busService;
    private final ThingRepository thingsRepository;
    private final BehaviorManager behaviorManager;

    @Inject
    TriggerCheck(
            Autodiscovery autodiscovery,
            ThingRepository thingsRepository,
            BusService busService,
            BehaviorManager behaviorManager) {
        this.autodiscovery = autodiscovery;
        this.thingsRepository = thingsRepository;
        this.busService = busService;
        this.behaviorManager = behaviorManager;
    }

    /**
     * Executes trigger-event comparison in a separated thread
     *
     * @param event
     * @param trigger
     * @return
     */
    public boolean check(final EventTemplate event, final Trigger trigger) {
        if ((event == null) || (trigger == null)) {
            throw new IllegalArgumentException("Event and Trigger cannot be null while performing trigger check");
        }

        StringBuilder buff = new StringBuilder();

        try {
            if (trigger.isHardwareLevel()) { //hardware triggers can always fire

                Trigger resolved = resolveTrigger(event, trigger);

                if (resolved.isConsistentWith(event)) {
                    buff.append("[CONSISTENT] hardware level trigger '").append(resolved.getName()).append("' ")
                            .append(resolved.getPayload().toString()).append("'\nconsistent with received event '")
                            .append(event.getEventName()).append("' ").append(event.getPayload().toString());
                    applySensorNotification(resolved, event);
                    LOG.fine(buff.toString());
                    return true;
                }
            } else {
                if (trigger.canFire()) {
                    Trigger resolved = resolveTrigger(event, trigger);

                    if (resolved.isConsistentWith(event)) {
                        buff.append("[CONSISTENT] registred trigger '").append(resolved.getName()).append("' ")
                                .append(resolved.getPayload().toString()).append("'\nconsistent with received event '")
                                .append(event.getEventName()).append("' ").append(event.getPayload().toString());
                        executeTriggeredAutomations(resolved, event);
                        LOG.fine(buff.toString());
                        return true;
                    }
                }
            }

            //if we are here the trigger is not consistent
            buff.append("[NOT CONSISTENT] registred trigger '").append(trigger.getName()).append("' ")
                    .append(trigger.getPayload().toString()).append("'\nnot consistent with received event '")
                    .append(event.getEventName()).append("' ").append(event.getPayload().toString());
            LOG.fine(buff.toString());

            return false;
        } catch (Exception e) {
            LOG.severe(Freedomotic.getStackTraceInfo(e));

            return false;
        }
    }

    private Trigger resolveTrigger(final EventTemplate event, final Trigger trigger) throws VariableResolutionException {
        Resolver resolver = new Resolver();
        resolver.addContext("event.",
                event.getPayload());

        return resolver.resolve(trigger);
    }

    private void applySensorNotification(Trigger resolved, final EventTemplate event) {
        String protocol;
        String address;
        List<EnvObjectLogic> affectedObjects = new ArrayList<EnvObjectLogic>();

        //join device: add the object on the map if not already there
        //join device requires to know 'object.class' and 'object.name' properties
        protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
        address = resolved.getPayload().getStatements("event.address").get(0).getValue();

        if ((protocol != null) && (address != null)) {
            String clazz = event.getProperty("object.class");
            String name = event.getProperty("object.name");
            affectedObjects = thingsRepository.findByAddress(protocol, address);

            if (affectedObjects.isEmpty()) { //there isn't an object with this protocol and address
                LOG.log(Level.WARNING, "Found a candidate for things autodiscovery: thing ''{0}'' of type ''{1}''", new Object[]{name, clazz});
                if ((clazz != null) && !clazz.isEmpty()) {
                    EnvObjectLogic joined = autodiscovery.join(clazz, name, protocol, address);
                    affectedObjects.add(joined);
                }
            }
        }

        //now we have the target object on the map for sure. Apply changes notified by sensors
        boolean done = false;

        for (EnvObjectLogic object : affectedObjects) {
            //uses trigger->behavior mapping to apply the trigger to this object
            boolean executed = object.executeTrigger(resolved);

            if (executed) {
                done = true;

                long elapsedTime = System.currentTimeMillis() - event.getCreation();
                LOG.log(Level.INFO,
                        "Sensor notification ''{0}'' applied to object ''{1}'' in {2}ms.",
                        new Object[]{resolved.getName(), object.getPojo().getName(), elapsedTime});
            }
        }

        if (!done) {
            LOG.log(Level.WARNING, "Hardware trigger {0} is not associated to any object.", resolved.getName());
        }
    }

    private void executeTriggeredAutomations(final Trigger trigger, final EventTemplate event) {
        Runnable automation;
        automation = new Runnable() {
            @Override
            public void run() {
                Iterator<Reaction> it = ReactionPersistence.iterator();

                //Searching for reactions using this trigger
                boolean found = false;

                while (it.hasNext()) {
                    Reaction reaction = it.next();
                    Trigger reactionTrigger = reaction.getTrigger();

                    //found a related reaction. This must be executed
                    if (trigger.equals(reactionTrigger) && !reaction.getCommands().isEmpty()) {
                        if (!checkAdditionalConditions(reaction)) {
                            LOG.log(Level.INFO,
                                    "Additional conditions test failed in reaction {0}", reaction.toString());
                            return;
                        }
                        reactionTrigger.setExecuted();
                        found = true;
                        LOG.log(Level.FINE, "Try to execute reaction {0}", reaction.toString());

                        try {
                            //executes the commands in sequence (only the first sequence is used) 
                            //if more then one sequence is needed it can be done with two reactions with the same trigger
                            Resolver commandResolver = new Resolver();
                            event.getPayload().addStatement("description",
                                    trigger.getDescription()); //embedd the trigger description to the event payload
                            commandResolver.addContext("event.",
                                    event.getPayload());

                            for (final Command command : reaction.getCommands()) {
                                if (command == null) {
                                    continue; //skip this loop
                                }

                                if (command.getReceiver()
                                        .equalsIgnoreCase(BehaviorManager.getMessagingChannel())) {
                                    //this command is for an object so it needs only to know only about event parameters
                                    Command resolvedCommand = commandResolver.resolve(command);
                                    //doing so we bypass messaging system gaining better performances
                                    behaviorManager.parseCommand(resolvedCommand);
                                } else {
                                    //if the event has a target object we include also object info
                                    EnvObjectLogic targetObject
                                            = thingsRepository.findByName(event.getProperty("object.name")).get(0);

                                    if (targetObject != null) {
                                        commandResolver.addContext("current.",
                                                targetObject.getExposedProperties());
                                        commandResolver.addContext("current.",
                                                targetObject.getExposedBehaviors());
                                    }

                                    final Command resolvedCommand = commandResolver.resolve(command);

                                    //it's not a user level command for objects (eg: turn it on), it is for another kind of actuator
                                    Command reply = busService.send(resolvedCommand); //blocking wait until executed

                                    if (reply == null) {
                                        command.setExecuted(false);
                                        LOG.log(Level.WARNING,
                                                "Unreceived reply within given time ({0}ms) for command {1}",
                                                new Object[]{command.getReplyTimeout(), command.getName()});
                                        notifyMessage("Unreceived reply within given time for command " + command.getName());
                                    } else {
                                        if (reply.isExecuted()) {
                                            //the reply is executed so mark the origial command as executed as well
                                            command.setExecuted(true);
                                            LOG.log(Level.FINE, "Executed succesfully {0}", command.getName());
                                        } else {
                                            command.setExecuted(false);
                                            LOG.log(Level.WARNING, "Unable to execute command {0}. Skipping the others", command.getName());
                                            notifyMessage("Unable to execute command " + command.getName());
                                            // skip the other commands
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.severe("Exception while merging event parameters into reaction.\n");
                            LOG.severe(Freedomotic.getStackTraceInfo(e));

                            return;
                        }

                        String info
                                = "Executing automation '" + reaction.toString() + "' takes "
                                + (System.currentTimeMillis() - event.getCreation()) + "ms.";
                        LOG.info(info);

                        MessageEvent message = new MessageEvent(null, info);
                        message.setType("callout"); //display as callout on frontends
                        busService.send(message);
                    }
                }

                if (!found) {
                    LOG.log(Level.CONFIG, "No valid reaction bound to trigger ''{0}''", trigger.getName());
                }
            }

            /**
             * Resolves the additional conditions of the reaction in input. Now
             * it just takes the statement attribute and value and check if they
             * are equal to the target behavior name and value respectively.
             * This should be improved to allow also REGEX and other statement
             * resolution.
             */
            private boolean checkAdditionalConditions(Reaction rea) {
                boolean result = true;
                for (Condition condition : rea.getConditions()) {
                    //System.out.println("DEBUG: check condition " + condition.getTarget());
                    EnvObjectLogic object = thingsRepository.findByName(condition.getTarget()).get(0);
                    Statement statement = condition.getStatement();
                    if (object != null) {
                        BehaviorLogic behavior = object.getBehavior(statement.getAttribute());
                        //System.out.println("DEBUG: " + object.getPojo().getName() + " "
                        //+ " behavior: " + behavior.getName() + " " + behavior.getValueAsString());
                        boolean eval = behavior.getValueAsString().equalsIgnoreCase(statement.getValue());
                        if (statement.getLogical().equalsIgnoreCase("AND")) {
                            result = result && eval;
                            //System.out.println("DEBUG: result and: " + result + "(" + eval +")");
                        } else {
                            result = result || eval;
                            //System.out.println("DEBUG: result or: " + result + "(" + eval +")");
                        }
                    } else {
                        LOG.log(Level.WARNING, "Cannot test condition on unexistent object: {0}", condition.getTarget());
                        return false;
                    }
                }
                return result;
            }
        };

        EXECUTOR.execute(automation);
    }

    private void notifyMessage(String message) {
        MessageEvent event = new MessageEvent(this, message);
        event.setType("callout"); //display as callout on frontends
        busService.send(event);
    }

    private static final Logger LOG = Logger.getLogger(TriggerCheck.class.getName());
}
