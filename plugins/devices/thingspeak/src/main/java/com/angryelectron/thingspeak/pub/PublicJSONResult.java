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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * POJO to handle de-serialized JSON Public Channel data. Provides methods to
 * PublicIterator for accessing paging info.
 * @author abythell
 */
class PublicJSONResult {
    
    /**
     * This class must match the JSON returned by Thingspeak.
     */
    private class Pagination {
        private Integer current_page;
        private Integer per_page;
        private Integer total_entries;
    }

    /**
     * These members must match the JSON returned by Thingspeak.
     */
    private final Pagination pagination = new Pagination();
    private final ArrayList<PublicChannel> channels = new ArrayList<>();
   
    /**
     * Get the current page represented by the data in channels.
     * @return Page number.
     */
    Integer getCurrentPage() {
        return pagination.current_page;
    }
        
    /**
     * Determine if the current page is the last one in the set.
     * @return True if this is the last page.
     */
    Boolean isLastPage() {
        if (pagination.total_entries <= pagination.per_page) {
            return true;
        } else {
            Double pages = (double) pagination.total_entries / pagination.per_page;
            return (pages.intValue() == pagination.current_page);        
        }
    }
    
    /**
     * Get the iterator for the channel data.  Used by PublicIterator to access
     * the PublicChannel objects stored in the current page.
     * @return 
     */
    Iterator<PublicChannel> iterator() {
        return channels.iterator();
    }
    
    /**
     * Get a total count of all public channels.
     * @return Number of public channels in this result.
     */
    Integer getTotalEntries() {
        return pagination.total_entries;
    }

}
