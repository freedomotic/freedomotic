package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.object.Behavior;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.EnvObjectPersistence;
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
 * Translates a generic request like 'turn on light 1' into a series
 * of hardware commands like 'send to serial COM1 the string A01AON'
 * using the mapping between abstract action -to-> concrete action
 * defined in every environment object, for lights
 * it can be something like 'turn on' -> 'turn on X10 light'
 *
 * <p> This class is listening on channels:
 * app.events.sensors.behavior.request.objects If a command (eg: 'turn on
 * light1' or 'turn on all lights') is received on this channel the requested
 * behavior is applied to the single object or all objects of the same type as
 * described by the command paramenters. </p>
 *
 * @author Enrico
 */
public final class BehaviorManagerForObjects implements BusConsumer {

    private static CommandChannel channel;

    static String getMessagingChannel() {
        return "app.events.sensors.behavior.request.objects";
    }

    public BehaviorManagerForObjects() {
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
    public void onMessage(final ObjectMessage message) {
        Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            Logger.getLogger(BehaviorManagerForObjects.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (jmsObject instanceof Command) {
            Command command = (Command) jmsObject;
            //reply to the command to notify that is received it can be something like "turn on light 1"
            parseCommand(command);
            try {
                if (message.getJMSReplyTo() != null) {
                    channel.reply(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
                }
            } catch (JMSException ex) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
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
        if (behavior != null && object != null) {
            applyToSingleObject(userLevelCommand);
        } else {
            /*
             * if we have the category and the behavior (and not the object
             * name) it means the behavior must be applied to all object
             * belonging to the given category. eg: all lights on
             */
            if (behavior != null && objectClass != null) {
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
        Config param = null;
        //gets the behavior name in the user level command
        String behaviorName = userLevelCommand.getProperty("behavior");
        //gets a reference to an EnvObject using the key 'object' in the user level command
        EnvObjectLogic obj = EnvObjectPersistence.getObject(userLevelCommand.getProperty("object"));


        if (obj != null) { //if the object exists
            behavior = obj.getBehavior(behaviorName);
            if (behavior != null) { //if this behavior exists in object obj
                Freedomotic.logger.config("User level command '" + userLevelCommand.getName() + "' request changing behavior "
                        + behavior.getName() + " of object '" + obj.getPojo().getName()
                        + "' from value '" + behavior.getValueAsString() + "' to value '" + userLevelCommand.getProperties().getProperty("value") + "'");
                behavior.filterParams(userLevelCommand.getProperties(), true); //true means a command must be fired
            } else {
                Freedomotic.logger.warning("Behavior '" + behaviorName + "' is not a valid behavior for object '" + obj.getPojo().getName()
                        + "'. Please check 'behavior' parameter spelling in command " + userLevelCommand.getName());
            }
        } else {
            Freedomotic.logger.warning("Object '" + userLevelCommand.getProperty("object") + "' don't exist in this environment. "
                    + "Please check 'object' parameter spelling in command " + userLevelCommand.getName());
        }
    }

    private static void applyToCategory(Command userLevelCommand) {
        Behavior behavior = null;
        Config param = null;
        //gets a reference to an EnvObject using the key 'object' in the user level command
        String objectClass = userLevelCommand.getProperty("object.class");
        Iterator it = EnvObjectPersistence.iterator();
        String regex = "^" + objectClass.replace(".", "\\.") + ".*";
        while (it.hasNext()) {
            EnvObjectLogic object = (EnvObjectLogic) it.next();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(object.getPojo().getType());
            if (matcher.matches()) {
                userLevelCommand.setProperty("object", object.getPojo().getName());
                applyToSingleObject(userLevelCommand);
            }
        }
    }

    private static void applyToZone(Command userLevelCommand) {
        Behavior behavior = null;
        Config param = null;
        //gets a reference to an EnvObject using the key 'object' in the user level command
        String objectClass = userLevelCommand.getProperty("object.class");
        Iterator it = EnvObjectPersistence.iterator();
        String regex = "^" + objectClass.replace(".", "\\.") + ".*";
        while (it.hasNext()) {
            EnvObjectLogic object = (EnvObjectLogic) it.next();
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(object.getPojo().getType());
            if (matcher.matches()) {
                //TODO: and is in the zone
                //add another if
                String zone = userLevelCommand.getProperty("zone");
                userLevelCommand.setProperty("object", object.getPojo().getName());
                applyToSingleObject(userLevelCommand);
            }
        }
    }
}
