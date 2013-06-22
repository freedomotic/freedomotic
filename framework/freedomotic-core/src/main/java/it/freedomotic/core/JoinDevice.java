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
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.plugins.ObjectPlugin;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.TriggerPersistence;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
public final class JoinDevice implements BusConsumer {

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

    protected static EnvObjectLogic join(String clazz, String name, String protocol, String address) {
        EnvObjectLogic loaded = null;
        try {
            ObjectPlugin objectPlugin = (ObjectPlugin) ClientStorage.get(clazz);
            if (objectPlugin == null) {
                Freedomotic.logger.warning("Doesen't exist an object class called " + clazz);
                return null;
            }
            File templateFile = objectPlugin.getTemplate();
            loaded = EnvObjectPersistence.loadObject(templateFile);
            //changing the name and other properties invalidates related trigger and commands
            //call init() again after this changes
            if (name != null && !name.isEmpty()) {
                loaded.getPojo().setName(name);
            } else {
                loaded.getPojo().setName(protocol);
            }
            loaded = EnvObjectPersistence.add(loaded, EnvObjectPersistence.MAKE_UNIQUE);
            loaded.getPojo().setProtocol(protocol);
            loaded.getPojo().setPhisicalAddress(address);
            loaded.setRandomLocation();

            //set the PREFERRED MAPPING of the protocol plugin (if any is defined in its manifest)
            Client addon = Freedomotic.clients.getClientByProtocol(protocol);
            if (addon != null) {
                for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
                    Map<String,String> tuple = addon.getConfiguration().getTuples().getTuple(i);
                    String regex = tuple.get("object.class");
                    if (regex != null && clazz.matches(regex)) {
                        //map object behaviors to hardware triggers
                        for (Behavior behavior : loaded.getPojo().getBehaviors()) {
                            String triggerName =  tuple.get(behavior.getName());
                            loaded.addTriggerMapping(TriggerPersistence.getTrigger(triggerName), behavior.getName());
                        }
                        //map object actions to hardware commands
                        Iterator<String> it = loaded.getPojo().getActions().stringPropertyNames().iterator();
                        while (it.hasNext()) {
                            String action = it.next();
                            String commandName =  tuple.get(action);
                            if (commandName != null) {
                                loaded.setAction(action, CommandPersistence.getHardwareCommand(commandName));
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
        return loaded;
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            Object jmsObject = message.getObject();
            if (jmsObject instanceof Command) {
                Command command = (Command) jmsObject;
                String name = command.getProperty("object.name");
                String protocol = command.getProperty("object.protocol");
                String address = command.getProperty("object.address");
                String clazz = command.getProperty("object.class");
                if (EnvObjectPersistence.getObjectByAddress(protocol, address).isEmpty()) {
                    join(clazz, name, protocol, address);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
