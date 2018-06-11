/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.twilight;

import com.freedomotic.events.GenericEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author Matteo Mazzoni
 */
public class TwilightUtils {

    private final int POLLING_WAIT;
    private final WeatherInfo provider;

    /**
     *
     * @param pw
     * @param provider
     */
    public TwilightUtils(int pw, WeatherInfo provider) {
        this.POLLING_WAIT = pw;
        this.provider = provider;
    }

    /**
     *
     * @param ref
     * @return
     */
    public GenericEvent prepareEvent(DateTime ref) {
        DateTime sunsetTime = provider.getNextSunset();
        DateTime sunriseTime = provider.getNextSunrise();
        String humidity = provider.getNextHumidity();
        String pressure = provider.getNextPressure();
        String temperature = provider.getNextTemperature();

        while (sunsetTime.isBefore(ref) && sunriseTime.isBefore(ref)) {
            if (sunsetTime.isBefore(sunriseTime)) {
                // after the sunrise: update next sunset
                sunsetTime = sunsetTime.plusDays(1);
            } else {
                // after the sunset: update next sunrise
                sunriseTime = sunriseTime.plusDays(1);
            }
        }

        Duration toSunset = sunsetTime.isAfter(ref) ? new Duration(ref, sunsetTime) : new Duration(sunsetTime, ref);
        Duration toSunrise = sunriseTime.isAfter(ref) ? new Duration(ref, sunriseTime) : new Duration(sunriseTime, ref);

        // create the event 
        GenericEvent ev = new GenericEvent(getClass());
        ev.setDestination("app.event.sensor.calendar.event.twilight");

        if (toSunset.getMillis() < POLLING_WAIT / 2) {
            // it's sunset
            ev.addProperty("isSunset", "true");
        } else if (toSunrise.getMillis() < POLLING_WAIT / 2) {
            // it's sunrise
            ev.addProperty("isSunrise", "true");
        }
        if (ref.isBefore(sunriseTime)) {
            // before sunrise
            ev.addProperty("beforeSunrise", Long.toString(toSunrise.getStandardMinutes()));
        } else if (ref.isAfter(sunriseTime)) {
            // after sunrise 
            ev.addProperty("afterSunrise", Long.toString(toSunrise.getStandardMinutes()));
        }
        if (ref.isBefore(sunsetTime)) {
            // before sunset
            ev.addProperty("beforeSunset", Long.toString(toSunset.getStandardMinutes()));
        } else if (ref.isAfter(sunsetTime)) {
            // after sunset
            ev.addProperty("afterSunset", Long.toString(toSunset.getStandardMinutes()));
        }
        ev.addProperty("humidity", humidity);
        ev.addProperty("pressure", pressure);
        ev.addProperty("temperature", temperature);
        return ev;
    }
}
