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
package it.freedomotic.core;

import com.thoughtworks.xstream.XStream;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.Behavior;

import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;

import it.freedomotic.reactions.Command;

import java.lang.String;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

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

    private static CommandChannel channel;

    static String getMessagingChannel() {
        return "app.events.sensors.behavior.request.objects";
    }

    public BehaviorManager() {
        register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        channel = new CommandChannel();
        channel.setHandler(this);
        channel.consumeFrom(getMessagingChannel());
    }

    @Override
    public final void onMessage(final ObjectMessage message) {
        Object jmsObject = null;

        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            Logger.getLogger(BehaviorManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (jmsObject instanceof Command) {
            Command command = (Command) jmsObject;
            //reply to the command to notify that is received it can be something like "turn on light 1"
            parseCommand(command);

            try {
                if (message.getJMSReplyTo() != null) {
                    channel.reply(command,
                            message.getJMSReplyTo(),
                            message.getJMSCorrelationID());
                }
            } catch (JMSException ex) {
                LOG.severe(Freedomotic.getStackTraceInfo(ex));
            }
        }
    }

    protected static void parseCommand(Command command) {
        Command userLevelCommand = command;

        //getting all info we need from the command
        String object = userLevelCommand.getProperty("object");
        String objectClass = userLevelCommand.getProperty("object.class");
        String behavior = userLevelCommand.getProperty("behavior");
        String zone = userLevelCommand.getProperty("zone");

        /*
         * if we have the object name and the behavior it meass the behavior
         * must be applied only to the given object name.
         */
        if ((behavior != null) && (object != null)) {
            applyToSingleObject(userLevelCommand);
        } else {
            /*
             * if we have the category and the behavior (and not the object
             * name) it means the behavior must be applied to all object
             * belonging to the given category. eg: all lights on
             */
            if ((behavior != null) && (objectClass != null)) {
                if (zone != null) {
                    //apply behavior to all instances of objectClass (eg: lights) in zone (eg:kitchen)
                    applyToZone(userLevelCommand);
                } else {
                    //apply behavior to all instances of objectClass
                    applyToCategory(userLevelCommand);
                }
            }
        }
    }

    private static void applyToSingleObject(Command userLevelCommand) {
//        RelativeBehavior relative = null;
        BehaviorLogic behavior = null;
        // Config param = null;
        //gets the behavior name in the user level command
        String behaviorName = userLevelCommand.getProperty("behavior");

        //gets a reference to an EnvObject using the key 'object' in the user level command
        EnvObjectLogic obj = EnvObjectPersistence.getObjectByName(userLevelCommand.getProperty("object"));

        if (obj != null) { //if the object exists
            behavior = obj.getBehavior(behaviorName);

            if (behavior != null) { //if this behavior exists in object obj
                LOG.config("User level command '" + userLevelCommand.getName()
                        + "' request changing behavior " + behavior.getName() + " of object '"
                        + obj.getPojo().getName() + "' from value '"
                        + behavior.getValueAsString() + "' to value '"
                        + userLevelCommand.getProperties().getProperty("value") + "'");
                behavior.filterParams(userLevelCommand.getProperties(),
                        true); //true means a command must be fired
            } else {
                LOG.warning("Behavior '" + behaviorName + "' is not a valid behavior for object '"
                        + obj.getPojo().getName()
                        + "'. Please check 'behavior' parameter spelling in command "
                        + userLevelCommand.getName());
            }
        } else {
            LOG.warning("Object '" + userLevelCommand.getProperty("object")
                    + "' don't exist in this environment. "
                    + "Please check 'object' parameter spelling in command "
                    + userLevelCommand.getName());
        }
    }

    private static void applyToCategory(Command userLevelCommand) {
        // Behavior behavior = null;
        // Config param = null;
        //gets a reference to an EnvObject using the key 'object' in the user level command
        String objectClass = userLevelCommand.getProperty("object.class");
        Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator();
        String regex = "^" + objectClass.replace(".", "\\.") + ".*";

        while (it.hasNext()) {
            EnvObjectLogic object = it.next();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(object.getPojo().getType());

            if (matcher.matches()) {
                userLevelCommand.setProperty("object",
                        object.getPojo().getName());
                applyToSingleObject(userLevelCommand);
            }
        }
    }

    private static void applyToZone(Command userLevelCommand) {
        
		//	Behavior behavior = null;
		//	Config param = null;
        
        //gets a reference to an EnvObject using the key 'object' in the user level command
        String objectClass = userLevelCommand.getProperty("object.class");
        Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator();
        String regex = "^" + objectClass.replace(".", "\\.") + ".*";

        while (it.hasNext()) {
            EnvObjectLogic object = it.next();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(object.getPojo().getType());

            if (matcher.matches()) {

            	//TODO: and is in the zone
                //add another if
                // String zone = userLevelCommand.getProperty("zone");
                
                userLevelCommand.setProperty("object", object.getPojo().getName());
                applyToSingleObject(userLevelCommand);
            }
        }
    }
    private static final Logger LOG = Logger.getLogger(BehaviorManager.class.getName());
}
