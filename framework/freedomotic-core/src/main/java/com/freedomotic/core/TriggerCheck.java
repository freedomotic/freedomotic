/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.core;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.FreedomoticException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.rules.ExpressionFactory;
import com.freedomotic.rules.Payload;
import com.freedomotic.rules.Statement;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;

/**
 *
 * @author Enrico Nicoletti
 */
public class TriggerCheck {

    private static final Logger LOG = LoggerFactory.getLogger(TriggerCheck.class.getName());
    private static final ExecutorService AUTOMATION_EXECUTOR = Executors.newCachedThreadPool();
    // Dependencies
    private final Autodiscovery autodiscovery;
    private final BusService busService;
    private final ThingRepository thingsRepository;
    private final ReactionRepository reactionRepository;
    private final BehaviorManager behaviorManager;
    private static final Pattern PATTERN = Pattern.compile("\\[(.*?)\\]\\.+[0-9A-Za-z]");

    @Inject
    TriggerCheck(
            Autodiscovery autodiscovery,
            ThingRepository thingsRepository,
            BusService busService,
            BehaviorManager behaviorManager,
            ReactionRepository reactionRepository) {
        this.autodiscovery = autodiscovery;
        this.thingsRepository = thingsRepository;
        this.busService = busService;
        this.behaviorManager = behaviorManager;
        this.reactionRepository = reactionRepository;
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

        try {
        	Trigger resolved = resolveTrigger(event, trigger);
        	
            if (trigger.isHardwareLevel() && resolved!=null && resolved.isConsistentWith(event)) {
                    LOG.debug("[CONSISTENT] hardware level trigger \"{} {}\"\nconsistent with received event \"{}\" \"{}\"", 
                    		resolved.getName(), 
                    		this.getPayload(resolved.getPayload()), 
                    		event.getEventName(), 
                    		this.getPayload(event.getPayload()));
                    applySensorNotification(resolved, event);
                    return true;
            } else {
                if (trigger.canFire() && resolved!=null && resolved.isConsistentWith(event)) {
                            LOG.debug("[CONSISTENT] registered trigger \"{} {}\"\nconsistent with received event ''{}'' {}", 
                            		resolved.getName(), 
                            		this.getPayload(resolved.getPayload()), 
                            		event.getEventName(), 
                            		this.getPayload(event.getPayload()));
                        executeTriggeredAutomations(resolved, event);
                        return true;
                    }
            }

            //if we are here the trigger is not consistent
            if (LOG.isDebugEnabled()) {
                LOG.debug("[NOT CONSISTENT] registered trigger \"{} {}\"\nnot consistent with received event ''{}'' {}", 
                		trigger.getName(), 
                		trigger.getPayload().toString(), 
                		event.getEventName(), 
                		event.getPayload().toString());
            }
            return false;
        } catch (Exception e) {
            LOG.error("Error while performing trigger check", e);
            return false;
        }
    }
    
    //it returns the payload as string, if any.
    private String getPayload(Payload payload) {
    	return (payload!=null)?payload.toString():"";
    }
    /**
     *
     *
     * @param event
     * @param trigger
     * @return
     * @throws VariableResolutionException
     */
    private Trigger resolveTrigger(final EventTemplate event, final Trigger trigger) throws VariableResolutionException {
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        return resolver.resolve(trigger);
    }

    /**
     *
     *
     * @param resolved
     * @param event
     */
    private void applySensorNotification(Trigger resolved, final EventTemplate event) {
        String protocol = null;
        String address = null;
        EnvObjectLogic affectedObject = null;

        if (!(resolved.getPayload().getStatements("event.protocol").isEmpty() && resolved.getPayload().getStatements("event.address").isEmpty())) {
            //join device: add the object on the map if not already there
            //join device requires to know 'object.class' and 'object.name' properties
            protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
            address = resolved.getPayload().getStatements("event.address").get(0).getValue();
        }

        if ((protocol != null) && (address != null)) {
        	affectedObject = this.getAffectedObject(event, protocol, address);
        }

        //uses trigger->behavior mapping to apply the trigger to this object
        if (affectedObject != null && affectedObject.executeTrigger(resolved)) {
            long elapsedTime = System.currentTimeMillis() - event.getCreation();
            LOG.info("Sensor notification \"{}\" applied to thing \"{}\" in {} ms.", resolved.getName(), affectedObject.getPojo().getName(), elapsedTime);
        } else {
            LOG.warn("Hardware trigger \"{}\" is not associated to any thing.", resolved.getName());
        }
        resolved.getPayload().clear();
        event.getPayload().clear();
    }

