/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class ScheduledEvent
        extends EventTemplate {

    private static final long serialVersionUID = 7508683624189475354L;

    /**
     *
     * @param source
     */
    public ScheduledEvent(Object source) {
        payload.addStatement("sender", source.getClass().getCanonicalName());
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return eventName;
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
        return "app.event.sensor.calendar.event.schedule";
    }
    private static final Logger LOG = Logger.getLogger(ScheduledEvent.class.getName());
}
