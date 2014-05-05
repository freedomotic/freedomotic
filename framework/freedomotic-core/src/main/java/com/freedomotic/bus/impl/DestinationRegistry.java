/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.bus.impl;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusDestination;
import com.freedomotic.bus.BusService;
import com.freedomotic.util.UidGenerator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;

/**
 * Destination registry. Caches destination registration.
 * 
 * Future uses are:
 *  <blockquote>
 *  - sanity check of bus operations
 *  <br>
 *  - improved log monitor facility based on contexts
 *  </blockquote>
 * 
 * @author Freedomotic Team
 *
 */
// TODO LCG Should it be life cycle managed?
public class DestinationRegistry {

	private final BusService busService;
	
	private interface CreateDestinationAction {

		public BusDestination create() throws JMSException;

		public String getQueueName();
	}

	private abstract class DestinationAction implements CreateDestinationAction {

		private String queueName;

		public DestinationAction(String queueName) {

			this.queueName = queueName;
		}

		@Override
		public abstract BusDestination create() throws JMSException;

		@Override
		public String getQueueName() {
			
			return queueName;
		}

	}

	private Map<String, BusDestination> consumer = new ConcurrentHashMap<String, BusDestination>();

    /**
     *
     */
    public DestinationRegistry() {
		
		super();
        
		busService = Freedomotic.INJECTOR.getInstance(BusService.class);
	}

	private synchronized BusDestination register(CreateDestinationAction command)
			throws JMSException {

		BusDestination destination;
		if (!consumer.containsKey(command.getQueueName())) {

			destination = command.create();
			consumer.put(command.getQueueName(), destination);

		} else {

			destination = consumer.get(command.getQueueName());
		}

		return destination;
	}

    /**
     *
     * @param queueName
     * @return
     * @throws JMSException
     */
    protected BusDestination registerCommandQueue(String queueName)
			throws JMSException {

		return registerQueue(queueName);
	}

    /**
     *
     * @param queueName
     * @return
     * @throws JMSException
     */
    protected BusDestination registerEventQueue(String queueName)
			throws JMSException {

		final String eventQueueName = "Consumer."
				+ UidGenerator.getNextStringUid() + ".VirtualTopic."
				+ queueName;

		return registerQueue(eventQueueName);
	}

	private BusDestination registerQueue(String queueName) throws JMSException {

		CreateDestinationAction command = new DestinationAction(queueName) {

			@Override
			public BusDestination create() throws JMSException {

				final Session receiveSession = busService.getReceiveSession();

				Destination destination = receiveSession
						.createQueue(getQueueName());

				BusDestination busDestination = new BusDestination(destination,
						getQueueName(), DestinationType.QUEUE);

				return busDestination;
			}
		};

		return register(command);
	}

    /**
     *
     * @param queueName
     * @return
     * @throws JMSException
     */
    public BusDestination registerTopic(String queueName) throws JMSException {

		CreateDestinationAction command = new DestinationAction(queueName) {

			@Override
			public BusDestination create() throws JMSException {

				final Session sendSession = busService.getSendSession();

				Destination destination = sendSession
						.createTopic("VirtualTopic." + getQueueName());

				BusDestination busDestination = new BusDestination(destination,
						getQueueName(), DestinationType.TOPIC);

				return busDestination;

			}
		};

		return register(command);
	}
}
