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

import java.util.Date;
import java.util.HashMap;

/**
 * Create a new Entry to update a channel, or retrieve individual elements from
 * a {@link Feed}.
 * 
 */
public class Entry {
    
    /**
     * The names of these private members must match the JSON fields in a
     * channel feed returned by ThingSpeak. If they don't, GSON might not be
     * able to deserialize the JSON feed into Entry objects. Note that
     * 'longitude' and 'latitude' are returned by feeds, but 'lat' and 'long'
     * are used when updating.
     */
    private Date created_at;
    private Integer entry_id;
    private String field1;
    private String field2;
    private String field3;
    private String field4;
    private String field5;
    private String field6;
    private String field7;
    private String field8;
    private Double latitude;
    private Double longitude;
    private Double elevation;
    private String status;
    private String twitter;
    private String tweet;
    
    private final HashMap<String, Object> updateMap = new HashMap<>();

    /**
     * Get a map of all fields in a format compatible with the API's update
     * parameters.  Used internally by {@link Channel#update(com.angryelectron.thingspeak.Entry)}.
     * @return Field map.
     */
    HashMap<String, Object> getUpdateMap() {        
        return updateMap;
    }
            
    /**
     * Get data for a field.  Fields must be enabled via the web in the Channel's
     * settings.
     * @param field 1-8
     * @return Field data; null for status feeds,  undefined fields, and field 
     * feeds where field was not specified.
     */
    public Object getField(Integer field) {
        switch(field) {
            case 1:
                return field1;
            case 2:
                return field2;
            case 3:
                return field3;
            case 4:
                return field4;
            case 5:
                return field5;
            case 6:
                return field6;
            case 7:
                return field7;
            case 8:
                return field8;                
        }
        throw new IllegalArgumentException("Invalid field.");
    }

    /**
     * Set the value for a field.  Fields must be enabled via the web in the Channel's
     * settings.
     * @param field 1-8.
     * @param value Value for field.  
     */
    public void setField(Integer field, String value) {
        switch(field) {
            case 1:
                field1 = value;
                updateMap.put("field1", value);
                return;
            case 2:
                field2 = value;
                updateMap.put("field2", value);
                return;
            case 3:
                field3 = value;
                updateMap.put("field3", value);
                return;
            case 4:
                field4 = value;
                updateMap.put("field4", value);
                return;
            case 5:
                field5 = value;
                updateMap.put("field5", value);
                return;
            case 6:
                field6 = value;
                updateMap.put("field6", value);
                return;
            case 7:
                field7 = value;
                updateMap.put("field7", value);
                return;
            case 8:
                field8 = value;
                updateMap.put("field8", value);
                return;
        }
        throw new IllegalArgumentException("Invalid field.");
    }
    
    /**
     * Get latitude.
     * @return Latitude, in decimal degrees; 0.0 if undefined; null for status feeds or if 
     * location info was not requested using {@link FeedParameters#location(java.lang.Boolean) }.
     * 
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Set latitude.     
     * @param latitude Latitude, in decimal degrees.
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
        updateMap.put("lat", latitude);
    }

    /**
     * Get longitude. 
     * @return Longitude, in decimal degrees; 0.0 if undefined; null for status feeds or if
     * location info was not requested using {@link FeedParameters#location(java.lang.Boolean) }.
     */
    public Double getLongitude() {        
        return longitude;
    }

    /**
     * Set longitude.
     * @param longitude Longitude, in decimal degrees. 
     */
    public void setLong(Double longitude) {
        this.longitude = longitude;
        updateMap.put("long", longitude);
    }

    /**
     * Get elevation.
     * @return Elevation, in meters; 0.0 if undefined; null for status feeds or if
     * location info was not requested using {@link FeedParameters#location}.
     */
    public Double getElevation() {
        return elevation;
    }

    /**
     * Set elevation.
     * @param elevation Elevation, in meters. 
     */
    public void setElevation(Double elevation) {
        this.elevation = elevation;
        updateMap.put("elevation", elevation);
    }

    /**
     * Get status.
     * @return Status string; null for Channel and Field feeds if status info
     * was not requested using {@link FeedParameters#status(java.lang.Boolean)}
     */
    public String getStatus() {
        return status;
    }

    /**
     * Set status.
     * @param status Status string. 
     */
    public void setStatus(String status) {
        this.status = status;
        updateMap.put("status", status);
    }
    
    /**
     * Set Twitter username.  If set, a tweet will be posted to the user's
     * twitter feed for each channel update.
     * @param twitter Twitter username.
     */
    public void setTwitter(String twitter) {
        this.twitter = twitter;
        updateMap.put("twitter", twitter);
    }

    /**
     * Set Twitter message.  This message will be posted to the user's twitter
     * feed for each channel update.
     * @param tweet Twitter message.
     */
    public void setTweet(String tweet) {
        this.tweet = tweet;
        updateMap.put("tweet", tweet);
    }

    /**
     * Set the created date of an entry. If not explicitly set, the channel update time is used.
     * Useful when entries are not created and updated at the same time (offline mode, queuing to avoid rate-limiting, etc.)
     * @param created date which will be send to thingspeak
     */
    public void setCreated(Date created) {
        this.created_at = created;
        updateMap.put("created_at", created);
    }
	
    /**
     * Get date on which this channel entry was created.  Use 
     * {@link FeedParameters#offset(java.lang.Integer)} to adjust timezones.
     * @return Date.
     */
    public Date getCreated() {        
        return created_at;
    }

    /**
     * Get the ID of this entry.
     * @return Entry ID.
     */
    public Integer getEntryId() {
        return entry_id;
    }                  
}