    private EnvObjectLogic getAffectedObject(EventTemplate event, String protocol, String address) {
        String clazz = event.getProperty("object.class");
        String name = event.getProperty("object.name");
        String autodiscoveryAllowClones = event.getProperty("autodiscovery.allow-clones");

        EnvObjectLogic affectedObject = thingsRepository.findByAddress(protocol, address);

        if (affectedObject == null) { //there isn't an object with this protocol and address
            LOG.warn("Found a candidate for things autodiscovery: thing \"{}\" of type \"{}\"", name, clazz);
            if ((clazz != null) && !clazz.isEmpty()) {
                boolean allowClones = ("false".equalsIgnoreCase(autodiscoveryAllowClones))?false:true;
                try {
                    affectedObject = autodiscovery.join(clazz, name, protocol, address, allowClones);
                } catch (RepositoryException ex) {
                    LOG.error(Freedomotic.getStackTraceInfo(ex));
                }
            }
        }
        return affectedObject;
    }
    
    /**
     *
     *
     * @param trigger
     * @param event
     */
    private void executeTriggeredAutomations(final Trigger trigger, final EventTemplate event) {
        AUTOMATION_EXECUTOR.execute(() -> {
            //Searching for reactions using this trigger
        	Iterator<Reaction> reactIterator = new CopyOnWriteArrayList<Reaction>(reactionRepository.findAll()).listIterator();
        	
            while (reactIterator.hasNext()) {
            	Reaction reaction = reactIterator.next();
                Trigger reactionTrigger = reaction.getTrigger();
                //found a related reaction. This must be executed
                if (trigger.equals(reactionTrigger) && !reaction.getCommands().isEmpty()) {
                    if (!checkAdditionalConditions(reaction)) {
                        LOG.info("Additional conditions test failed in reaction \"{}\"", reaction.getShortDescription());
                        return;
                    }
                    reactionTrigger.setExecuted();
                  
                    LOG.debug("Try to execute reaction \"{}\"", reaction.toString());
                      
                    //executes the commands in sequence (only the first sequence is used) 
                    //if more then one sequence is needed it can be done with two reactions with the same trigger
                    Resolver commandResolver = new Resolver();
                    event.getPayload().addStatement("description", trigger.getDescription()); //embedd the trigger description to the event payload
                    commandResolver.addContext("event.", event.getPayload());

                    if(!this.processReactionCommands(event, reaction, commandResolver))
                    	return;
                    
                    String info = "Executing automation \"" + reaction.toString() + "\" takes "
                            + (System.currentTimeMillis() - event.getCreation()) + "ms.";
                    LOG.info(info);

                    MessageEvent message = new MessageEvent(null, info);
                    message.setType("callout"); //display as callout on frontends
                    busService.send(message);
                }
                
                else {
                	LOG.info("No valid reaction {} bound to trigger \"{}\"", reaction.getShortDescription(), trigger.getName());
                }
            }

            trigger.getPayload().clear();
            event.getPayload().clear();
        });
    }
    
    
    private boolean processReactionCommands(final EventTemplate event, Reaction reaction, Resolver commandResolver) { 
    	try { 
            for (final Command command : reaction.getCommands()) {
                if(!this.processCommand(event, command, commandResolver))
                	return false;
            }
        } catch (Exception e) {
            LOG.error("Exception while merging event parameters into reaction.", e);
            return false;
        }
    	
    	return true;
    }
    
    private boolean processCommand(
    		final EventTemplate event, 
    		final Command command, 
    		Resolver commandResolver) throws VariableResolutionException, CloneNotSupportedException {
    	
    	 if (command == null) {
             return true;
         }
    	
    	if (command.getReceiver().equalsIgnoreCase(BehaviorManager.getMessagingChannel())) {
            //this command is for an object so it needs only to know only about event parameters
            Command resolvedCommand = commandResolver.resolve(command);
            //doing so we bypass messaging system gaining better performances
            behaviorManager.parseCommand(resolvedCommand);
            return true;
        }
    	
        //if the event has a target object we include also object info
        List<EnvObjectLogic> targetObjects = thingsRepository.findByName(event.getProperty("object.name"));

        if (!targetObjects.isEmpty()) {
            EnvObjectLogic targetObject = targetObjects.get(0);
            commandResolver.addContext("current.", targetObject.getExposedProperties());
            commandResolver.addContext("current.", targetObject.getExposedBehaviors());
        }

        final Command resolvedCommand = commandResolver.resolve(command);
        //it's not a user level command for objects (eg: turn it on), it is for another kind of actuator
        Command reply = busService.send(resolvedCommand); //blocking wait until executed

        if (reply == null) {
            command.setExecuted(false);
            LOG.warn("Unreceived reply within given time ({} ms) for command \"{}\"", command.getReplyTimeout(), command.getName());
            notifyMessage("Unreceived reply within given time for command \"" + command.getName() + "\"");
        } else {
            if (reply.isExecuted()) {
                //the reply is executed so mark the origial command as executed as well
                command.setExecuted(true);
                LOG.debug("Executed successfully \"{}\"", command.getName());
            } else {
                command.setExecuted(false);
                LOG.warn("Unable to execute command \"{}\". Skipping the others", command.getName());
                notifyMessage("Unable to execute command \"" + command.getName() + "\"");
                // skip the other commands
                return false;
            }
        }
        
        return true;
    }

