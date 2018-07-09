/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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
 * Channel <b>app.event.sensor.reaction.change</b> informs about reaction related
 * events.
 *
 * @author Mauro Cicolella
 */
public class ReactionHasChanged
        extends EventTemplate {

    private static final String DEFAULT_DESTINATION = "app.event.sensor.reaction.change";

    /**
     *
     */
    public enum ReactionActions {

        /**
         * New reaction added
         */
        ADD,
        /**
         * Reaction removed
         */
        REMOVE,
        /**
         * Reaction edited
         */
        EDIT
    };

    /**
     *
     * @param source
     * @param reactionName
     * @param action
     */
    public ReactionHasChanged(Object source, String reactionName, ReactionActions action) {
        payload.addStatement("reaction.name", reactionName);
        payload.addStatement("reaction.action", action.toString());
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
