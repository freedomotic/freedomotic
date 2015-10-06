/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
 * http://freedomotic.com
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
import com.freedomotic.model.geometry.FreedomPoint;
import java.util.logging.Logger;

/**
 * A person is detected in a position with coordinates x,y of the environment.
 * This event is thrown on the first time a person is detected, after a
 * detection, if the person moves, a {@link PersonMoving} event is thrown.
 *
 * @author Enrico
 */
public final class LocationEvent extends EventTemplate {

    private static final Logger LOG = Logger.getLogger(LocationEvent.class.getName());
    private final String uuid;
    private final int x;
    private final int y;
    private final int z;

    /**
     *
     * @param source
     * @param uuid
     * @param location
     */
    public LocationEvent(Object source, String uuid, FreedomPoint location) {
        this.uuid = uuid;
        x = location.getX();
        y = location.getY();
        z = 0;
        generateEventPayload();
    }

    
    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        payload.addStatement("id", uuid);
        payload.addStatement("coord.x", x);
        payload.addStatement("coord.y", y);
        //TODO: z coordinate is currently not supported
        payload.addStatement("coord.z", 0);
    }

    /**
     *
     * @return
     */
    public String getUuid() {
        return uuid;
    }

    /**
     *
     * @return
     */
    public int getX() {
        return x;
    }

    /**
     *
     * @return
     */
    public int getY() {
        return y;
    }
    
        /**
     *
     * @return
     */
    public int getZ() {
        return z;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Object " + uuid + " has been detected at location " + x + "," + y);
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.movement.detected";
    }
}
