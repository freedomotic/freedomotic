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

import org.joda.time.DateTime;


/**
 *
 * @author Matteo Mazzoni
 */
public interface WeatherInfo {

    /**
     *
     * @return
     */
    DateTime getNextSunset();

    /**
     *
     * @return
     */
    DateTime getNextSunrise();

    /**
     *
     * @return
     */
    String getNextHumidity();

    /**
     *
     * @return
     */
    String getNextPressure();

     /**
     *
     * @return
     */
    String getNextTemperature();

    /**
     *
     * @return @throws Exception
     */
    boolean updateData() throws Exception;

    /**
     *
     * @param sunset
     */
    void setNextSunset(DateTime sunset);

    /**
     *
     * @param sunrise
     */
    void setNextSunrise(DateTime sunrise);

    /**
     *
     * @param sunrise
     */
    void setNextHumidity(String humidity);

    /**
     *
     * @param sunrise
     */
    void setNextPressure(String pressure);

    /**
     *
     * @param sunrise
     */
    void setNextTemperature(String temperature);

}
