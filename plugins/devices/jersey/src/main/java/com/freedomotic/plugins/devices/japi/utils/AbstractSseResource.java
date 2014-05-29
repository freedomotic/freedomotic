/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.japi.utils;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.reactions.Payload;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;

/**
 *
 * @author matteo
 */
public abstract class AbstractSseResource implements SseResouceInterface {

    private static final SseBroadcaster BROADCASTER = new SseBroadcaster();

    @Override
    @GET
    @Produces(SseFeature.SERVER_SENT_EVENTS)
    @ApiOperation("Listens to a SSE channel")
    public EventOutput getSSE() {
        final EventOutput eventOutput = new EventOutput();
        BROADCASTER.add(eventOutput);
        return eventOutput;
    }

    protected static void broadcast(String name, Object data,Class type, String id) {
        OutboundEvent.Builder eventBuilder = new OutboundEvent.Builder();
        OutboundEvent message = eventBuilder
                .name(name)
                .mediaType(MediaType.APPLICATION_JSON_TYPE)
                .data(type, data)
                .id(id)
                .build();
        BROADCASTER.broadcast(message);
        // enable next line for debug purposes only!!
        // BROADCASTER.closeAll();
    }

    public static void broadcast(EventTemplate event) {
        broadcast(event.getEventName(), event.getPayload(), Payload.class, new Long(event.getCreation()).toString());
    }

}
