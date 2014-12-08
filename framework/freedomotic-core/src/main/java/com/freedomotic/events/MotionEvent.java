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
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPoint;
import java.util.logging.Logger;

/**
 *
 * @author Mauro Cicolella
 */
public class MotionEvent
        extends EventTemplate {

    private static final Logger LOG = Logger.getLogger(MotionEvent.class.getName());
    private static final long serialVersionUID = 4965942901211451802L;
    private String zoneName;
    private int motionArea;
    private int centerOfGravity;
    private int distance;
    private FreedomPoint location;

    /**
     * @param source
     * @param zone
     */
    public MotionEvent(Object source, Zone zone) {
        this.setSender(source);
        zoneName = zone.getName();
        generateEventPayload();
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone.name", zoneName);

    }

    public String getZoneName() {
        return zoneName;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int dist) {
        distance = dist;
        payload.addStatement("distance", dist);

    }

    public FreedomPoint getLocation() {
        return location;
    }

    public void setLocation(FreedomPoint loc) {
        location = loc;
        payload.addStatement("location-x", loc.getX());
        payload.addStatement("location-y", loc.getY());

    }

    public int getCenterOfGravity() {
        return centerOfGravity;
    }

    public void setCenterOfGravity(int cog) {
        centerOfGravity = cog;
        payload.addStatement("center-of-gravity", cog);

    }

    public int getMotionArea() {
        return motionArea;
    }

    public void setMotionArea(int area) {
        motionArea = area;
        payload.addStatement("motion-area", area);

    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Motion detected in zone " + zoneName);
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.motion";
    }
}
