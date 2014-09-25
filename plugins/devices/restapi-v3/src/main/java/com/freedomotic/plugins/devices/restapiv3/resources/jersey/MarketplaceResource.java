/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("/marketplace/")
@Singleton
@Api(value = "marketplace", description = "Manage plugin installation from marketplace(s)", position = 100)
public class MarketplaceResource {

    MarketPlaceService mps = MarketPlaceService.getInstance();
    ArrayList<IPluginCategory> catList;
    protected final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    protected final static API api = INJECTOR.getInstance(API.class);

    @GET
    @Path("/providers")
    @ApiOperation(value = "Show the list of registered remote marketplace providers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listProviders() {
        return Response.ok(mps.getProviders()).build();
    }

    @GET
    @Path("/categories")
    @ApiOperation(value = "Download a list of plugin categories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listCategories() {
        catList = mps.getCategoryList();
        return Response.ok(catList).build();
    }

    @GET
    @Path("categories/{cat}/plugins")
    @ApiOperation(value = "Download a list of plugin from the specified category")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPlugins(
            @ApiParam(value = "Name of plugins category to fetch", required = true)
            @PathParam("cat") String cat) {
        if (cat != null && !cat.equals("")) {
            for (IPluginCategory category : catList) {
                if (category.getName().equalsIgnoreCase(cat)) {
                    return Response.ok(category.retrievePluginsInfo()).build();
                }
            }
            throw new ItemNotFoundException();
        }
        return Response.ok(mps.getPackageList()).build();
    }

    @POST
    @Path("/install/{url}")
    @ApiOperation(value = "Download and install a plugin")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Plugin installation succeded")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response installPlugin(
            @ApiParam(value = "URL of plugin to download and install", required = true)
            @PathParam("url") String pluginPath) {
        boolean done;
        PluginsManager plugMgr = api.getPluginManager();
        try {
            done = plugMgr.installBoundle(new URL(pluginPath));
        } catch (MalformedURLException ex) {
            done = false;
        }
        if (done) {
            return Response.accepted().build();
        }
        return Response.serverError().build();
    }
}
