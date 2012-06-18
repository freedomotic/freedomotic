/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.api.Client;
import it.freedomotic.api.ObjectPlaceholder;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.plugins.AddonManager;
import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.reactions.Command;
import java.io.File;
import java.io.IOException;
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
                ObjectPlaceholder client = (ObjectPlaceholder) Freedomotic.clients.get(clazz);
                if (client == null) {
                    Freedomotic.logger.warning("Don't exist an object class called " + clazz);
                    return;
                }
                System.out.println(client.getName());
                File exampleObject = client.getExample();
                EnvObjectLogic loaded = EnvObjectPersistence.loadObject(exampleObject, true);
                System.out.println(loaded.getPojo().getName());
                loaded.getPojo().setName(name);
                loaded.getPojo().setProtocol(protocol);
                loaded.getPojo().setPhisicalAddress(address);
                loaded.getPojo().setName(address);
            }
        } catch (IOException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JMSException ex) {
            Logger.getLogger(JoinDevice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
