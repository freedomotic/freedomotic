/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.events;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.model.environment.Zone;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class LuminosityEvent
        extends EventTemplate {

    private static final long serialVersionUID = 1605869382477368794L;
	
	int luminosity;
    String zone;

    /**
     *
     * @param source
     * @param temperature
     * @param z
     */
    public LuminosityEvent(Object source, int temperature, Zone z) {
        this.luminosity = temperature;
        zone = z.getName();
        generateEventPayload();
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone", zone);
        payload.addStatement("luminosity", luminosity);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Luminosity in " + zone + " is " + luminosity + "%");
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.luminosity";
    }
    private static final Logger LOG = Logger.getLogger(LuminosityEvent.class.getName());
}
