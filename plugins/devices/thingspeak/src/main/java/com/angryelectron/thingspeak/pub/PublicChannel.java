/**
 * ThingSpeak Java Client Copyright 2014, Andrew Bythell <abythell@ieee.org>
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
package com.angryelectron.thingspeak.pub;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A ThingSpeak public channel listing.
 *
 * @author abythell
 */
public class PublicChannel {

    /**
     * These fields must match the JSON returned by ThingSpeak. They are
     * populated by de-serializing a JSON stream using GSON in the
     * PublicIterator class.
     */
    private Date created_at;
    private String description;
    private Double elevation;
    private Integer id;
    private Integer last_entry_id;
    private Double latitude;
    private Double longitude;
    private String name;
    private Integer ranking;
    private String username;
    private ArrayList<Tag> tags = new ArrayList<>();

    /**
     * This class must match the JSON returned by ThingSpeak.
     */
    private class Tag {

        private Integer id;
        private String name;
    }

    /**
     * Get channel name.
     *
     * @return Name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get channel creation date.
     *
     * @return Date.
     */
    public Date getCreatedAt() {
        return created_at;
    }

    /**
     * Get channel description.
     *
     * @return Description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get channel elevation.
     *
     * @return Elevation, or 0.0 if no elevation was set for the channel.
     */
    public Double getElevation() {
        return elevation;
    }

    /**
     * Get channel id. The id is required to access the channel's feed.
     *
     * @return Channel id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * Get the id of the last entry posted to this channel.
     *
     * @return Entry id.
     */
    public Integer getLastEntryId() {
        return last_entry_id;
    }

    /**
     * Get the latitude of this channel.
     *
     * @return Latitude in decimal degrees, or 0.0 if no latitude was set for
     * this channel.
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude of this channel.
     *
     * @return Longitude in decimal degrees, or 0.0 if no longitude was set for
     * this channel.
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Get the channel's ranking. It is unclear to the author how rankings are
     * compiled.
     *
     * @return Rank.
     */
    public Integer getRanking() {
        return ranking;
    }

    /**
     * Get the name of the user who owns this channel.
     *
     * @return Username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Get the channel's tags.
     *
     * @return A List of tags, which can be passed to
     * {@link PublicChannelCollection#PublicChannelCollection(java.lang.String)}
     * to obtain a list of other public channels containing the same tag.
     */
    public List<String> getTags() {
        List<String> list = new ArrayList<>();
        for (Tag t : tags) {
            list.add(t.name);
        }
        return list;
    }

}
