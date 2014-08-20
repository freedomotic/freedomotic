/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.util.Info;
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
 * @author matteo
 */
@Path("/system")
@Singleton
@Api(value = "system", description = "Manages Freedomotic instance", position = 200)
public class SystemResource {

    protected static API api = Freedomotic.INJECTOR.getInstance(API.class);
    
    @GET
    @Path("/info/framework")
    @ApiOperation(value = "Show information about Freedomotic Framework")
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
        GenericEvent exitSignal = new GenericEvent(this);
        exitSignal.setDestination("app.event.system.exit");
        Freedomotic.sendEvent(exitSignal);
        return Response.accepted().build();
    }
}
