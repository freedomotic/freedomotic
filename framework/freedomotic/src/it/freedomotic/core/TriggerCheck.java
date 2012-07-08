/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.exceptions.TriggerCheckException;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.util.UidGenerator;
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
     * Executes trigger comparision in a separated thread
     *
     * @param event
     * @param trigger
     * @return
     * @throws TriggerCheckException
     */
    public static boolean check(final EventTemplate event, final Trigger trigger) throws TriggerCheckException {
        
        Callable check = new Callable() {
            
            @Override
            public Object call() throws Exception {
                
                StringBuilder buff = new StringBuilder();
                try {
                    if (trigger.isHardwareLevel()) {
                        if (!trigger.isConsistentWith(event)) {
                            buff.append("[NOT CONSISTENT] hardware level trigger '").append(trigger.getName()).append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                            Freedomotic.logger.fine(buff.toString());
                            return false;
                        } else {
                            buff.append("[CONSISTENT] hardware level trigger '").append(trigger.getName()).append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                            Freedomotic.logger.fine(buff.toString());
                            changeObjectProperties(trigger, event);
                            return true;
                        }
                    } else {
                        if (!trigger.canFire()) {
                            return false;
                        }
                        if (!trigger.isConsistentWith(event)) {
                            buff.append("[NOT CONSISTENT] registred trigger '").append(trigger.getName()).append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                            Freedomotic.logger.fine(buff.toString());
                            return false;
                        }
                        buff.append("[CONSISTENT] registred trigger '").append(trigger.getName()).append(trigger.getPayload().toString()).append("'\nnot consistent with received event '").append(event.getEventName()).append("' ").append(event.getPayload().toString());
                        executeRelatedReactions(trigger, event);
                        return true;
                    }
                } catch (Exception e) {
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                    return false;
                } finally {
                    Freedomotic.logger.config(buff.toString());
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
    
    private synchronized static void changeObjectProperties(Trigger trigger, final EventTemplate event) {
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        Trigger resolved = null;
        try {
            resolved = resolver.resolve(trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String protocol = null;
        String address = null;
        ArrayList<EnvObjectLogic> objectList = null;
        protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
        address = resolved.getPayload().getStatements("event.address").get(0).getValue();
        objectList = EnvObjectPersistence.getObject(protocol, address);
        if (objectList.isEmpty()) {
            Freedomotic.logger.warning("No objects with protocol=" + protocol + " and address=" + address + " (" + trigger.getName() + ") creating a new one");
            JoinDevice.join(
                    "Switch", 
                    protocol + "-" + UidGenerator.getNextStringUid(), 
                    protocol, 
                    address);
            return;
        }
        boolean done = false;
        for (EnvObjectLogic object : objectList) {
            boolean executed = object.executeTrigger(resolved); //user trigger->behavior mapping to apply the trigger to this object
            if (executed) {
                done = true;
                long elapsedTime = System.currentTimeMillis() - event.getCreation();
                Freedomotic.logger.info("Sensor notification '" + trigger.getName() + "' applied to object '"
                        + object.getPojo().getName() + "' in " + elapsedTime + "ms.");
            }
        }
        if (!done) {
            Freedomotic.logger.warning("Hardware trigger " + trigger.getName() + " is not associated to any object.");
        }
    }
    
    public static void executeRelatedReactions(final Trigger trigger, final EventTemplate event) {
        Iterator it = ReactionPersistence.iterator();
        //Searching for reactions using this trigger
        boolean found = false;
        while (it.hasNext()) {
            Reaction reaction = (Reaction) it.next();
            //the trigger defined in the current reaction
            Trigger reactionTrigger = reaction.getTrigger();
            Reaction resolved = null;
            //found a related reaction. This must be executed
            if (trigger.equals(reactionTrigger)) {
                trigger.setExecuted();
                found = true;
                Freedomotic.logger.config("Try to execute reaction " + reaction.toString());
                try {
                    //resolves temporary values in command like @event.date replacing freedomotic variables with string or numerical values
                    event.getPayload().addStatement("description", trigger.getDescription()); //embedd the trigger description to the event payload
                    //new the trigger description can be used in commands (as it was the event description) with @event.description
                    Resolver resolver = new Resolver();
                    resolver.addContext("event.", event.getPayload());
                    resolved = resolver.resolve(reaction);
                } catch (Exception e) {
                    Freedomotic.logger.severe("Exception while merging event parameters into reaction.\n");
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                    return;
                }
//                SchedulingData data = new SchedulingData(event.getCreation());
//                data.getLog().append(buff.toString());
//                resolved.setScheduling(data);
//                Freedomotic.getScheduler().schedule(resolved);
                //executes the commands in sequence (only the first sequence is used) 
                //if more then one sequence is needed it can be done with two reactions with the same trigger
                for (Command command : resolved.getCommands()) {
//                    if (command.getReceiver().equalsIgnoreCase(BehaviorManagerForObjects.getMessagingChannel())) {
//                        //doing so we bypass messagin system gaining better performances
//                        BehaviorManagerForObjects.parseCommand(command);
//                    } else {
                    //it's not a usel level command for objects (eg: turn it on), it is for another kind of actuator
                    Command reply = Freedomotic.sendCommand(command); //blocking wait (in this case in a thread) until executed
                    if (reply != null) {
                        if (reply.isExecuted() == true) {
                            Freedomotic.logger.config("Executed succesfully " + command.getName());
                        } else {
                            Freedomotic.logger.config("Unable to execute " + command.getName());
                        }
                    } else {
                        Freedomotic.logger.config("Unreceived reply within given time ("
                                + command.getReplyTimeout() + "ms) for command " + command.getName());
                    }
//                    }
                }
                Freedomotic.logger.info("Executing reaction '" + reaction.toString()
                        + "' takes " + (System.currentTimeMillis() - event.getCreation()) + "ms.");
            }
        }
        if (!found) {
            Freedomotic.logger.warning("No reaction bound to trigger '" + trigger.getName() + "'");
        }
    }
//    private static void displayOnFrontend(String message) {
//        final Command c = new Command();
//        c.setName("A callout from the scheduler");
//        c.setDelay(0);
//        c.setExecuted(true);
//        c.setEditable(false);
//        c.setReceiver("app.actuators.frontend.javadesktop.in");
//        c.setProperty("callout-message", message);
//        //this method is supposed to be called in threads so it not instantiate others
//        Freedomotic.sendCommand(c);
//    }
}
