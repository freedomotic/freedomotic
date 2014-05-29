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
package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("plugins")
@Api(value = "/plugins", description = "Operations on plugins", position = 7)
public class PluginResource extends AbstractResource<Plugin>{

    private static final ClientStorage clientStorage =  Freedomotic.INJECTOR.getInstance(ClientStorage.class);
    
    @Override
    protected URI doCreate(Plugin o) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected boolean doDelete(String name) {
        Plugin p =prepareSingle(name);
        if (p!=null) {
            clientStorage.remove(p);
            return true;
        }
        return false;
    }

    @Override
    protected Plugin doUpdate(Plugin o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<Plugin> prepareList() {
        List<Plugin> plugins = new ArrayList<Plugin>();
        for (Client c : clientStorage.getClients("plugin")){
            plugins.add((Plugin)c);
        }
        return plugins;
    }

    @Override
    protected Plugin prepareSingle(String name) {
        for (Client c : clientStorage.getClients("plugin")){
            if (c.getName().equalsIgnoreCase(name)){
                return (Plugin) c;
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
    public Response start(@PathParam("id")String name){
        Plugin p = prepareSingle(name);
        if (p !=null){
            p.start();
            return Response.accepted().build();
        }
        return Response.notModified().build();
    }
    
    @POST
    @Path("/{id}/stop")
    @ApiOperation("Stop a plugin")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Plugin not found"),
        @ApiResponse(code = 202, message = "Plugin stopped"),
        @ApiResponse(code = 304, message = "Plugin not stopped")
    })
    public Response stop(@PathParam("id")String name){
        Plugin p = prepareSingle(name);
        if (p !=null){
            p.stop();
            return Response.accepted().build();
        }
        return Response.notModified().build();
    }

    @Override
    protected URI doCopy(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
