/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.api.Client;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.model.object.Behavior;
import it.freedomotic.model.object.Representation;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.plugins.ObjectPlugin;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.UidGenerator;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
public class JoinDevice implements BusConsumer {

    private static CommandChannel channel;

    static String getMessagingChannel() {
        return "app.objects.create";
    }

    public JoinDevice() {
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

    protected static void join(String clazz, String name, String protocol, String address) {
        try {
            ObjectPlugin client = (ObjectPlugin) Freedomotic.clients.get(clazz);
            if (client == null) {
                Freedomotic.logger.warning("Doesen't exist an object class called " + clazz);
                return;
            }
            System.out.println(client.getName());
            File exampleObject = client.getExample();
            EnvObjectLogic loaded = EnvObjectPersistence.loadObject(exampleObject, true);
            System.out.println(loaded.getPojo().getName());
            //changing the name and other properties invalidates related trigger and commands
            //call init() again after this changes
            if (name != null && !name.isEmpty()) {
                loaded.getPojo().setName(name);
            } else {
                loaded.getPojo().setName(protocol + "-" + UidGenerator.getNextStringUid());
            }
            loaded.getPojo().setProtocol(protocol);
            loaded.getPojo().setPhisicalAddress(address);
            loaded.setRandomLocation();

            //use the preferred mapping of the protocol plugin
            Client addon = Freedomotic.clients.getClientByProtocol(protocol);
            if (addon != null) {
                for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
                    HashMap tuple = addon.getConfiguration().getTuples().getTuple(i);
                    String regex = (String) tuple.get("object.class");
                    if (clazz.matches(regex)) {
                        //map object behaviors to hardware triggers
                        for (Behavior behavior : loaded.getPojo().getBehaviors()) {
                            String triggerName = (String) tuple.get(behavior.getName());
                            loaded.addTriggerMapping(TriggerPersistence.getTrigger(triggerName), behavior.getName());
                        }
                        //map object actions to hardware commands
                        Iterator it = loaded.getPojo().getActions().stringPropertyNames().iterator();
                        while (it.hasNext()) {
                            String action = (String) it.next();
                            System.out.println("action is " + action);
                            String commandName = (String) tuple.get(action);
                            if (commandName != null) {
                                loaded.setAction(action, CommandPersistence.getHardwareCommand(commandName));
                            }
                        }
                    }
                }
            }
            //validate the object
            loaded.init();
        } catch (IOException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            Object jmsObject = message.getObject();
            if (jmsObject instanceof Command) {
                Command command = (Command) jmsObject;
                System.out.println("received " + command.getName());
                String name = command.getProperty("object.name");
                String protocol = command.getProperty("object.protocol");
                String address = command.getProperty("object.address");
                String clazz = command.getProperty("object.class");
                join(clazz, name, protocol, address);
            }
        } catch (JMSException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
