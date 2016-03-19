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
import com.freedomotic.model.environment.Zone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class TemperatureEvent
        extends EventTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(TemperatureEvent.class.getName());
    private static final long serialVersionUID = 2965942901211451802L;

    private int temperature;
    private String zone;

    /**
     *
     * @param source
     * @param temperature
     * @param z
     */
    public TemperatureEvent(Object source, int temperature, Zone z) {
        this.temperature = temperature;
        zone = z.getName();
        generateEventPayload();
    }

    /**
     *
     */
    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone", zone);
        payload.addStatement("temperature", temperature);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return ("Temperature in " + zone + " is " + temperature + "°C");
    }

    /**
     *
     * @return
     */
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.temperature";
    }
}
