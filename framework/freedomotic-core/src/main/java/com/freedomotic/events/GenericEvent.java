/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import java.util.logging.Logger;

/**
 * All purpose event. Use only if no more specific events are present in this
 * list. You can generate your own events extending the {@link EventTemplate}
 * class.
 *
 * @author enrico
 */
public class GenericEvent extends EventTemplate {

    private static final long serialVersionUID = 6029054631809171990L;

    private String destination = "app.event.sensor";

    /**
     *
     * @param source
     */
    public GenericEvent(Object source) {
        this.setSender(source);
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return destination;
    }

    /**
     *
     * @param destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }
    private static final Logger LOG = Logger.getLogger(GenericEvent.class.getName());
}
