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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel <b>app.event.sensor.plugin.change</b> informs about plugin related
 * events like plugin started, stopped, description changes, and so on.
 *
 * @author Enrico Nicoletti
 */
public class PluginHasChanged
        extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(PluginHasChanged.class.getName());
    private static final long serialVersionUID = 5203339184820441643L;

    /**
     *
     */
    public enum PluginActions {

        /**
         *
         */
        SHOW,
        /**
         *
         */
        HIDE,
        /**
         *
         */
        DESCRIPTION,
        /**
         *
         */
        START,
        /**
         *
         */
        STOP,
        /**
         *
         */
        DISPOSE,
        /**
         *
         */
        MAXIMIZE,
        /**
         *
         */
        MINIMIZE,
        /**
         *
         */
        ENQUEUE,
        /**
         *
         */
        DEQUEUE
    };

    /**
     *
     * @param source
     * @param pluginName
     * @param action
     */
    public PluginHasChanged(Object source, String pluginName, PluginActions action) {
        payload.addStatement("plugin", pluginName);
        payload.addStatement("action",
                action.toString());
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
        return "app.event.sensor.plugin.change";
    }
}
