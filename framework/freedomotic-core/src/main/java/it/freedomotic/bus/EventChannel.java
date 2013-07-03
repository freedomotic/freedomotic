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
package it.freedomotic.bus;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.app.Profiler;
import it.freedomotic.util.UidGenerator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;

public class EventChannel extends AbstractBusConnector implements MessageListener {

    private BusConsumer handler;
    private javax.jms.Queue virtualTopic = null;
    private MessageProducer producer;
    private MessageConsumer consumer;

    /*produce on a queue and listen on another queue*/
    public EventChannel() {
        super();
        try {
            producer = sendSession.createProducer(null);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (JMSException ex) {
            Logger.getLogger(CommandChannel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void consumeFrom(String topicName) {
        if (handler != null) {
            try {
                virtualTopic = receiveSession.createQueue("Consumer." + UidGenerator.getNextStringUid() + ".VirtualTopic." + topicName);
                javax.jms.MessageConsumer subscriber = receiveSession.createConsumer(virtualTopic);
                subscriber.setMessageListener(this);
                Freedomotic.logger.config(getHandler().getClass().getSimpleName() + " listen on " + virtualTopic.toString());
            } catch (javax.jms.JMSException jmse) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jmse));
            }
        }
    }

    public void consumeFrom(String channel, final Method method) {
        if (method != null) {
            try {
                virtualTopic = receiveSession.createQueue("Consumer." + UidGenerator.getNextStringUid() + ".VirtualTopic." + channel);
                javax.jms.MessageConsumer subscriber = receiveSession.createConsumer(virtualTopic);
                subscriber.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessage(Message msg) {
                        if (msg instanceof ObjectMessage) {
                            ObjectMessage objectMessage = (ObjectMessage) msg;
                            try {
                                EventTemplate event = (EventTemplate) objectMessage.getObject();
                                method.invoke(handler, event);
                            } catch (IllegalAccessException ex) {
                                Logger.getLogger(EventChannel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (IllegalArgumentException ex) {
                                Logger.getLogger(EventChannel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InvocationTargetException ex) {
                                Logger.getLogger(EventChannel.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (Exception e) {
                                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                            }
                        }
                    }
                });
                Freedomotic.logger.config(getHandler().getClass().getSimpleName() + " listen on " + virtualTopic.toString());
            } catch (javax.jms.JMSException jmse) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(jmse));
            }
        }
    }

    public void send(EventTemplate ev) {
        send(ev, ev.getDefaultDestination());
    }

    public void send(final EventTemplate ev, final String to) {
        if (ev != null) {
            try {
                ObjectMessage msg = sendSession.createObjectMessage();
                msg.setObject(ev);
                //a consumer consumes on Consumer.A_PROGRESSIVE_INTEGER_ID.VirtualTopic.
                javax.jms.Topic tmpTopic = sendSession.createTopic("VirtualTopic." + to);
                Profiler.incrementSentEvents();
                producer.send(tmpTopic, msg);
            } catch (JMSException ex) {
                Logger.getLogger(EventChannel.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Freedomotic receives something from the bus. It can be an event or a
     * command. This is the bus hook for any java class that use the freedomotic
     * bus. Classes register itself as handlers on this class.
     *
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

    public void unsubscribe() {
        try {
            producer.close();
            consumer.close();
            receiveSession.unsubscribe(virtualTopic.toString());
        } catch (JMSException ex) {
            Freedomotic.logger.severe("Unable to unsubscribe from event channel " + virtualTopic.toString());
        } catch (Exception e){
            Freedomotic.logger.warning(e.getMessage());
        }
    }
}
