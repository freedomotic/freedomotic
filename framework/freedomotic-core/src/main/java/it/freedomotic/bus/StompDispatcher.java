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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.bus;

import it.freedomotic.reactions.Command;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        System.out.println("stomp request received");
        try {
            Command command = (Command) message.getObject();
            //XStream xstream = FreedomXStream.getXstream();
            //String xml = xstream.toXML(Freedomotic.environment.getPojo());
            System.out.println("XML Message from STOMP received");
            channel.reply(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
        } catch (JMSException ex) {
            Logger.getLogger(StompDispatcher.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private static final Logger LOG = Logger.getLogger(StompDispatcher.class.getName());
}
