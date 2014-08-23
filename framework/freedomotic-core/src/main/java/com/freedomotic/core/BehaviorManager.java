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
package com.freedomotic.core;

import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.objects.BehaviorLogic;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.reactions.Command;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final Logger LOG = Logger.getLogger(BehaviorManager.class.getName());
    private static final String MESSAGING_CHANNEL = "app.events.sensors.behavior.request.objects";
    private BusMessagesListener listener;
    private final BusService busService;
    private static API api = Freedomotic.INJECTOR.getInstance(API.class);

    static String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

    public BehaviorManager() {
        this.busService = Freedomotic.INJECTOR.getInstance(BusService.class);
        register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        listener = new BusMessagesListener(this);
        listener.consumeCommandFrom(getMessagingChannel());
    }

    @Override
    public final void onMessage(final ObjectMessage message) {

        // TODO LCG Boiler plate code, move this idiom to an abstract superclass.
        Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        if (jmsObject instanceof Command) {

            Command command = (Command) jmsObject;

            parseCommand(command);

            // reply to the command to notify that is received it can be
            // something like "turn on light 1"
            sendReply(message, command);
        }
    }

    private static void applyToCategory(Command userLevelCommand) {

        // gets a reference to an EnvObject using the key 'object' in the user
        // level command
        List<String> objNames = EnvObjectPersistence.getObjectsNames();
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

    private static List<String> filterByTags(Command userLevelCommand, List<String> origList) {
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

            for (EnvObjectLogic object : api.objects().list()) {
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
                    LOG.config("Filter by tag affects object " + pojo.getName());
                    newList.add(pojo.getName());
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private static List<String> filterByObjClass(Command userLevelCommand, List<String> origList) {
        String objectClass = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_CLASS);

        if (objectClass != null) {
            List<String> newList = new ArrayList<String>();
            String regex = "^" + objectClass.replace(".", "\\.") + ".*";
            Pattern pattern = Pattern.compile(regex);

            for (EnvObjectLogic object : api.objects().list()) {

                final EnvObject pojo = object.getPojo();
                final Matcher matcher = pattern.matcher(pojo.getType());

                if (matcher.matches()) {
                    LOG.config("Filter by class affects object " + pojo.getName());
                    newList.add(pojo.getName());
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private static List<String> filterByZone(Command userLevelCommand, List<String> origList) {
        String zoneName = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_ZONE);

        if (zoneName != null) {
            List<String> newList = new ArrayList<String>();

            //Search for the 'object.zone' name in all environments
            for (EnvironmentLogic env : api.environments().list()) {
                if (zoneName != null) {
                    ZoneLogic z = env.getZone(zoneName);
                    for (EnvObject obj : z.getPojo().getObjects()) {
                        LOG.config("Filter by zone affects object " + obj.getName());
                        newList.add(obj.getName());
                    }
                }
            }
            // Filter out the objects wich are not affected
            origList.retainAll(newList);
        }
        return origList;
    }

    private static void applyToSingleObject(Command userLevelCommand) {

        // gets a reference to an EnvObject using the key 'object' in the user
        // level command
        EnvObjectLogic obj = EnvObjectPersistence
                .getObjectByName(userLevelCommand.getProperty(Command.PROPERTY_OBJECT));

        // if the object exists
        if (obj != null) {

            // gets the behavior name in the user level command
            String behaviorName = userLevelCommand.getProperty(Command.PROPERTY_BEHAVIOR);
            BehaviorLogic behavior = obj.getBehavior(behaviorName);

            // if this behavior exists in object obj
            if (behavior != null) {

                LOG.log(Level.CONFIG,
                        "User level command ''{0}'' request changing behavior {1} of object ''{2}'' "
                        + "from value ''{3}'' to value ''{4}''",
                        new Object[]{userLevelCommand.getName(), behavior.getName(), obj.getPojo().getName(), behavior.getValueAsString(), userLevelCommand.getProperties().getProperty("value")});

                // true means a command must be fired
                behavior.filterParams(userLevelCommand.getProperties(), true);

            } else {
                LOG.log(Level.WARNING,
                        "Behavior ''{0}'' is not a valid behavior for object ''{1}''. "
                        + "Please check ''behavior'' parameter spelling in command {2}",
                        new Object[]{behaviorName, obj.getPojo().getName(), userLevelCommand.getName()});
            }
        } else {
            LOG.log(Level.WARNING, "Object ''{0}"
                    + "'' don''t exist in this environment. "
                    + "Please check ''object'' parameter spelling in command {1}",
                    new Object[]{userLevelCommand.getProperty(Command.PROPERTY_OBJECT), userLevelCommand.getName()});
        }
    }

    /**
     *
     * @param userLevelCommand
     */
    protected static void parseCommand(Command userLevelCommand) {

        if (userLevelCommand.getProperty(Command.PROPERTY_BEHAVIOR) != null) {

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
                        LOG.log(Level.SEVERE, null, ex);
                    }
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
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        }
    }
}
