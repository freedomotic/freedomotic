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

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.BusService;
import it.freedomotic.bus.BusMessagesListener;
import it.freedomotic.reactions.Command;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.google.inject.Inject;

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

    private static final Logger LOG = Logger.getLogger(BehaviorManager.class.getName());

	private static final String MESSAGING_CHANNEL = "app.events.sensors.behavior.request.objects";

	private static BusMessagesListener listener;

	private BusService busService;
	
    static String getMessagingChannel() {
    	return MESSAGING_CHANNEL;
    }

    public BehaviorManager() {
		this.busService = Freedomotic.INJECTOR.getInstance(BusService.class);
       register();
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
    	listener = new BusMessagesListener(this);
    	listener.consumeCommandFrom(getMessagingChannel());
    }

    @Override
    public final void onMessage(final ObjectMessage message) {
        
    	// TODO LCG Boiler plate code, move this idiom to an abstract superclass.
    	Object jmsObject = null;
        try {
            jmsObject = message.getObject();
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        
		if (jmsObject instanceof Command) {

			Command command = (Command) jmsObject;

			command.onCommandMessage();
			
			// reply to the command to notify that is received it can be
			// something like "turn on light 1"
			sendReply(message, command);
		}
    }

    /**
	 * @param message
	 * @param command
	 */
	public void sendReply(final ObjectMessage message, Command command) {

		try {

			Destination jmsReplyTo = message.getJMSReplyTo();
			if (jmsReplyTo != null) {

				final String jmsCorrelationID = message.getJMSCorrelationID();
				busService.reply(command, jmsReplyTo, jmsCorrelationID);
			}

		} catch (JMSException ex) {
			LOG.severe(Freedomotic.getStackTraceInfo(ex));
		}
	}
}
