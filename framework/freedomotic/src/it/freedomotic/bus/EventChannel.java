/*Copyright 2009 Enrico Nicoletti
eMail: enrico.nicoletti84@gmail.com

This file is part of Freedomotic.

Freedomotic is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
any later version.

Freedomotic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with EventEngine; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.bus;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.app.Profiler;
import it.freedomotic.util.UidGenerator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

public class EventChannel extends AbstractBusConnector implements MessageListener {

    private BusConsumer handler;
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private javax.jms.Queue subscriberVirtualTopic=null;

    /*produce on a queue and listen on another queue*/
    public EventChannel() {
        super();
    }

    public void consumeFrom(String topicName) {
        if (handler != null) {
            try {
                subscriberVirtualTopic = getBusSharedSession().createQueue("Consumer." + UidGenerator.getNextStringUid() + ".VirtualTopic." + topicName);
                javax.jms.MessageConsumer subscriber = getBusSharedSession().createConsumer(subscriberVirtualTopic);
                subscriber.setMessageListener(this);
                Freedomotic.logger.info(getHandler().getClass().getSimpleName() + " listen on " + subscriberVirtualTopic.toString());
            } catch (javax.jms.JMSException jmse) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jmse));
            }
        }
    }

    public void send(EventTemplate ev) {
        send(ev, ev.getDefaultDestination());
    }

    public void send(final EventTemplate ev, final String to) {

        Runnable task = new Runnable() {

            @Override
            public void run() {
                try {
                    if (ev != null) {
                        ObjectMessage msg = getBusSharedSession().createObjectMessage();
                        msg.setObject(ev);
                        //a consumer consumes on Consumer.A_PROGRESSIVE_INTEGER_ID.VirtualTopic.
                        javax.jms.Topic tmpTopic = getBusSharedSession().createTopic("VirtualTopic." + to);
                        Profiler.incrementSentEvents();
                        getBusSharedWriter().send(tmpTopic, msg);
                    }
                } catch (JMSException jMSException) {
                }
            }
        };
        EXECUTOR.execute(task);
    }

    /**
     * Freedomotic receives something from the bus.
     * It can be an event or a command.
     * This is the bus hook for any java class that use the freedomotic bus. Classes register itself as handlers on this class.
     * @param aMessage
     */
    @Override
    public final void onMessage(Message aMessage) {
        Profiler.incrementReceivedEvents();
        if (getHandler() != null) {
            if (aMessage instanceof ObjectMessage) {
                getHandler().onMessage((ObjectMessage) aMessage);
            }
        }
    }

    private BusConsumer getHandler() {
        return handler;
    }

    public void setHandler(BusConsumer handler) {
        this.handler = handler;
    }
    
    public void unsubscribe(){
        try {
            getBusSharedSession().unsubscribe(subscriberVirtualTopic.toString());
        } catch (JMSException ex) {
            Freedomotic.logger.severe("Unable to unsubscribe from event channel " + subscriberVirtualTopic.toString());
        }
    }
}
