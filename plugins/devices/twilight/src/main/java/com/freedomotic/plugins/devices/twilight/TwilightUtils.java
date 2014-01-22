/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.twilight;

import com.freedomotic.plugins.devices.twilight.WeatherInfo;
import com.freedomotic.events.GenericEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class TwilightUtils {

    private int POLLING_WAIT;
    private WeatherInfo provider;

    public TwilightUtils(int pw, WeatherInfo provider) {
        this.POLLING_WAIT = pw;
        this.provider = provider;
    }
    
    public GenericEvent prepareEvent(DateTime ref) {
        // DateTime ref = DateTime.now();
        DateTime sunsetTime = provider.getNextSunset();
        DateTime sunriseTime = provider.getNextSunrise();
        
        while (sunsetTime.isBefore(ref) && sunriseTime.isBefore(ref)) {
            if (sunsetTime.isBefore(sunriseTime)) {

                // dopo il tramonto: aggiorna data prissima alba
                sunsetTime =
                        sunsetTime.plusDays(1);
            } else {

                // dopo il tramonto: aggiorna data prissima alba
                sunriseTime =
                        sunriseTime.plusDays(1);
            }
        }

        Duration toSunset = sunsetTime.isAfter(ref) ? new Duration(ref, sunsetTime) : new Duration(sunsetTime, ref);
        Duration toSunrise = sunriseTime.isAfter(ref) ? new Duration(ref, sunriseTime) : new Duration(sunriseTime, ref);

        // genera evento: 
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
            // prima dell'alba
            ev.addProperty("beforeSunrise", Long.toString(toSunrise.getStandardMinutes()));
        } else if (ref.isAfter(sunriseTime)) {
            // dopo l'alba, 
            ev.addProperty("afterSunrise", Long.toString(toSunrise.getStandardMinutes()));
        }
        
        if (ref.isBefore(sunsetTime)) {
            // prima del tramonto
            ev.addProperty("beforeSunset", Long.toString(toSunset.getStandardMinutes()));
        } else if (ref.isAfter(sunsetTime)) {
            // dopo il tramonto
            ev.addProperty("afterSunset", Long.toString(toSunset.getStandardMinutes()));
        }
        return ev;
    }


}
