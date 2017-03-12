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
package com.angryelectron.thingspeak;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>ThingSpeak status, channel, or field feed.  Methods for accessing feed info
 * and individual {@link Entry}s.  Only
 * the following methods are supported for status feeds, as they contain limited
 * channel information (other methods will return null):</p>
 * <ul>
 * <li>{@link #getChannelName() }</li>
 * <li>{@link #getEntry(java.lang.Integer)  }</li>
 * <li>{@link #getEntryList() }</li>
 * <li>{@link #getEntryMap() }</li>
 * </ul>
 * <p>To obtain a Feed, call one of the {@link Channel} methods.  For example,
 * to get feeds for channel 1234:
 * <pre>
 * {@code 
 * Channel channel = new Channel(1234);
 * Feed statusFeed = channel.getStatusFeed();
 * Feed channelFeed = channel.getChannelFeed();
 * Feed fieldFeed = channel.getFieldFeed(1);
 * }
 * </pre>
 */
public class Feed {

    /**
     * Although there are more sensible ways to build this class, the structure
     * must match the JSON returned by the ThingSpeak API to allow GSON to
     * de-serialize it.
     */
    private class ChannelInfo {

        private Date created_at;
        private String description;
        private String field1;
        private String field2;
        private String field3;
        private String field4;
        private String field5;
        private String field6;
        private String field7;
        private String field8;
        private Integer id;
        private Integer last_entry_id;
        private String name;
        private Date updated_at;
    }
    private final ChannelInfo channel = new ChannelInfo();
    private final ArrayList<Entry> feeds = new ArrayList<>();

    /**
     * Constructor is package/class private so it can only be created via GSON
     * and not created directly.
     */
    private Feed() {

    }

    /**
     * Get channel creation date.  Use {@link FeedParameters#offset(java.lang.Integer)}
     * to adjust to local time zone.
     *
     * @return Date on which this channel was created; null for status feeds.
     */
    public Date getChannelCreationDate() {
        return channel.created_at;
    }

    /**
     * Get channel description. Channel description can be set via the
     * ThingSpeak server web interface.
     *
     * @return Description of this channel; null for status feeds.
     */
    public String getChannelDescription() {
        return channel.description;
    }

    /**
     * Get the user-defined name of a field. Define fields and names via the ThingSpeak server
     * web interface.
     *
     * @param field 1-8.
     * @return The assigned name of the field; null for status feeds or if the
     * field is unassigned.
     */
    public String getFieldName(Integer field) {
        switch (field) {
            case 1:
                return channel.field1;
            case 2:
                return channel.field2;
            case 3:
                return channel.field3;
            case 4:
                return channel.field4;
            case 5:
                return channel.field5;
            case 6:
                return channel.field6;
            case 7:
                return channel.field7;
            case 8:
                return channel.field8;
        }
        throw new IllegalArgumentException("Invalid field.");
    }

    /**
     * Get the ID of this channel.
     *
     * @return ID of this channel; null for status feeds.
     */
    public Integer getChannelId() {
        return channel.id;
    }

    /**
     * Get the ID of the last entry made to this channel. 
     *
     * @return The ID of the last entry made in this channel; null for status
     * feeds.
     */
    public Integer getChannelLastEntryId() {
        return channel.last_entry_id;
    }

    /**
     * Get the name of this channel. Set a name for the channel using the
     * ThingSpeak server's web interface.
     *
     * @return The name of this channel; null if not set.
     */
    public String getChannelName() {
        return channel.name;
    }

    /**
     * Get the date of the last channel update.  Use {@link FeedParameters#offset(java.lang.Integer)}
     * to adjust to local timezone.
     *
     * @return The date of the last update of this channel; null for status feeds.
     */
    public Date getChannelUpdateDate() {
        return channel.updated_at;
    }

    /**
     * Get a List of all {@link Entry}s in this feed.
     *
     * @return All Entries in this feed.
     */
    public ArrayList<Entry> getEntryList() {
        return this.feeds;
    }

    /**
     * Get a Map of all {@link Entry}s in this feed. 
     *
     * @return All Entries in this feed, keyed by entry ID.
     */
    public Map<Integer, Entry> getEntryMap() {
        HashMap<Integer, Entry> map = new HashMap<>();
        for (Entry entry : this.feeds) {
            map.put(entry.getEntryId(), entry);
        }
        return map;
    }

    /**
     * Get an Entry in the feed by ID. If the feed is large, or you need to lookup
     * many different entries, this method could be quite slow. It may be better
     * to call {@link #getEntryMap()} to obtain a map of entries indexed by id.
     *
     * @param id Entry ID.
     * @return Entry.
     * @throws ThingSpeakException if the feed does not contain an Entry with
     * the given id.
     */
    public Entry getEntry(Integer id) throws ThingSpeakException {
        for (Entry entry : this.feeds) {
            if (entry.getEntryId().equals(id)) {
                return entry;
            }
        }
        throw new ThingSpeakException("Entry with ID " + id + " not found in feed.");

    }

    /**
     * Get the last / latest entry in this feed. If you only need the last entry
     * and not the rest of the feed, consider using
     * {@link Channel#getLastChannelEntry()}.
     *
     * @return An Entry with id equal to the feed's last_entry_id.
     * @throws ThingSpeakException The channel does not have a last entry or the 
     * feed is a Status feed.
     */
    public Entry getChannelLastEntry() throws ThingSpeakException {
        return getEntry(channel.last_entry_id);
    }

}
