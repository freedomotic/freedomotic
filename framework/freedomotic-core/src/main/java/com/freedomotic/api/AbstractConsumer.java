/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.api;

import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * TODO: THIS CLASS IT'S NOT FINISHED IT NEEDS TO IMPLEMENT THE REPLY FEATURE
 * Convenience root class for all bus message consumers
 *
 * @author Enrico Nicoletti
 */
public abstract class AbstractConsumer implements BusConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractConsumer.class.getName());
	private final BusMessagesListener listener;
	private final BusService busService;
	private boolean automaticReply;

	/**
	 * Constructor of consumer.
	 * 
	 * @param busService	busService use by the consumer
	 */
	public AbstractConsumer(BusService busService) {
		this.busService = busService;
		listener = new BusMessagesListener(this, busService);
		listener.consumeCommandFrom(getMessagingChannel());
		setAutomaticReply(true);
	}

	/**
	 * Makes actions implied by a command.
	 * 
	 * @param c	command to process
	 * @throws IOException
	 * @throws UnableToExecuteException
	 */
	protected abstract void onCommand(Command c) throws IOException, UnableToExecuteException;

	/**
	 * Makes actions implied by the event triggered.
	 * 
	 * @param event	event to process
	 */
	protected abstract void onEvent(EventTemplate event);

	/**
	 * Gets the messaging channel.
	 * 
	 * @return String representing the messaging channel
	 */
	protected abstract String getMessagingChannel();

	/**
	 * Handle a input message.
	 * 
	 * @param message	message to process.
	 * @see com.freedomotic.bus.BusConsumer#onMessage(javax.jms.ObjectMessage)
	 */
	@Override
	public void onMessage(ObjectMessage message) {
		Object jmsObject;
		try {
			jmsObject = message.getObject();

			if (jmsObject instanceof Command) {
				final Command command = (Command) jmsObject;
				LOG.info("\"{}\" receives command \"{}\" with parameters '{''{'{}'}''}'",
						this.getClass().getCanonicalName(), command.getName(), command.getProperties());
				ActuatorPerforms task = new ActuatorPerforms(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
				task.start();
			} else {
				// TODO Maybe make an else if instead
				if (jmsObject instanceof EventTemplate) {
					final EventTemplate event = (EventTemplate) jmsObject;
					onEvent(event);
				} else {
					throw new FreedomoticRuntimeException( "Unrecognized type in JMS message, is neither a Command or an Event");
				}
			}
		} catch (JMSException ex) {
			LOG.error("Error while receiving a JMS message", ex);
		}
	}

	/**
	 * Informs whether the reply is automatic or not.
	 * 
	 * @return true if automatic and false instead
	 */
	public boolean isAutomaticReply() {
		return automaticReply;
	}

	/**
	 * Set automaticReply flag.
	 * 
	 * @param automaticReply
	 *            true if automatic, false instead
	 */
	public void setAutomaticReply(boolean automaticReply) {
		this.automaticReply = automaticReply;
	}

	/**
	 * Returns the used busService.
	 * 
	 * @return the used busService
	 */
	public BusService getBusService() {
		return busService;
	}

	/**
	 * TODO create a real brand new class instead of an inner class ?
	 */
	private class ActuatorPerforms extends Thread {

		private final Command command;
		private final Destination reply;
		private final String correlationID;

		/**
		 * Constructor of ActuatorPerforms.
		 * 
		 * @param c
		 *            command to process
		 * @param reply
		 *            destination
		 * @param correlationID
		 *            identifier used for reply
		 */
		ActuatorPerforms(Command c, Destination reply, String correlationID) {
			this.command = c;
			this.reply = reply;
			this.correlationID = correlationID;
			this.setName("command-executor");
		}

		@Override
		public void run() {
			try {
				// a command is supposed executed if the plugin doesn't say the contrary
				command.setExecuted(true);
				onCommand(command);
			} catch (IOException ex) {
				LOG.error(ex.getLocalizedMessage());
				command.setExecuted(false);
			} catch (UnableToExecuteException ex) {
				command.setExecuted(false);
				LOG.info("\"{}\" failed to execute command \"{}\": {}", getName(), command.getName(), ex.getMessage());
			}

			// automatic-reply-to-command is used when the plugin executes the command in a
			// separate thread. In this cases the onCommand() returns immediately (as execution is forked in a thread)
			// and sometimes this is not the intended behavior. Take a look at the Delayer plugin configuration
			// it has to call reply(...) explicitely
			if ((isAutomaticReply()) && (command.getReplyTimeout() > 0)) {
				// sends back the command marked as executed/ or not
				getBusService().reply(command, reply, correlationID);
			}
		}
	}

}
