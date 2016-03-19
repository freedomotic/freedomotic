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
import com.freedomotic.things.EnvObjectLogic;
import java.util.Iterator;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel <b>app.event.sensor.object.behavior.clicked</b> informs that an
 * object is clicked on a frontend. Different types of click are supported
 * (single_click, double_click, right_click)
 *
 * @author Enrico Nicoletti
 */
public class ObjectReceiveClick
        extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(ObjectReceiveClick.class.getName());
    private static final long serialVersionUID = 8985824879207319982L;

    /**
     *
     */
    public static final String SINGLE_CLICK = "SINGLE_CLICK";

    /**
     *
     */
    public static final String DOUBLE_CLICK = "DOUBLE_CLICK";

    /**
     *
     */
    public static final String RIGHT_CLICK = "RIGHT_CLICK";

    /**
     *
     * @param source
     * @param obj
     * @param click
     */
    public ObjectReceiveClick(Object source, EnvObjectLogic obj, String click) {
        this.setSender(source);
        payload.addStatement("click", click.toString());
        Iterator<Entry<String, String>> it = obj.getExposedProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            payload.addStatement(entry.getKey().toString(), entry.getValue().toString());
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
        return "app.event.sensor.object.behavior.clicked";
    }
}
