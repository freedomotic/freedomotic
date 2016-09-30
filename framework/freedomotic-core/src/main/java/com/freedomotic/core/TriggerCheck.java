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
package com.freedomotic.core;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.rules.Expression;
import com.freedomotic.rules.ExpressionFactory;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            if (trigger.isHardwareLevel()) { //hardware triggers can always fire

                Trigger resolved = resolveTrigger(event, trigger);

                if (resolved.isConsistentWith(event)) {
                    LOG.debug("[CONSISTENT] hardware level trigger ''{}'' {}''\nconsistent with received event ''{}'' {}", new Object[]{resolved.getName(), resolved.getPayload().toString(), event.getEventName(), event.getPayload().toString()});
                    applySensorNotification(resolved, event);
                    return true;
                }
            } else {
                if (trigger.canFire()) {
                    Trigger resolved = resolveTrigger(event, trigger);
                    if (resolved.isConsistentWith(event)) {
                        LOG.debug("[CONSISTENT] registered trigger ''{}'' {}''\nconsistent with received event ''{}'' {}", new Object[]{resolved.getName(), resolved.getPayload().toString(), event.getEventName(), event.getPayload().toString()});
                        executeTriggeredAutomations(resolved, event);
                        return true;
                    }
                }
            }

            //if we are here the trigger is not consistent
            LOG.debug("[NOT CONSISTENT] registered trigger ''{}'' {}''\nnot consistent with received event ''{}'' {}", new Object[]{trigger.getName(), trigger.getPayload().toString(), event.getEventName(), event.getPayload().toString()});

            return false;
        } catch (Exception e) {
            LOG.error("Error while performing trigger check", e);
            return false;
        }
    }

    private Trigger resolveTrigger(final EventTemplate event, final Trigger trigger) throws VariableResolutionException {
        Resolver resolver = new Resolver();
        resolver.addContext("event.", event.getPayload());
        return resolver.resolve(trigger);
    }

    private void applySensorNotification(Trigger resolved, final EventTemplate event) {
        String protocol;
        String address;
        EnvObjectLogic affectedObject = null;

        //join device: add the object on the map if not already there
        //join device requires to know 'object.class' and 'object.name' properties
        protocol = resolved.getPayload().getStatements("event.protocol").get(0).getValue();
        address = resolved.getPayload().getStatements("event.address").get(0).getValue();

        if ((protocol != null) && (address != null)) {
            String clazz = event.getProperty("object.class");
            String name = event.getProperty("object.name");
            String autodiscoveryAllowClones = event.getProperty("autodiscovery.allow-clones");

            affectedObject = thingsRepository.findByAddress(protocol, address);

            if (affectedObject == null) { //there isn't an object with this protocol and address
                LOG.warn("Found a candidate for things autodiscovery: thing ''{}'' of type ''{}''", new Object[]{name, clazz});
                if ((clazz != null) && !clazz.isEmpty()) {
                    boolean allowClones;
                    if (autodiscoveryAllowClones.equalsIgnoreCase("false")) {
                        allowClones = false;
                    } else {
                        allowClones = true;
                    }
                    try {
                        affectedObject = autodiscovery.join(clazz, name, protocol, address, allowClones);
                    } catch (RepositoryException ex) {
                        LOG.error(ex.getMessage());
                    }
                }
            }
        }

        //uses trigger->behavior mapping to apply the trigger to this object
        if (affectedObject != null && affectedObject.executeTrigger(resolved)) {
            long elapsedTime = System.currentTimeMillis() - event.getCreation();
            LOG.info(
                    "Sensor notification ''{}'' applied to object ''{}'' in {}ms.",
                    new Object[]{resolved.getName(), affectedObject.getPojo().getName(), elapsedTime});
        } else {
            LOG.warn("Hardware trigger {} is not associated to any object.", resolved.getName());
        }
        resolved.getPayload().clear();
        event.getPayload().clear();
    }

    private void executeTriggeredAutomations(final Trigger trigger, final EventTemplate event) {
        Runnable automation = new Runnable() {

            @Override
            public void run() {

                //Searching for reactions using this trigger
                boolean found = false;

                for (Reaction reaction : reactionRepository.findAll()) {

                    Trigger reactionTrigger = reaction.getTrigger();

                    //found a related reaction. This must be executed
                    if (trigger.equals(reactionTrigger) && !reaction.getCommands().isEmpty()) {
                        if (!checkAdditionalConditions(reaction)) {
                            LOG.info(
                                    "Additional conditions test failed in reaction {}", reaction.toString());
                            return;
                        }
                        reactionTrigger.setExecuted();
                        found = true;
                        LOG.debug("Try to execute reaction {}", reaction.toString());

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

                                if (command.getReceiver().equalsIgnoreCase(BehaviorManager.getMessagingChannel())) {
                                    //this command is for an object so it needs only to know only about event parameters
                                    Command resolvedCommand = commandResolver.resolve(command);
                                    //doing so we bypass messaging system gaining better performances
                                    behaviorManager.parseCommand(resolvedCommand);
                                } else {
                                    //if the event has a target object we include also object info
                                    List<EnvObjectLogic> targetObjects = thingsRepository.findByName(event.getProperty("object.name"));

                                    if (!targetObjects.isEmpty()) {
                                        EnvObjectLogic targetObject = targetObjects.get(0);
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
                                        LOG.warn(
                                                "Unreceived reply within given time ({}ms) for command {}",
                                                new Object[]{command.getReplyTimeout(), command.getName()});
                                        notifyMessage("Unreceived reply within given time for command " + command.getName());
                                    } else {
                                        if (reply.isExecuted()) {
                                            //the reply is executed so mark the origial command as executed as well
                                            command.setExecuted(true);
                                            LOG.debug("Executed succesfully {}", command.getName());
                                        } else {
                                            command.setExecuted(false);
                                            LOG.warn("Unable to execute command {}. Skipping the others", command.getName());
                                            notifyMessage("Unable to execute command " + command.getName());
                                            // skip the other commands
                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LOG.error("Exception while merging event parameters into reaction.", e);
                            return;
                        }

                        String info = "Executing automation '" + reaction.toString() + "' takes "
                                + (System.currentTimeMillis() - event.getCreation()) + "ms.";
                        LOG.info(info);

                        MessageEvent message = new MessageEvent(null, info);
                        message.setType("callout"); //display as callout on frontends
                        busService.send(message);
                    }
                }

                if (!found) {
                    LOG.info("No valid reaction bound to trigger ''{}''", trigger.getName());
                }
                trigger.getPayload().clear();
                event.getPayload().clear();
            }
        };

        AUTOMATION_EXECUTOR.execute(automation);
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
                BehaviorLogic behavior = object.getBehavior(statement.getAttribute());
                String attributeValue = behavior.getValueAsString();
                String operand = statement.getOperand();
                String value = statement.getValue();
                String valueBehavior;

                //check if value is an object behavior eg. <value>[object name].temperature</value>
                Pattern p = Pattern.compile("\\[(.*?)\\]\\.+[0-9A-Za-z]");
                Matcher m = p.matcher(value);
                if (m.find()) {
                    // in this case we consider the target object behavior 
                    if (value.startsWith("[]")) {
                        valueBehavior = value.substring(value.indexOf(".") + 1);
                        behavior = object.getBehavior(valueBehavior);
                        if (behavior != null) {
                            value = behavior.getValueAsString();
                        }
                    } else {
                        List<EnvObjectLogic> newObject = thingsRepository.findByName(value.substring(value.indexOf("[") + 1, value.indexOf("]")));
                        if (newObject.isEmpty()) {
                            LOG.warn("Cannot test condition on unexistent object: {}", value.substring(value.indexOf("[") + 1, value.indexOf("]")));
                            return false;
                        } else {
                            valueBehavior = value.substring(value.indexOf(".") + 1);
                            behavior = newObject.get(0).getBehavior(valueBehavior);
                            if (behavior != null) {
                                value = behavior.getValueAsString();
                            } else {
                                return false;
                            }
                        }
                    }
                    // if attributeValue and value are float and operand not "EQUALS" we must convert them to integer
                    if ((isDecimalNumber(attributeValue) || isDecimalNumber(value)) && !(operand.equalsIgnoreCase("EQUALS"))) {
                        attributeValue = String.valueOf((int) Float.parseFloat(attributeValue) * 10);
                        value = String.valueOf((int) (Float.parseFloat(value) * 10));
                    }
                    ExpressionFactory factory = new ExpressionFactory<>();
                    Expression exp = factory.createExpression(attributeValue, operand, value);
                    boolean eval = (boolean) exp.evaluate();
                    if (statement.getLogical().equalsIgnoreCase("AND")) {
                        result = result && eval;
                    } else {
                        result = result || eval;
                    }
                } else {
                    // regex fails LOG syntax error
                    LOG.warn("Cannot test condition on unexistent object: {}", condition.getTarget());
                    return false;
                }
            }
        } // all is ok
        return result;
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

    private void notifyMessage(String message) {
        MessageEvent event = new MessageEvent(this, message);
        event.setType("callout"); //display as callout on frontends
        busService.send(event);
    }
}
