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
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.plugins.devices.restapiv3.filters.ForbiddenException;
import com.freedomotic.settings.Info;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("/system")
@Singleton
@Api(value = "system", description = "Manages Freedomotic instance", position = 200)
public class SystemResource {

    protected final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    protected final static API api = INJECTOR.getInstance(API.class);

    @GET
    @Path("/info/framework")
    @ApiOperation(value = "Show information about Freedomotic framework")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listFrameworkSettings() {
        return Response.ok(Info.FRAMEWORK).build();
    }

    @GET
    @Path("/info/messaging")
    @ApiOperation(value = "Show information about the messaging system")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listMessaginsSettings() {
        return Response.ok(Info.MESSAGING).build();
    }

    @GET
    @Path("/info/paths")
    @ApiOperation(value = "Show information about configured paths")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPathSettings() {
        return Response.ok(Info.PATHS).build();
    }

    @GET
    @Path("/info/languages")
    @ApiOperation(value = "Show supported languages")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLanguages() {
        return Response.ok(api.getI18n().getAvailableLocales()).build();
    }

    @POST
    @Path("/exit")
    @ApiOperation(value = "Initiate shutdown procedure")
    @Produces(MediaType.APPLICATION_JSON)
    public Response exit() {
        if (api.getAuth().isPermitted("sys:shutdown")) {
            GenericEvent exitSignal = new GenericEvent(this);
            exitSignal.setDestination("app.event.system.exit");
            Freedomotic.sendEvent(exitSignal);
            return Response.accepted().build();
        } else {
            throw new ForbiddenException("Only privileged users are able to shutdown system");
        }
    }
}
