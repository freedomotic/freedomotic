/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.util.Info;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("/info")
@Singleton
@Api(value = "info", description = "Shows information about Freedomotic instance", position = 200)
public class InfoResource {

    protected static API api = Freedomotic.INJECTOR.getInstance(API.class);
    
    @GET
    @Path("/framework")
    @ApiOperation(value = "Show information about Freedomotic Framework")
    public Response listFrameworkSettings() {
        return Response.ok(Info.FRAMEWORK).build();
    }

    @GET
    @Path("/messaging")
    @ApiOperation(value = "Show information about the messaging system")
    public Response listMessaginsSettings() {
        return Response.ok(Info.MESSAGING).build();
    }

    @GET
    @Path("/paths")
    @ApiOperation(value = "Show information about configured paths")
    public Response listPathSettings() {
        return Response.ok(Info.PATHS).build();
    }

    @GET
    @Path("/users")
    @ApiOperation(value = "Show information about users and roles")
    public Response listUsers() {
        return Response.ok().build();
    }

    @GET
    @Path("/languages")
    @ApiOperation(value = "Show supported languages")
    public Response listLanguages() {
        return Response.ok(api.getI18n().getAvailableLocales()).build();
    }

}
