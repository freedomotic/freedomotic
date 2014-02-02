/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.mazzoni.twilight;

import com.freedomotic.events.GenericEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class TwilightUtils {

    private DateTime sunriseTime;
    private DateTime sunsetTime;
    private Duration toSunset;
    private Duration toSunrise;
    private int POLLING_WAIT;

    public TwilightUtils(int pw) {
        this.POLLING_WAIT = pw;
    }

    public GenericEvent prepareEvent(DateTime ref) {
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

        toSunset = sunsetTime.isAfter(ref) ? new Duration(ref, sunsetTime) : new Duration(sunsetTime, ref);
        toSunrise = sunriseTime.isAfter(ref) ? new Duration(ref, sunriseTime) : new Duration(sunriseTime, ref);

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

    public void setSunriseTime(DateTime ref) {
        sunriseTime = ref;
    }

    public void setSunsetTime(DateTime ref) {
        sunsetTime = ref;
    }

    public DateTime getSunriseTime() {
        return sunriseTime;
    }

    public DateTime getSunsetTime() {
        return sunsetTime;
    }
}
