package com.freedomotic.plugins.devices.twilight;

import org.joda.time.DateTime;

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
