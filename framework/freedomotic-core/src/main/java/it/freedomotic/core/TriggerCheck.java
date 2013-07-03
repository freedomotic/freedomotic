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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.MessageEvent;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.Trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Enrico
 */
public final class TriggerCheck {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool();

    //private static ExecutorService executor = Executors.newCachedThreadPool();
    //uncallable constructor
    private TriggerCheck() {
    }

    /**
     * Executes trigger-event comparison in a separated thread
     *
     * @param event
     * @param trigger
     * @return
     * @throws TriggerCheckException
     */
    public static boolean check(final EventTemplate event, final Trigger trigger) {
        if (event == null || trigger == null) {
            throw new IllegalArgumentException("Event and Trigger cannot be null while performing trigger check");
        }

        StringBuilder buff = new StringBuilder();
        try {
            if (trigger.isHardwareLevel()) { //hardware triggers can always fire
                Trigger resolved = resolveTrigger(event, trigger);
                if (resolved.isConsistentWith(event)) {
                    buff.append("[CONSISTENT] hardware level trigger '").append(resolved.getName()).append("' ").append(resolved.getPayload().toString()).append("'\nconsistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                    applySensorNotification(resolved, event);
                    Freedomotic.logger.config(buff.toString());
                    return true;
                }
            } else {
                if (trigger.canFire()) {
                    Trigger resolved = resolveTrigger(event, trigger);
                    if (resolved.isConsistentWith(event)) {
                        buff.append("[CONSISTENT] registred trigger '").append(resolved.getName()).append("' ").append(resolved.getPayload().toString()).append("'\nconsistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                        executeTriggeredAutomations(resolved, event);
                        Freedomotic.logger.config(buff.toString());
                        return true;
                    }
                }
            }
            //if we are here the trigger is not consistent
            buff.append("[NOT CONSISTENT] registred trigger '").append(trigger.getName()).append("' ").append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
            Freedomotic.logger.config(buff.toString());
            return false;
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
            return false;
        }
    }

    private static Trigger resolveTrigger(final EventTemplate event, final Trigger trigger) {
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        return resolver.resolve(trigger);
    }

    private static void applySensorNotification(Trigger resolved, final EventTemplate event) {
        String protocol = null;
        String address = null;
        ArrayList<EnvObjectLogic> affectedObjects = new ArrayList<EnvObjectLogic>();

        //join device: add the object on the map if not already there
        //join device requires to know 'object.class' and 'object.name' properties
        protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
        address = resolved.getPayload().getStatements("event.address").get(0).getValue();
        if (protocol != null && address != null) {
            String clazz = event.getProperty("object.class");
            String name = event.getProperty("object.name");
            affectedObjects = EnvObjectPersistence.getObjectByAddress(protocol, address);
            if (affectedObjects.isEmpty()) { //there isn't an object with this protocol and address
                if (clazz != null && !clazz.isEmpty()) {
                    EnvObjectLogic joined = JoinDevice.join(clazz, name, protocol, address);
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
                Freedomotic.logger.info("Sensor notification '" + resolved.getName() + "' applied to object '"
                        + object.getPojo().getName() + "' in " + elapsedTime + "ms.");
            }
        }
        if (!done) {
            Freedomotic.logger.warning("Hardware trigger " + resolved.getName() + " is not associated to any object.");
        }
    }

    private static void executeTriggeredAutomations(final Trigger trigger, final EventTemplate event) {
        Runnable automation = new Runnable() {
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
                        reactionTrigger.setExecuted();
                        found = true;
                        Freedomotic.logger.fine("Try to execute reaction " + reaction.toString());
                        try {
                            //executes the commands in sequence (only the first sequence is used) 
                            //if more then one sequence is needed it can be done with two reactions with the same trigger
                            Resolver commandResolver = new Resolver();
                            event.getPayload().addStatement("description", trigger.getDescription()); //embedd the trigger description to the event payload
                            commandResolver.addContext("event.", event.getPayload());
                            for (final Command command : reaction.getCommands()) {
                                if (command == null) {
                                    continue; //skip this loop
                                }
                                if (command.getReceiver().equalsIgnoreCase(BehaviorManager.getMessagingChannel())) {
                                    //this command is for an object so it needs only to know only about event parameters
                                    Command resolvedCommand = commandResolver.resolve(command);
                                    //doing so we bypass messaging system gaining better performances
                                    BehaviorManager.parseCommand(resolvedCommand);
                                } else {
                                    //if the event has a target object we include also object info
                                    EnvObjectLogic targetObject = EnvObjectPersistence.getObjectByName(event.getProperty("object.name"));
                                    if (targetObject != null) {
                                        commandResolver.addContext("current.", targetObject.getExposedProperties());
                                        commandResolver.addContext("current.", targetObject.getExposedBehaviors());
                                    }
                                    final Command resolvedCommand = commandResolver.resolve(command);
                                    //it's not a user level command for objects (eg: turn it on), it is for another kind of actuator
                                    Command reply = Freedomotic.sendCommand(resolvedCommand); //blocking wait until executed
                                    if (reply == null) {
                                        Freedomotic.logger.warning("Unreceived reply within given time ("
                                                + command.getReplyTimeout() + "ms) for command " + command.getName());
                                    } else {
                                        if (reply.isExecuted()) {
                                            Freedomotic.logger.fine("Executed succesfully " + command.getName());
                                        } else {
                                            Freedomotic.logger.warning("Unable to execute command" + command.getName());
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            Freedomotic.logger.severe("Exception while merging event parameters into reaction.\n");
                            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                            return;
                        }
                        String info = "Executing automation '" + reaction.toString()
                                + "' takes " + (System.currentTimeMillis() - event.getCreation()) + "ms.";
                        Freedomotic.logger.info(info);
                        MessageEvent message = new MessageEvent(null, info);
                        message.setType("callout"); //display as callout on frontends
                        Freedomotic.sendEvent(message);
                    }
                }
                if (!found) {
                    Freedomotic.logger.config("No valid reaction bound to trigger '" + trigger.getName() + "'");
                }

            }
        };
        EXECUTOR.execute(automation);
    }
}
