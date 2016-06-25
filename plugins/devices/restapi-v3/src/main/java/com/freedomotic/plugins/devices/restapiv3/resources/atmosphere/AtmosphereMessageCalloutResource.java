/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.atmosphere;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.freedomotic.plugins.devices.restapiv3.representations.MessageCalloutRepresentation;
import com.wordnik.swagger.annotations.Api;
import javax.ws.rs.Path;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
@Path(AtmosphereMessageCalloutResource.PATH)
@Api(value = "ws_messageCallout", description = "WS for receiving generic notifications", position = 10)
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class},
        path = "/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereMessageCalloutResource.PATH,
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class AtmosphereMessageCalloutResource extends AbstractWSResource {

    private static final Logger LOG = LoggerFactory.getLogger(AtmosphereMessageCalloutResource.class.getName());

    public final static String PATH = "messagecallout";

    @Override
    public void broadcast(EventTemplate message) {
        if (api != null) {
            String msg;
            try {
                msg = om.writeValueAsString(new MessageCalloutRepresentation(message.getProperty("message.text")));
                BroadcasterFactory
                        .getDefault()
                        .lookup("/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereMessageCalloutResource.PATH)
                        .broadcast(msg);
            } catch (JsonProcessingException ex) {
            }
        }
    }
}
