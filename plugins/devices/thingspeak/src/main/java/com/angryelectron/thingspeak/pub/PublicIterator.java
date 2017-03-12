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

import com.angryelectron.thingspeak.ThingSpeakException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.GetRequest;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Provides a custom iterator for PublicChannelCollections which works with
 * ThingSpeak's paginated results.
 *
 * @author abythell
 */
class PublicIterator implements Iterator<PublicChannel> {

    /**
     * URL of the Thingspeak server.
     */
    private final String url;

    /**
     * Optional tag to search for. If null, all channels are returned.
     */
    private final String tag;

    /**
     * Current page of results and an iterator to the list of PublicChannel
     * channels it contains.
     */
    private PublicJSONResult results;
    private Iterator<PublicChannel> iterator;

    /**
     * ThingSpeak channels don't always contain elevation, latitude, or
     * longitude data. These empty strings cause NumberFormatExceptions when
     * using the default GSON de-serializer for Double. This replacement
     * de-serializes empty strings to 0.0.
     */
    private static class LocationDeserializer implements JsonDeserializer<Double> {

        @Override
        public Double deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                return Double.parseDouble(json.getAsString());
            } catch (NumberFormatException ex) {
                return 0.0;
            }
        }
    }

    /**
     * Build a GSON de-serializer for the ThingSpeak PublicChannel Channel JSON.
     */
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Double.class, new LocationDeserializer())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX").create();

    /**
     * Constructor.
     *
     * @param url ThingSpeak server URL (eg. http://api.thingspeak.com).
     * @param tag Get channels with this tag only, or null to return all
     * channels.
     */
    protected PublicIterator(String url, String tag) {
        this.url = url;
        this.tag = tag;
        thingRequest(1);
    }

    /**
     * Make requests to the ThingSpeak server and parse the results.
     *
     * @param page The page of results to request.
     */
    private void thingRequest(Integer page) {
        try {
            GetRequest request = Unirest.get(url + "/channels/public.json");
            if (tag != null) {
                request.field("tag", tag);
            }
            request.field("page", page);
            HttpResponse<JsonNode> response = request.asJson();
            if (response.getCode() != 200) {
                throw new ThingSpeakException("Request failed with code " + response.getCode());
            }
            results = gson.fromJson(response.getBody().toString(), PublicJSONResult.class);
            iterator = results.iterator();
        } catch (UnirestException | ThingSpeakException ex) {
            Logger.getLogger(PublicIterator.class.getName()).log(Level.SEVERE, null, ex);
            results = null;
        }
    }

    @Override
    public boolean hasNext() {
        if (iterator.hasNext()) {
            /* current page still has unreturned channels */
            return true;
        } else if (results.isLastPage()) {
            /* current page has returned all channels and there
             * are no more pages left */
            return false;
        } else {
            /* current page has returned all channels but there
             * are more pages remaining. */
            thingRequest(results.getCurrentPage() + 1);
            return true;
        }
    }

    @Override
    public PublicChannel next() {
        return iterator.next();
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    Integer size() {
        return results.getTotalEntries();
    }

}
