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

import it.freedomotic.app.Freedomotic;
import it.freedomotic.app.Profiler;
import java.util.logging.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * {@link MessageListener} implementation (former AbstractBusConnector class)
 * <p>
 * Receives an {@link ObjectMessage} (it can be an event or a command) and sends it to his {@link BusConsumer}
 * <p>
 * This is the bus hook for any {@link BusConsumer} that should register itself in this listener.
 * 
 * @author Freedomotic Team
 * 
 * @see BusConsumer
 */
public class BusMessagesListener implements MessageListener {

	private static final Logger LOG = Logger
			.getLogger(BusMessagesListener.class.getName());

	private BusService busService;

	private BusConsumer busConsumer;

	private MessageConsumer messageConsumer;

	/**
	 * Constructor
	 * 
	 * @param busConsumer
	 */
	public BusMessagesListener(BusConsumer busConsumer) {

		this.busConsumer = busConsumer;
		this.busService = Freedomotic.INJECTOR.getInstance(BusService.class);
	}

	/**
	 * Passes a message to the listener
	 * 
	 * @param message
	 */
	@Override
	public final void onMessage(Message message) {

		Profiler.incrementReceivedEvents();

		if (message instanceof ObjectMessage) {

			final ObjectMessage objectMessage = (ObjectMessage) message;

			busConsumer.onMessage(objectMessage);

		} else {

			LOG.severe("Message received by " + this.getClass().getSimpleName()
					+ " is not an object message, is a "
					+ message.getClass().getCanonicalName());

			if (message instanceof TextMessage) {

				TextMessage text = (TextMessage) message;

				try {

					LOG.severe(text.getText());

				} catch (JMSException ex) {

					// Do nothing.
				}
			}
		}
	}

	/**
	 * Registers on a command queue
	 * 
	 * @param queueName Queue name
	 */
	public void consumeCommandFrom(String queueName) {

		try {

			BusDestination busDestination = busService
					.registerCommandQueue(queueName);

			registerOnQueue(busDestination);

		} catch (JMSException e) {

			LOG.severe(Freedomotic.getStackTraceInfo(e));
		}
	}

	/**
	 * Registers on a event queue
	 * 
	 * @param queueName Queue name
	 */
	public void consumeEventFrom(String queueName) {

		try {

			BusDestination busDestination = busService
					.registerEventQueue(queueName);

			registerOnQueue(busDestination);

		} catch (JMSException e) {

			LOG.severe(Freedomotic.getStackTraceInfo(e));
		}
	}

	private void registerOnQueue(BusDestination destination)
			throws JMSException {

		final Session receiveSession = busService.getReceiveSession();
		messageConsumer = receiveSession.createConsumer(destination
				.getDestination());
		messageConsumer.setMessageListener(this);

		LOG.info(busConsumer.getClass().getSimpleName() + " listen on "
				+ destination.getDestinationName());

	}

	/**
	 * Unsubscribes from topics queues
	 * <br>
	 * (invocations should be life cycle managed)
	 */
	public void unsubscribe() {

		try {

			messageConsumer.close();

			// FIXME LCG Some unsuscribe invocations are pending 
			
			// TODO Why is suppossed to be always suscribed within the
			// receiveSession?

			// BusService busService = BusService.getInstance();
			// final Session receiveSession = busService.getReceiveSession();
			// receiveSession.unsubscribe(
			// ..retrieve channel from an internal list populated on
			// registration..);

		} catch (JMSException e) {

			LOG.severe(e.getMessage());

			// "Unable to unsubscribe from event channel "
			// + getChannelName());

		} catch (Exception e) {

			LOG.warning(e.getMessage());
		}
	}

	/** FIXME LCG is really unused? */
//	private ObjectMessage createObjectMessage() throws JMSException {
//
//		final Session sendSession = busService.getSendSession();
//		ObjectMessage msg = sendSession.createObjectMessage();
//
//		return msg;
//	}
}