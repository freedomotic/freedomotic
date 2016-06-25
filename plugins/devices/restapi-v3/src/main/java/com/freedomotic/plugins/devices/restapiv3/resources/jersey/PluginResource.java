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

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("plugins")
@Api(value = "plugins", description = "Operations on plugins", position = 7)
public class PluginResource extends AbstractResource<Plugin> {

    @Override
    protected URI doCreate(Plugin o) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean doDelete(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected Plugin doUpdate(String uuid, Plugin o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all installed plugins", position = 10)
    @Override
    public Response list() {
        return super.list();
    }

    /**
     * @param UUID
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a plugin", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Plugin not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of plugin to fetch (e.g. logviewer, automationseditor)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    protected List<Plugin> prepareList() {
        List<Plugin> plugins = new ArrayList<Plugin>();
        for (Client c : api.getClients("plugin")) {
            plugins.add((Plugin) c);
        }
        return plugins;
    }

    @Override
    protected Plugin prepareSingle(String name) {
        for (Client c : api.getClients("plugin")) {
            Plugin plug = (Plugin) c;
            if (plug.getClassName().equalsIgnoreCase(name)) {
                return plug;
            }
        }
        return null;
    }

    @POST
    @ApiOperation("Start a plugin")
    @Path("/{id}/start")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Plugin not found"),
        @ApiResponse(code = 202, message = "Plugin started"),
        @ApiResponse(code = 304, message = "Plugin not started")
    })
    public Response start(
            @PathParam("id")
            @ApiParam(value = "Classname of plugin", required = true) String name) {
        if (!api.getAuth().isPermitted("sys:plugins:start:" + name)) {
            throw new ForbiddenException();
        }
        Plugin p = prepareSingle(name);
        if (p != null) {
            if (p.isAllowedToStart()) {
                p.start();
                return Response.accepted().build();
            } else {
                return Response.notModified("Cannot start plugin as its status is: " + p.getStatus()).build();
            }
        }
        throw new ItemNotFoundException();
    }

    @POST
    @Path("/{id}/stop")
    @ApiOperation("Stop a plugin")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Plugin not found"),
        @ApiResponse(code = 202, message = "Plugin stopped"),
        @ApiResponse(code = 304, message = "Plugin not stopped")
    })
    public Response stop(
            @PathParam("id")
            @ApiParam(value = "Classname of plugin", required = true) String name
    ) {
        if (!api.getAuth().isPermitted("sys:plugins:stop:" + name)) {
            throw new ForbiddenException();
        }
        Plugin p = prepareSingle(name);
        if (p != null) {
            if (p.isRunning()) {
                p.stop();
                return Response.accepted().build();
            } else {
                return Response.notModified("Plugin is not running").build();
            }
        }
        throw new ItemNotFoundException();
    }

    @Override
    protected URI doCopy(String UUID
    ) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
