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

package com.angryelectron.thingspeak.pub;

import java.util.AbstractCollection;
import java.util.Iterator;

/**
 * A collection of public ThingSpeak channels. The ThingSpeak Public API returns
 * paginated results, presumably because it could return a large set of
 * channels. This collection transparently fetches additional pages on-demand,
 * instead of trying to load all results into memory. Access the
 * {@link PublicChannel} objects in this collection by iterating:
 * <pre>
 * {@code
 * PublicChannelCollection pl = new PublicChannelCollection("temperature");
 * Iterator<PublicChannel> publicIterator = pl.iterator();
 * while (publicIterator.hasNext()) {
 *    PublicChannel p = publicIterator.next();
 * }
 * }
 * </pre> or a for loop:
 * <pre>
 * {@code
 * PublicChannelCollection publicChannels = new PublicChannelCollection("cheerlights");
 * for (PublicChannel channel : publicChannels) {
 *     //do something with channel
 * }
 * }
 * </pre>
 *
 * @author abythell
 */
public class PublicChannelCollection extends AbstractCollection<PublicChannel> {

    private final String url = "http://api.thingspeak.com";   
    private final String tag;
    private Integer size;

    /**
     * Create a collection containing all public channels.
     */
    public PublicChannelCollection() {
        tag = null;
    }

    /**
     * Create a collection containing all public channels with the given tag.
     * @param tag Tag.
     */
    public PublicChannelCollection(String tag) {
        this.tag = tag;
    }

    /**
     * Use a server other than thingspeak.com. If you are hosting your own
     * Thingspeak server, set the url of the server here.  
     * @param url eg. http://localhost, http://thingspeak.local:3000, etc.
     */    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Public API is not implemented in open-source servers.");
    }

    /**
     * Get a PublicChannel iterator, for iterating through the collection.
     * @return Iterator.
     */
    @Override
    public Iterator<PublicChannel> iterator() {
        PublicIterator iterator = new PublicIterator(url, tag);
        size = iterator.size();
        return iterator;
    }
    
    /**
     * Get the number of public channels in this collection.
     * @return Number of channels.
     */
    @Override
    public int size() {
        return size;
    }
            
}
