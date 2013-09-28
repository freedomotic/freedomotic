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

import it.freedomotic.bus.impl.DestinationType;

import javax.jms.Destination;

/**
 * Bus destination (queue) definition
 * 
 * @see DestinationType
 * 
 * @author Freedomotic Team
 *
 */
public class BusDestination {

	private Destination destination;
	
	private String destinationName;
	
	// TODO Think about later if is necessary to have it
	private DestinationType destinationType;

	/**
	 * Constructor
	 * 
	 * @param destination the destination provided by the bus
	 * @param destinationName the destination name
	 * @param destinationType the destination type
	 */
	public BusDestination(Destination destination, String destinationName,
			DestinationType destinationType) {
		
		super();
		
		this.destination = destination;
		this.destinationName = destinationName;
		this.destinationType = destinationType;
	}

	/**
	 * Getter method for destination
	 * 
	 * @return the destination
	 */
	public Destination getDestination() {
		return destination;
	}

	/**
	 * Getter method for destination name
	 * 
	 * @return the destination name
	 */
	protected String getDestinationName() {
		
		return destinationName;
	}

	/**
	 * Getter method for destination type
	 * 
	 * @return the destination type
	 */
	protected DestinationType getDestinationType() {
		
		return destinationType;
	}
}
