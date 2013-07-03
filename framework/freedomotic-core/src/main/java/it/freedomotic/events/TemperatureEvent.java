/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.model.environment.Zone;

/**
 *
 * @author Enrico
 */
public class TemperatureEvent extends EventTemplate {

    private static final long serialVersionUID = 2965942901211451802L;
	
	private int temperature;
    private String zone;

    public TemperatureEvent(Object source, int temperature, Zone z) {
        this.temperature = temperature;
        zone = z.getName();
        generateEventPayload();
    }


    @Override
    protected void generateEventPayload() {
        payload.addStatement("zone", zone);
        payload.addStatement("temperature", temperature);
    }

    @Override
    public String toString() {
        return ("Temperature in " + zone + " is " + temperature + "°C");
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.temperature";
    }
}