    /**
     * Resolves the additional conditions of the reaction in input. Now it just
     * takes the statement attribute and value and check if they are equal to
     * the target behavior name and value respectively. This should be improved
     * to allow also REGEX and other statement resolution.
     */
    private boolean checkAdditionalConditions(Reaction rea) {
        boolean result = true;
        for (Condition condition : rea.getConditions()) {
            EnvObjectLogic object = thingsRepository.findByName(condition.getTarget()).get(0);
            Statement statement = condition.getStatement();
            if (object != null) {
                String attributeValue = object.getBehavior(statement.getAttribute()).getValueAsString();
                String operand = statement.getOperand();
                String value = statement.getValue();
                boolean isAndCondition = "AND".equalsIgnoreCase(statement.getLogical());
               
                if(StringUtils.isBlank(value))
                	value = "";

                //check if value is an external object behavior eg. <value>[object name].temperature</value>
                //or a target object behavior <value>[].temperature</value> 
                try {
					value = this.getCurrentValueFromBehavior(value, object);
				} catch (FreedomoticException e) {
					LOG.warn(e.getMessage());
					return false;
				}
                  
                // if attributeValue and value are float and operand not "EQUALS" we must convert them to integer
                if ((isDecimalNumber(attributeValue) || isDecimalNumber(value)) && !("EQUALS".equalsIgnoreCase(operand))) {
                    attributeValue = this.increaseStringValue(attributeValue);
                    value = this.increaseStringValue(value);
                }
                
                boolean eval = (boolean) new ExpressionFactory<>().createExpression(attributeValue, operand, value).evaluate();
                result = executeBooleanExpression(isAndCondition, result, eval);            
            }
        } // all is ok
        return result;
    }

    private String increaseStringValue(String value) {
    	return String.valueOf((int) Float.parseFloat(value) * 10);
    }
    
    private String getCurrentValueFromBehavior(String currValue, EnvObjectLogic object) throws FreedomoticException {
    	String currentValue = currValue;
    	Matcher m = PATTERN.matcher(currentValue);	
        if (m.find()) {
        	BehaviorLogic behavior = null;
        	String valueBehavior = currentValue.substring(currentValue.indexOf('.') + 1);
            // in this case we consider the target object behavior 
            if (currentValue.startsWith("[]")) {
                behavior = object.getBehavior(valueBehavior);
            } else {
            	String thingName = currentValue.substring(currentValue.indexOf('[') + 1, currentValue.indexOf(']'));
                List<EnvObjectLogic> newObject = thingsRepository.findByName(thingName); 
                if (newObject.isEmpty() || newObject.get(0).getBehavior(valueBehavior)==null) {
                    throw new FreedomoticException("Cannot test condition on unexistent thing: "+thingName);
                } else {
                	behavior = newObject.get(0).getBehavior(valueBehavior);
                }
            }
            
            if (behavior != null) {
            	currentValue = behavior.getValueAsString();
            }
        } 
        return currentValue;
    }
    
    private boolean executeBooleanExpression(boolean isAndCondition, boolean result, boolean eval) {
    	 if (isAndCondition) {
             return result && eval;
         } else {
             return result || eval;
         }
    }
    
    /**
     * Checks if a string represents a decimal number.
     *
     * @param number string to check
     * @return true if it's a decimal number, false otherwise
     */
    private boolean isDecimalNumber(String number) {
        String decimalPattern = "([0-9]*)\\.([0-9]*)";
        return Pattern.matches(decimalPattern, number);
    }

    /**
     * Notifies a message as callout on frontends.
     *
     * @param message message to notify
     */
    private void notifyMessage(String message) {
        MessageEvent event = new MessageEvent(this, message);
        event.setType("callout"); //display as callout on frontends
        busService.send(event);
    }
}
