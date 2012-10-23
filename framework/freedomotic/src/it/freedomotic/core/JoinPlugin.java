/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.model.ds.Config;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
public class JoinPlugin implements BusConsumer {

    private static CommandChannel channel;

    static String getMessagingChannel() {
        return "app.plugin.create";
    }

    public JoinPlugin() {
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
            //here a plugin manifest is expected
            Config manifest = (Config) message.getObject();
            Plugin plugin = new Plugin(manifest.getProperty("name"), manifest);
            Freedomotic.clients.enqueue(plugin);
        } catch (JMSException ex) {
            Freedomotic.logger.severe("Join Plugin receives a not valid plugin manifest");
        }
    }
    
}
