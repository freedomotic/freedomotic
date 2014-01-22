/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.twilight;

import org.joda.time.DateTime;

/**
 *
 * @author matteo
 */
public interface WeatherInfo {
    
    DateTime getNextSunset();
    DateTime getNextSunrise();
    boolean updateData() throws Exception;
    void setNextSunset(DateTime sunset);
    void setNextSunrise(DateTime sunrise);

}
