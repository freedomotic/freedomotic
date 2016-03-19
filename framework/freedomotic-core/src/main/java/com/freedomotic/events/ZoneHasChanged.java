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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.environment.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel <b>app.event.sensor.environment.zone.change</b> informs that a zone
 * (eg: an environment room) has changed its state (zone description or zone
 * topology)
 *
 * @author Enrico Nicoletti
 */
public class ZoneHasChanged
        extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(ZoneHasChanged.class.getName());
    private static final long serialVersionUID = -2676123835322299252L;

    /**
     *
     * @param source
     * @param zone
     */
    public ZoneHasChanged(Object source, Zone zone) {
        this.setSender(source);
        payload.addStatement("zone.name", zone.getName());
        payload.addStatement("zone.description", zone.getDescription());
        payload.addStatement("zone.uuid", zone.getUuid());
        //TODO: just a reminder for this property
        //    Room room = null; 
        //    if (room != null) {
        //        payload.addStatement("zone.type", "room");
        //    } else {

        payload.addStatement("zone.type", "zone");

        //    }
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
        return "app.event.sensor.environment.zone.change";
    }
}
