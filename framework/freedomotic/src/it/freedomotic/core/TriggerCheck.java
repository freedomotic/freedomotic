/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.events.MessageEvent;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.Trigger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public final class TriggerCheck {

    private static ExecutorService executor = Executors.newCachedThreadPool();

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

        Callable check = new Callable() {
            @Override
            public Object call() throws Exception {

                StringBuilder buff = new StringBuilder();
                try {
                    if (trigger.isHardwareLevel()) { //hardware triggers can always fire
                        Trigger resolved = resolveTrigger(event, trigger);
                        if (resolved.isConsistentWith(event)) {
                            buff.append("[CONSISTENT] hardware level trigger '").append(resolved.getName()).append(resolved.getPayload().toString()).append("'\nconsistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                            applySensorNotification(resolved, event);
                        }
                    } else {
                        if (trigger.canFire()) {
                            Trigger resolved = resolveTrigger(event, trigger);
                            if (resolved.isConsistentWith(event)) {
                                buff.append("[CONSISTENT] registred trigger '").append(resolved.getName()).append(resolved.getPayload().toString()).append("'\nconsistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                                executeTriggeredAutomations(resolved, event);
                            }
                        }
                    }
                    //if we are here the trigger is not consistent
                    buff.append("[NOT CONSISTENT] registred trigger '").append(trigger.getName()).append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                    Freedomotic.logger.config(buff.toString());
                    return false;
                } catch (Exception e) {
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                    return false;
                }
            }
        };
        Future<Boolean> result = executor.submit(check);
        try {
            return result.get();
        } catch (InterruptedException ex) {
            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ExecutionException ex) {
            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    private static Trigger resolveTrigger(final EventTemplate event, final Trigger trigger) {
        Resolver resolver = new Resolver();
        event.getPayload().addStatement("description", trigger.getDescription()); //embedd the trigger description to the event payload
        resolver.addContext("event.", event.getPayload());
        return resolver.resolve(trigger);
    }

    private synchronized static void applySensorNotification(Trigger resolved, final EventTemplate event) {
        String protocol;
        String address;
        ArrayList<EnvObjectLogic> objectList = new ArrayList<EnvObjectLogic>();

        //join device: add the object on the map if not already there
        protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
        address = resolved.getPayload().getStatements("event.address").get(0).getValue();
        if (protocol != null && address != null) {
            String clazz = event.getProperty("object.class");
            String name = event.getProperty("object.name");
            objectList = EnvObjectPersistence.getObject(protocol, address);
            if (objectList.isEmpty()) { //there isn't an object with this protocol and address
                Freedomotic.logger.warning("No objects with protocol=" + protocol + " and address=" + address + " (" + resolved.getName() + ")");
                if (clazz != null && !clazz.isEmpty()) {
                    EnvObjectLogic joined = JoinDevice.join(clazz, name, protocol, address);
                    objectList.add(joined);
                }
            }
        }
        //now we have the target object on the map for sure. Apply changes notified by sensors
        boolean done = false;
        for (EnvObjectLogic object : objectList) {
            boolean executed = object.executeTrigger(resolved); //user trigger->behavior mapping to apply the trigger to this object
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
        Iterator it = ReactionPersistence.iterator();
        //Searching for reactions using this trigger
        boolean found = false;
        while (it.hasNext()) {
            Reaction reaction = (Reaction) it.next();
            Trigger reactionTrigger = reaction.getTrigger();
            //found a related reaction. This must be executed
            if (trigger.equals(reactionTrigger)) {
                trigger.setExecuted();
                found = true;
                Freedomotic.logger.fine("Try to execute reaction " + reaction.toString());
                try {
                    //executes the commands in sequence (only the first sequence is used) 
                    //if more then one sequence is needed it can be done with two reactions with the same trigger
                    for (final Command command : reaction.getCommands()) {
                        if (command.getReceiver().equalsIgnoreCase(BehaviorManager.getMessagingChannel())) {
                            Resolver commandResolver = new Resolver();
                            //this command is for an object so it needs only to know only about event parameters
                            commandResolver.addContext("event.", event.getPayload());
                            Command resolvedCommand = commandResolver.resolve(command);
                            //doing so we bypass messaging system gaining better performances
                            BehaviorManager.parseCommand(resolvedCommand);
                        } else {
                            Resolver commandResolver = new Resolver();
                            commandResolver.addContext("event.", event.getPayload());
                            //if the event has a target object we include also object info
                            EnvObjectLogic targetObject = EnvObjectPersistence.getObject(event.getProperty("object.name"));
                            if (targetObject != null) {
                                commandResolver.addContext("current.", targetObject.getExposedProperties());
                                commandResolver.addContext("current.", targetObject.getExposedBehaviors());
                            }
                            Command resolvedCommand = commandResolver.resolve(command);
                            //it's not a user level command for objects (eg: turn it on), it is for another kind of actuator
                            Command reply = Freedomotic.sendCommand(resolvedCommand); //blocking wait (in this case in a thread) until executed
                            if (reply == null) {
                                Freedomotic.logger.fine("Unreceived reply within given time ("
                                        + command.getReplyTimeout() + "ms) for command " + command.getName());
                            } else {
                                if (reply.isExecuted()) {
                                    Freedomotic.logger.fine("Executed succesfully " + command.getName());
                                } else {
                                    Freedomotic.logger.fine("Unable to execute " + command.getName());
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
                Freedomotic.sendEvent(message);
            }
        }
        if (!found) {
            Freedomotic.logger.warning("No reaction bound to trigger '" + trigger.getName() + "'");
        }
    }
}
