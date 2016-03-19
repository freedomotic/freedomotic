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
package com.freedomotic.core;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.object.EnvObject;

/**
 * This class is reserver for internal use, Never use it outside the core!
 *
 * @author Enrico Nicoletti
 */
public class SynchThingRequest extends EventTemplate {

    private final EnvObject thing;

    public SynchThingRequest(SynchAction action, EnvObject thing) {
        this.thing = thing;
        this.addProperty(SynchAction.KEY_SYNCH_ACTION, action.name());
    }

    public EnvObject getThing() {
        return thing;
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        //do nothing
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return SynchManager.LISTEN_CHANNEL;
    }

}
