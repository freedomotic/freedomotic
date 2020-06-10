/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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

/**
 * Channel <b>app.event.sensor.trigger.change</b> informs about trigger related
 * events.
 *
 * @author Mauro Cicolella
 */
public class TriggerHasChanged
        extends EventTemplate {

    private static final String DEFAULT_DESTINATION = "app.event.sensor.trigger.change";

    /**
     *
     */
    public enum TriggerActions {

        /**
         * New trigger added
         */
        ADD,
        /**
         * Trigger removed
         */
        REMOVE,
        /**
         * Trigger edited
         */
        EDIT
    };

    /**
     *
     * @param source
     * @param triggerName
     * @param action
     */
    public TriggerHasChanged(Object source, String triggerName, TriggerActions action) {
        payload.addStatement("trigger.name", triggerName);
        payload.addStatement("trigger.action", action.toString());
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
        return DEFAULT_DESTINATION;
    }
}
