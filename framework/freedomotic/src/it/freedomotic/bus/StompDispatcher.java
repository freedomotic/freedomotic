/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.bus;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.GenericEvent;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.reactions.Command;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * It dispatch data structures (objects, environment topology, plugins status,
 * ...) on STOMP connections
 *
 * @author Enrico
 */
public class StompDispatcher implements BusConsumer {

    private static CommandChannel channel;

    static String getMessagingChannel() {
        return "app.data.request";
    }

    public StompDispatcher() {
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
        System.out.println("stomp reqest received");
        try {
            Command command = (Command) message.getObject();
            XStream xstream = FreedomXStream.getXstream();
            String xml = xstream.toXML(Freedomotic.environment.getPojo());
            System.out.println("XML Message from STOMP received");
            //System.out.println(xml);
            //command.setProperty("plugins", "<![CDATA[\n" + xml + "\n]]>");
            channel.reply(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
        } catch (JMSException ex) {
            Logger.getLogger(StompDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
