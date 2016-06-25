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
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.plugins.devices.restapiv3.RestAPIv3;
import com.wordnik.swagger.annotations.Api;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.atmosphere.client.TrackMessageSizeInterceptor;
import org.atmosphere.config.service.AtmosphereService;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.interceptor.AtmosphereResourceLifecycleInterceptor;

/**
 *
 * @author Matteo Mazzoni
 */
@Path(AtmosphereZoneChangeResource.PATH)
@Api(value = "ws_zoneChange", description = "WS for zone change events", position = 10)
@AtmosphereService(
        dispatch = false,
        interceptors = {AtmosphereResourceLifecycleInterceptor.class, TrackMessageSizeInterceptor.class},
        path = "/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereZoneChangeResource.PATH,
        servlet = "org.glassfish.jersey.servlet.ServletContainer")
public class AtmosphereZoneChangeResource extends AbstractWSResource {

    public final static String PATH = "zonechange";

    @POST
    @Override
    public void broadcast(EventTemplate message) {
        if (api != null) {
            for (EnvironmentLogic e : api.environments().findAll()) {
                ZoneLogic z = e.getZone(message.getPayload().getStatementValue("zone.name"));
                if (z != null) {
                    try {
                        BroadcasterFactory.getDefault()
                                .lookup("/" + RestAPIv3.API_VERSION + "/ws/" + AtmosphereZoneChangeResource.PATH)
                                .broadcast(
                                        om.writeValueAsString(z));
                    } catch (JsonProcessingException ex) {

                    }
                    return;
                }
            }
        }
    }
}
