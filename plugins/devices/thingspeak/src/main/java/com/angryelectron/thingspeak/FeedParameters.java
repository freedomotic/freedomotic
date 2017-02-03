/**
 * ThingSpeak Java Client 
 * Copyright 2014, Andrew Bythell <abythell@ieee.org>
 * http://angryelectron.com
 *
 * The ThingSpeak Java Client is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * The ThingSpeak Java Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * theThingSpeak Java Client. If not, see <http://www.gnu.org/licenses/>.
 */

package com.angryelectron.thingspeak;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * <p>
 * Optional parameters which control the format of a {@link Feed}.
 * All ThingSpeak optional parameters are supported except 'key' (because it is set via
 * {@link Channel}) and 'format' (which must be JSON in order to parse the feed).
 * </p>
 * <p>
 * To retrieve a feed with optional parameters, setup a {@link FeedParameters}
 * object with the required parameters, then pass it to one of the 
 * {@link Channel} methods to retrieve the desired feed.  For example, to 
 * include the latitude, longitude, elevation, and status fields in a feed for 
 * channel 1234:</p>
 * <pre>
 * {@code
 * Channel channel = new Channel(1234);
 * FeedParameters options = new FeedParameters();
 * options.location(true);
 * options.status(true);
 * options.results(100);
 * Feed feed = channel.getChannelFeed(options);
 * }
 * </pre>
 */
public class FeedParameters {
        
    /**
     * Pre-defined time periods.  ThingSpeak only accepts certain values.
     * For use with:
     * <ul>
     * <li>{@link #timescale(com.angryelectron.thingspeak.FeedParameters.Period)}</li>
     * <li>{@link #sum(com.angryelectron.thingspeak.FeedParameters.Period)}</li>
     * <li>{@link #average(com.angryelectron.thingspeak.FeedParameters.Period)}</li>
     * <li>{@link #median(com.angryelectron.thingspeak.FeedParameters.Period)}</li>
     * </ul>
     */
    public enum Period { 
        /**
         * 10 minutes.
         */
        T10m(10), 
        
        /**
         * 15 minutes.
         */
        T15m(15), 
        
        /**
         * 20 minutes.
         */
        T20m(20),
        
        /** 
         * 30 minutes.
         */
        T30m(30),
        
        /**
         * 1 hour / 60 minutes.
         */
        T1h(60), 
        
        /**
         * 4 hours / 240 minutes.
         */
        T4h(240), 
        
        /**
         * 12 hours / 720 minutes.
         */
        T12h(720),
        
        /**
         * 24 hours / 1440 minutes.
         */
        T24h(1440);
        
        private final Integer minutes;       
        
        private Period(Integer minutes) {
            this.minutes = minutes;
        }
        
        Integer minutes() {
            return this.minutes;
        }
        
    }
    
    /**
     * A map to store the parameter names and values.
     */
    HashMap<String, Object> fields = new HashMap<>();
    
    /**
     * The date format required by ThingSpeak.
     */
    private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    
    /**
     * Select the number of results to be returned.  Feeds that return more than 100
     * results are cached for 5 minutes, so set results &lt; 100 for real time 
     * applications.  By default, all results up to a maximum of 8000 will be returned.
     * @param results 8000 max, or less than 100 to disable 5 minute data cache.
     */
    public void results(Integer results) {
        if (results > 8000) {
            throw new IllegalArgumentException("Feed cannot return more than 8000 results.");
        }
        fields.put("results", results);
    }
    /**
     * Limit results to the past number of days.
     * @param days Number of days prior to now to include in feed.
     */       
    public void days(Integer days) {
        fields.put("days", days);
    }
    
    /**
     * Limit results to entries recorded on or after a start date.
     * @param date Start date.
     */
    public void start(Date date) {
        fields.put("start", formatter.format(date));
    }
    
    /**
     * Limit results to entries recorded on or before an end date.
     * @param date End date.
     */
    public void end(Date date) {
        fields.put("end", formatter.format(date));
    }
    
    /**
     * Timezone offset.  Default is UTC.  Applies to all dates returned in the
     * feed.
     * @param hours Offset (+/-) in hours.
     */
    public void offset(Integer hours) {
        fields.put("offset", hours);
    }
    
    /**
     * Include the status field for each entry in the feed.  By default,
     * the status field is not included.
     * @param include Feed includes the status field when True.
     */
    public void status(Boolean include) {
        fields.put("status", include);
    }
    
    /**
     * Include location information for each entry in the feed.  By default, 
     * latitude, longitude, and elevation are not included.
     * @param include Feed includes location fields when True.
     */
    public void location(Boolean include) {
        fields.put("location", include);
    }
    
    /**
     * Include only entries with fields greater or equal to a minimum value in the
     * feed.
     * @param value Minimum value.
     */
    public void min(Double value) {
        fields.put("min", value);
    }
    
    /**
     * Include only entries with fields less than or equal to a maximum value.
     * @param value Maximum value.
     */
    public void max(Double value) {
        fields.put("max", value);
    }
    
    /**
     * Round fields to a certain number of decimal places.
     * @param places Round to this many decimal places.
     */
    public void round(Integer places) {
        fields.put("round", places);
    }
    
    /**
     * Include only the first value in the given period.
     * @param t Time period.
     */
    public void timescale(Period t) {
        fields.put("timescale", t.minutes());
    }
    
    /**
     * For each field, sum values in the given period.
     * @param t Time period.
     */
    public void sum(Period t) {
        fields.put("sum", t.minutes());
    }
    
    /**
     * For each field, average the values over the given period.
     * @param t Time period.
     */
    public void average(Period t) {
        fields.put("average", t.minutes());        
    }
    
    /**
     * For each field, find the median value in the given period.
     * @param t Time period.
     */
    public void median(Period t) {
        fields.put("median", t.minutes());
    }
    
}
