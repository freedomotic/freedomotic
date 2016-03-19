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
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.things.GenericPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public final class PersonEntersZone extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(PersonEntersZone.class.getName());
    private final String uuid;
    private final String zoneName;

    public PersonEntersZone(ZoneLogic aThis, GenericPerson p, Zone zone) {
        this.uuid = p.getPojo().getUUID();
        this.zoneName = zone.getName();
        generateEventPayload();
    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("person.id", uuid);
        payload.addStatement("zone.name", zoneName);
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.zone.enter";
    }

    public String getPersonId() {
        return uuid;
    }

    public String getZoneName() {
        return zoneName;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Object " + uuid + " enters zone " + zoneName);
    }
}
