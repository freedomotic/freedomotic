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
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import java.util.Iterator;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel <b>app.event.sensor.object.behavior.change</b> informs that an object
 * has changed its behavior (eg: a light change behavior from off to on).
 *
 * Available tokens for triggers:
 *
 * @see com.freedomotic.api.EventTemplate for properties like date, time, sender
 * which are common to all events
 * @see com.freedomotic.object.EnvObjectLogic#getExposedProperties() for object
 * data
 *
 * @author Enrico Nicoletti
 */
public class ObjectHasChangedBehavior extends EventTemplate {

    private static final long serialVersionUID = 6892968576173017195L;
    private static final Logger LOG = LoggerFactory.getLogger(ObjectHasChangedBehavior.class.getName());

    //private EnvObject obj;
    /**
     *
     * @param source
     * @param obj
     */
    public ObjectHasChangedBehavior(Object source, EnvObjectLogic obj) {
        super(source);

        //add default object properties
        Iterator<Entry<String, String>> it = obj.getExposedProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            payload.addStatement(entry.getKey().toString(), entry.getValue().toString());
        }

        //add the list of changed behaviors
        payload.addStatement("object.currentRepresentation",
                obj.getPojo().getCurrentRepresentationIndex());

        for (BehaviorLogic behavior : obj.getBehaviors()) {
            if (behavior.isChanged()) {
                payload.addStatement("object.behavior." + behavior.getName(),
                        behavior.getValueAsString());
                behavior.setChanged(false);
            }
        }

        // Include object location
        try {
            FreedomPoint location = obj.getPojo().getCurrentRepresentation().getOffset();
            if (location != null) {
                payload.addStatement("object.location.x", location.getX());
                payload.addStatement("object.location.y", location.getY());
            }
        } catch (Exception e) {
            //best effort, location can be null
        }
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
        return "app.event.sensor.object.behavior.change";
    }
}
