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

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Translates a generic request like 'turn on light 1' into a series of hardware
 * commands like 'send to serial COM1 the string A01AON' using the mapping
 * between abstract action -to-> concrete action defined in every environment
 * object, for lights it can be something like 'turn on' -> 'turn on X10 light'
 *
 * <p>
 * This class is listening on channels:
 * app.events.sensors.behavior.request.objects If a command (eg: 'turn on
 * light1' or 'turn on all lights') is received on this channel the requested
 * behavior is applied to the single object or all objects of the same type as
 * described by the command parameters. </p>
 *
 * @author Enrico
 */
public final class BehaviorManager implements BusConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(BehaviorManager.class.getName());
    private static final String MESSAGING_CHANNEL = "app.events.sensors.behavior.request.objects";
    private BusMessagesListener listener;

    // Dependencies
    private final BusService busService;
    private final ThingRepository thingsRepository;
    private final EnvironmentRepository environmentRepository;

    @Inject
    BehaviorManager(BusService busService, ThingRepository thingsRepository, EnvironmentRepository environmentRepository) {
        this.busService = busService;
        this.thingsRepository = thingsRepository;
        this.environmentRepository = environmentRepository;
        register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        listener = new BusMessagesListener(this, busService);
        listener.consumeCommandFrom(getMessagingChannel());
    }

    @Override
    public final void onMessage(final ObjectMessage message) {

        // TODO LCG Boiler plate code, move this idiom to an abstract superclass.
        Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            LOG.error(ex.getMessage());
        }

        if (jmsObject instanceof Command) {

            Command command = (Command) jmsObject;

            parseCommand(command);

            // reply to the command to notify that is received it can be
            // something like "turn on light 1"
            sendReply(message, command);
        }
    }

    private void applyToCategory(Command userLevelCommand) {

        // gets a reference to an EnvObject using the key 'object' in the user
        // level command
        List<String> objNames = getObjectsNames();
        // filter first tags, then class, then zone
        List<String> affectedObjects
                = filterByZone(userLevelCommand,
                        filterByObjClass(userLevelCommand,
                                filterByTags(userLevelCommand, objNames)));
        //Execute the command on all affected objects
        for (String objName : affectedObjects) {
            userLevelCommand.setProperty(Command.PROPERTY_OBJECT, objName);
            applyToSingleObject(userLevelCommand);
        }
    }

    private List<String> filterByTags(Command userLevelCommand, List<String> origList) {
        String includeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_INCLUDETAGS);
        String excludeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_EXCLUDETAGS);
        if (includeTags != null || excludeTags != null) {
            List<String> newList = new ArrayList<String>();
            // prepare includ set
            includeTags += "";
            String tags[] = includeTags.split(",");
            Set<String> includeSearch = new HashSet<String>();
            for (String tag : tags) {
                if (!tag.isEmpty()) {
                    includeSearch.add(tag.trim());
                }
            }
            //prepare exclude set (remove tags listed in include set too)
            excludeTags += "";
            tags = excludeTags.split(",");
            Set<String> excludeSearch = new HashSet<String>();
            for (String tag : tags) {
                if (!tag.isEmpty() && !includeSearch.contains(tag)) {
                    excludeSearch.add(tag.trim());
                }
            }

            Set<String> testSet = new HashSet<String>();
            Set<String> extestSet = new HashSet<String>();

            for (EnvObjectLogic object : thingsRepository.findAll()) {
                final EnvObject pojo = object.getPojo();
                boolean apply;
                testSet.clear();
                extestSet.clear();

                // applies to objects that do not contain any exclused tag
                testSet.addAll(excludeSearch);
                testSet.retainAll(pojo.getTagsList());
                // if object contains forbidden tags, testSet is not empty
                apply = testSet.isEmpty();
                // if above is false, skip check for admitted tags.
                if (apply && !includeSearch.isEmpty()) {
                    // AND operation between searchSet and object's tag list
                    testSet.addAll(includeSearch);
                    testSet.retainAll(pojo.getTagsList());
                    // if object contains ANY of admitted tags, tastSet is populated
                    apply = !testSet.isEmpty();
                }

                if (apply) {
                    LOG.info("Filter by tag affects object " + pojo.getName());
                    newList.add(pojo.getName());
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private List<String> filterByObjClass(Command userLevelCommand, List<String> origList) {
        String objectClass = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_CLASS);

        if (objectClass != null) {
            List<String> newList = new ArrayList<String>();
            String regex = "^" + objectClass.replace(".", "\\.") + ".*";
            Pattern pattern = Pattern.compile(regex);

            for (EnvObjectLogic object : thingsRepository.findAll()) {

                final EnvObject pojo = object.getPojo();
                final Matcher matcher = pattern.matcher(pojo.getType());

                if (matcher.matches()) {
                    LOG.info("Filter by class affects object " + pojo.getName());
                    newList.add(pojo.getName());
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private List<String> filterByZone(Command userLevelCommand, List<String> origList) {
        String zoneName = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_ZONE);

        if (zoneName != null) {
            List<String> newList = new ArrayList<String>();

            //Search for the 'object.zone' name in all environments
            for (EnvironmentLogic env : environmentRepository.findAll()) {
                if (zoneName != null) {
                    ZoneLogic z = env.getZone(zoneName);
                    for (EnvObject obj : z.getPojo().getObjects()) {
                        LOG.info("Filter by zone affects object " + obj.getName());
                        newList.add(obj.getName());
                    }
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private void applyToSingleObject(Command userLevelCommand) {

        // gets a reference to an EnvObject using the key 'object' in the user
        // level command
        List<EnvObjectLogic> things = thingsRepository.findByName(userLevelCommand.getProperty(Command.PROPERTY_OBJECT));

        // if the object exists
        if (!things.isEmpty()) {
            for (EnvObjectLogic thing : things) {

                // gets the behavior name in the user level command
                String behaviorName = userLevelCommand.getProperty(Command.PROPERTY_BEHAVIOR);
                BehaviorLogic behavior = thing.getBehavior(behaviorName);

                // if this behavior exists in object obj
                if (behavior != null) {

                    LOG.info(
                            "User level command ''{}'' request changing behavior {} of object ''{}'' "
                            + "from value ''{}'' to value ''{}''",
                            new Object[]{userLevelCommand.getName(), behavior.getName(), thing.getPojo().getName(), behavior.getValueAsString(), userLevelCommand.getProperties().getProperty("value")});

                    // true means a command must be fired
                    behavior.filterParams(userLevelCommand.getProperties(), true);

                } else {
                    LOG.warn(
                            "Behavior ''{}'' is not a valid behavior for object ''{}''. "
                            + "Please check ''behavior'' parameter spelling in command {}",
                            new Object[]{behaviorName, thing.getPojo().getName(), userLevelCommand.getName()});
                }
            }
        } else {
            LOG.warn("Object ''{}"
                    + "'' don''t exist in this environment. "
                    + "Please check ''object'' parameter spelling in command {}",
                    new Object[]{userLevelCommand.getProperty(Command.PROPERTY_OBJECT), userLevelCommand.getName()});
        }

    }

    /**
     *
     * @param userLevelCommand
     */
    protected void parseCommand(Command userLevelCommand) {

        if (userLevelCommand.getProperty(Command.PROPERTY_BEHAVIOR) == null) {
            throw new IllegalArgumentException("Command '" + userLevelCommand.getName() + "' has not behavior property defined");
        }

        if (userLevelCommand.getProperty(Command.PROPERTY_OBJECT) != null) {
            /*
             * if we have the object name and the behavior it means the
             * behavior must be applied only to the given object name.
             */
            applyToSingleObject(userLevelCommand);
        } else {

            if (userLevelCommand.getProperty(Command.PROPERTY_OBJECT_CLASS) != null
                    || userLevelCommand.getProperty(Command.PROPERTY_OBJECT_INCLUDETAGS) != null
                    || userLevelCommand.getProperty(Command.PROPERTY_OBJECT_EXCLUDETAGS) != null
                    || userLevelCommand.getProperty(Command.PROPERTY_OBJECT_ENVIRONMENT) != null
                    || userLevelCommand.getProperty(Command.PROPERTY_OBJECT_ZONE) != null) {
                try {
                    /*
                     * if we have the category and the behavior (and not the
                     * object name) it means the behavior must be applied to all
                     * object belonging to the given category. eg: all lights on
                     */
                    Command clonedOne = userLevelCommand.clone();
                    applyToCategory(clonedOne);
                } catch (CloneNotSupportedException ex) {
                    LOG.error(ex.getMessage());
                }
            }
        }

    }

    /**
     * @param message
     * @param command
     */
    public void sendReply(final ObjectMessage message, Command command) {

        try {

            Destination jmsReplyTo = message.getJMSReplyTo();
            if (jmsReplyTo != null) {

                final String jmsCorrelationID = message.getJMSCorrelationID();
                busService.reply(command, jmsReplyTo, jmsCorrelationID);
            }

        } catch (JMSException ex) {
            LOG.error("Error while sending reply to " + command.getName(), ex);
        }
    }

    private List<String> getObjectsNames() {
        List<String> names = new ArrayList<String>();
        for (EnvObjectLogic thing : thingsRepository.findAll()) {
            names.add(thing.getPojo().getName());
        }
        return names;
    }

    public static String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }
}
