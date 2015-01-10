/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.bus;

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

    /**
     * Constructor
     *
     * @param destination the destination provided by the bus
     * @param destinationName the destination name
     */
    public BusDestination(Destination destination, String destinationName) {

        super();

        this.destination = destination;
        this.destinationName = destinationName;
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
}
