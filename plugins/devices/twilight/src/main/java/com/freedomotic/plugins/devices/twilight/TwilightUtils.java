package com.freedomotic.plugins.devices.twilight;

import com.freedomotic.events.GenericEvent;
import org.joda.time.DateTime;
import org.joda.time.Duration;

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
        DateTime aux = new DateTime(2018,6,19, 17,0);

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
        if(ref.getMillis() == aux.getMillis()){
            String humidity = provider.getNextHumidity();
            String pressure = provider.getNextPressure();
            String temperature = provider.getNextTemperature();
            if(humidity!=""){
                ev.addProperty("humidity", "true");
            }
            else{
                ev.addProperty("humidity", "false");   
            }
            if(pressure!=""){
                ev.addProperty("pressure", "true");    
            }
            else{
                ev.addProperty("pressure", "false");
            }
            if(temperature!=""){
                ev.addProperty("temperature", "true");    
            }
            else{
                ev.addProperty("temperature", "false");
            }
        }
        return ev;
    }
}
