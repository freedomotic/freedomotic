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
 *
 */

package com.freedomotic.plugins.devices.twilight;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.freedomotic.events.GenericEvent;
import com.freedomotic.plugins.devices.twilight.providers.EarthToolsWI;
import com.freedomotic.plugins.devices.twilight.providers.OpenWeatherMapWI;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Matteo Mazzoni 
 */


public class TwilightTest {

    private static TwilightUtils twu;
    private static WeatherInfo provider;
    private static WeatherInfo provider_alt;

    @BeforeClass
    public static void setUpClass() throws Exception {
        provider = new OpenWeatherMapWI("43.567", "11.021");
        provider_alt = new EarthToolsWI("43.567", "11.021");
        twu = new TwilightUtils(10000, provider);
        
       }

    @Test
    public void updateTest() throws Exception{
        provider.updateData();
        System.out.println(provider.getClass().getCanonicalName() + " Sunrise: " + provider.getNextSunrise().toString() +" - Sunset: "+ provider.getNextSunset() );
        provider_alt.updateData();
        System.out.println(provider_alt.getClass().getCanonicalName() + " Sunrise: " + provider_alt.getNextSunrise().toString() +" - Sunset: "+ provider_alt.getNextSunset() );
        
    }
    
    @Test
    public void NoonTest() {
        provider.setNextSunrise(dateTime(2013, 11, 20, 5, 0));
        provider.setNextSunset(dateTime(2013, 11, 20, 17, 0));
        
        ZonedDateTime noon = dateTime(2013, 11, 21, 12, 0);
        GenericEvent twAtNoon = twu.prepareEvent(noon);
        Assert.assertEquals("300", twAtNoon.getProperty("beforeSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("afterSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("isSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("isSunrise"));
        Assert.assertEquals("420", twAtNoon.getProperty("afterSunrise"));
        Assert.assertEquals("", twAtNoon.getProperty("beforeSunrise"));
        
        noon = dateTime(2013, 11, 21, 12, 1);
        twAtNoon = twu.prepareEvent(noon);
        Assert.assertEquals("299", twAtNoon.getProperty("beforeSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("afterSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("isSunset"));
        Assert.assertEquals("", twAtNoon.getProperty("isSunrise"));
        Assert.assertEquals("421", twAtNoon.getProperty("afterSunrise"));
        Assert.assertEquals("", twAtNoon.getProperty("beforeSunrise"));
    }

    @Test
    public void MidnightTest() {
        provider.setNextSunrise(dateTime(2013, 11, 20, 5, 0));
        provider.setNextSunset(dateTime(2013, 11, 20, 17, 0));
        
        ZonedDateTime midnight = dateTime(2013, 11, 22, 0, 0);
        GenericEvent twAtMidnight = twu.prepareEvent(midnight);
        Assert.assertEquals("", twAtMidnight.getProperty("beforeSunset"));
        Assert.assertEquals("420", twAtMidnight.getProperty("afterSunset"));
        Assert.assertEquals("", twAtMidnight.getProperty("isSunset"));
        Assert.assertEquals("", twAtMidnight.getProperty("isSunrise"));
        Assert.assertEquals("", twAtMidnight.getProperty("afterSunrise"));
        Assert.assertEquals("300", twAtMidnight.getProperty("beforeSunrise"));

    }

    @Test
    public void sunriseTest() {
        provider.setNextSunrise(dateTime(2013, 11, 20, 5, 0));
        provider.setNextSunset(dateTime(2013, 11, 20, 17, 0));
        
        ZonedDateTime sunrise = dateTime(2013, 11, 20, 5, 0);
        GenericEvent twAtSunrise = twu.prepareEvent(sunrise);
        Assert.assertEquals("720", twAtSunrise.getProperty("beforeSunset"));
        Assert.assertEquals("", twAtSunrise.getProperty("afterSunset"));
        Assert.assertEquals("", twAtSunrise.getProperty("isSunset"));
        Assert.assertEquals("true", twAtSunrise.getProperty("isSunrise"));
        Assert.assertEquals("", twAtSunrise.getProperty("afterSunrise"));
        Assert.assertEquals("", twAtSunrise.getProperty("beforeSunrise"));
        
        sunrise = dateTime(2013, 11, 20, 5, 1);
        twAtSunrise = twu.prepareEvent(sunrise);
        Assert.assertEquals("719", twAtSunrise.getProperty("beforeSunset"));
        Assert.assertEquals("", twAtSunrise.getProperty("afterSunset"));
        Assert.assertEquals("", twAtSunrise.getProperty("isSunset"));
        Assert.assertEquals("", twAtSunrise.getProperty("isSunrise"));
        Assert.assertEquals("1", twAtSunrise.getProperty("afterSunrise"));
        Assert.assertEquals("", twAtSunrise.getProperty("beforeSunrise"));
    }

    @Test
    public void sunsetTest() {
		provider.setNextSunrise(dateTime(2013, 11, 20, 5, 0));
        provider.setNextSunset(dateTime(2013, 11, 20, 17, 0));
        
        ZonedDateTime sunset = dateTime(2013, 11, 23, 17, 0);
        GenericEvent twAtSunset = twu.prepareEvent(sunset);
        Assert.assertEquals("", twAtSunset.getProperty("beforeSunset"));
        Assert.assertEquals("", twAtSunset.getProperty("afterSunset"));
        Assert.assertEquals("true", twAtSunset.getProperty("isSunset"));
        Assert.assertEquals("", twAtSunset.getProperty("isSunrise"));
        Assert.assertEquals("720", twAtSunset.getProperty("afterSunrise"));
        Assert.assertEquals("", twAtSunset.getProperty("beforeSunrise"));
        
        ZonedDateTime postSunset = dateTime(2013, 11, 23, 17, 1);
        GenericEvent twPostSunset = twu.prepareEvent(postSunset);
        Assert.assertEquals("", twPostSunset.getProperty("beforeSunset"));
        Assert.assertEquals("1", twPostSunset.getProperty("afterSunset"));
        Assert.assertEquals("", twPostSunset.getProperty("isSunset"));
        Assert.assertEquals("", twPostSunset.getProperty("isSunrise"));
        Assert.assertEquals("", twPostSunset.getProperty("afterSunrise"));
        Assert.assertEquals("719", twPostSunset.getProperty("beforeSunrise"));
    }

    private static ZonedDateTime dateTime(int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.of(year, month, day, hour, minute, 0, 0, ZoneId.systemDefault());
    }
}
