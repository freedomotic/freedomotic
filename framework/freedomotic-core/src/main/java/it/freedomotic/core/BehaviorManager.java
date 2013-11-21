/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.BusService;
import it.freedomotic.bus.BusMessagesListener;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.reactions.Command;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;

/**
 * Translates a generic request like 'turn on light 1' into a series of hardware
 * commands like 'send to serial COM1 the string A01AON' using the mapping
 * between abstract action -to-> concrete action defined in every environment
 * object, for lights it can be something like 'turn on' -> 'turn on X10 light'
 *
 * <p> This class is listening on channels:
 * app.events.sensors.behavior.request.objects If a command (eg: 'turn on
 * light1' or 'turn on all lights') is received on this channel the requested
 * behavior is applied to the single object or all objects of the same type as
 * described by the command parameters. </p>
 *
 * @author Enrico
 */
public final class BehaviorManager
        implements BusConsumer {

    private static final Logger LOG = Logger.getLogger(BehaviorManager.class.getName());
    private static final String MESSAGING_CHANNEL = "app.events.sensors.behavior.request.objects";
    public static final String PROPERTY_BEHAVIOR = "behavior";
    public static final String PROPERTY_OBJECT_CLASS = "object.class";
    public static final String PROPERTY_OBJECT_ADDRESS = "object.address";
    public static final String PROPERTY_OBJECT_NAME = "object.name";
    public static final String PROPERTY_OBJECT_PROTOCOL = "object.protocol";
    public static final String PROPERTY_OBJECT = "object";
    private static BusMessagesListener listener;
    private BusService busService;

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

        String objectClass = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_CLASS);
        if (objectClass != null) {
            String regex = "^" + objectClass.replace(".", "\\.") + ".*";
            Pattern pattern = Pattern.compile(regex);

            // TODO this should be in the collection
            final Collection<EnvObjectLogic> objectList = EnvObjectPersistence
                    .getObjectList();

            for (EnvObjectLogic object : objectList) {

                final EnvObject pojo = object.getPojo();
                final Matcher matcher = pattern.matcher(pojo.getType());

                if (matcher.matches()) {
                    // TODO Look at this setProperty later
                    userLevelCommand.setProperty(Command.PROPERTY_OBJECT, pojo.getName());
                    applyToSingleObject(userLevelCommand);
                }
            }
        }

        String includeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_INCLUDETAGS);
        String excludeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_EXCLUDETAGS);
        if (includeTags != null && excludeTags != null) {
            // prepare includ set
            String tags[] = includeTags.split(",");
            Set<String> includeSearch = new HashSet<String>();
            for (String tag : tags) {
                if (!tag.isEmpty()) {
                    includeSearch.add(tag.trim());
                }
            }
            //prepare exclude set (remove tags listed in include set too)
            tags = excludeTags.split(",");
            Set<String> excludeSearch = new HashSet<String>();
            for (String tag : tags) {
                if (!tag.isEmpty() && !includeSearch.contains(tag)) {
                    excludeSearch.add(tag.trim());
                }
            }

            Set<String> testSet = new HashSet<String>();
            Set<String> extestSet = new HashSet<String>();

            for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
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

                    // TODO Look at this setProperty later
                    userLevelCommand.setProperty(Command.PROPERTY_OBJECT, pojo.getName());
                    applyToSingleObject(userLevelCommand);
                }
            }
        }
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

                LOG.config("User level command '" + userLevelCommand.getName()
                        + "' request changing behavior " + behavior.getName()
                        + " of object '" + obj.getPojo().getName()
                        + "' from value '" + behavior.getValueAsString()
                        + "' to value '" + userLevelCommand.getProperties().getProperty("value")
                        + "'");

                // true means a command must be fired
                behavior.filterParams(userLevelCommand.getProperties(), true);

            } else {
                LOG.warning("Behavior '"
                        + behaviorName
                        + "' is not a valid behavior for object '"
                        + obj.getPojo().getName()
                        + "'. Please check 'behavior' parameter spelling in command "
                        + userLevelCommand.getName());
            }
        } else {
            LOG.warning("Object '"
                    + userLevelCommand.getProperty(Command.PROPERTY_OBJECT)
                    + "' don't exist in this environment. "
                    + "Please check 'object' parameter spelling in command "
                    + userLevelCommand.getName());
        }
    }

    protected static void parseCommand(Command userLevelCommand) {

        String object = userLevelCommand.getProperty(Command.PROPERTY_OBJECT);
        String objectClass = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_CLASS);
        String objectincludeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_INCLUDETAGS);
        String objectexcludeTags = userLevelCommand.getProperty(Command.PROPERTY_OBJECT_EXCLUDETAGS);
        String behavior = userLevelCommand.getProperty(Command.PROPERTY_BEHAVIOR);

        if (behavior != null) {

            if (object != null) {
                /*
                 * if we have the object name and the behavior it means the
                 * behavior must be applied only to the given object name.
                 */
                applyToSingleObject(userLevelCommand);
            } else {

                if (objectClass != null || objectincludeTags != null || objectexcludeTags != null) {
                    /*
                     * if we have the category and the behavior (and not the
                     * object name) it means the behavior must be applied to all
                     * object belonging to the given category. eg: all lights on
                     */
                    applyToCategory(userLevelCommand);
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
