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

import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.reactions.Trigger;
import com.wordnik.swagger.annotations.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("triggers")
@Api(value = "/triggers", description = "Operations on triggers", position = 4)
public class TriggerResource extends AbstractResource<Trigger> {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all triggers", position = 10)
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
    @ApiOperation(value = "Get a trigger", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Trigger not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of trigger to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete a trigger", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Trigger not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of trigger to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.delete(UUID);
    }
    
     /**
     *
     * @param UUID
     * @param s
     * @return
     */
    @Override
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
        @ApiResponse(code = 304, message = "Trigger not modified")
    })
    @ApiOperation(value = "Update a trigger", position = 40)
    public Response update(
            @ApiParam(value = "UUID of trigger to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, Trigger s) {
        return super.update(UUID, s);
    }
    

    /**
     *
     * @param s
     * @return
     * @throws URISyntaxException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Add a new trigger", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New trigger added")
    })
    @Override
    public Response create(Trigger s) throws URISyntaxException {
        return super.create(s);
    }
    
    
    @Override
    protected URI doCreate(Trigger o) throws URISyntaxException {
        api.triggers().create(o);
        try {
            o.register();
        } catch (Exception e) {
            LOG.error("Cannot register trigger", e);
        }
        return createUri(o.getUUID());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.triggers().delete(UUID);
    }

    @Override
    protected Trigger doUpdate(String uuid, Trigger o) {
        o.setUUID(uuid);
        return api.triggers().modify(uuid, o);
    }

    @Override
    protected List<Trigger> prepareList() {
        return api.triggers().findAll();
    }

    @Override
    protected Trigger prepareSingle(String uuid) {
        return api.triggers().findOne(uuid);
    }

    @Override
    protected URI doCopy(String UUID) {
        Trigger found = api.triggers().findOne(UUID);
        Trigger t = api.triggers().copy(found);
        try {
            t.register();
        } catch (Exception e) {
            LOG.error("Cannot register trigger ", e);
        }
        return createUri(t.getUUID());
    }
}
