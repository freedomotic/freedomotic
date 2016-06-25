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

import com.freedomotic.plugins.devices.restapiv3.representations.ReactionRepresentation;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.reactions.Reaction;
import com.wordnik.swagger.annotations.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("reactions")
@Api(value = "reactions", description = "Operations on reactions", position = 3)
public class ReactionResource extends AbstractResource<ReactionRepresentation> {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all reactions", position = 10)
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
    @ApiOperation(value = "Get a reaction", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Reaction not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of reaction to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete a reaction", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Reaction not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of reaction to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
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
        @ApiResponse(code = 304, message = "Reaction not modified")
    })
    @ApiOperation(value = "Update a reaction", position = 40)
    public Response update(
            @ApiParam(value = "UUID of reaction to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, ReactionRepresentation s) {
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
    @ApiOperation(value = "Add a new reaction", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New reaction added")
    })
    @Override
    public Response create(ReactionRepresentation s) throws URISyntaxException {
        return super.create(s);
    }

    public ReactionResource() {
        authContext = "reactions";
    }

    @Override
    protected URI doCopy(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        // api.reactions().copy(UUID);
        // return createUri(UUID);
    }

    @Override
    protected URI doCreate(ReactionRepresentation o) throws URISyntaxException {
        Reaction r = new Reaction();
        if (o.getUuid() != null && !o.getUuid().isEmpty()) {
            r.setUuid(o.getUuid());
        }
        r.setTrigger(api.triggers().findOne(o.getTriggerUuid()));
        for (HashMap<String, String> c : o.getCommands()) {
            r.getCommands().add(api.commands().findOne(c.get("uuid")));
        }
        r.setConditions(o.getConditions());
        r.setChanged();
        api.reactions().create(r);
        return createUri(r.getUuid());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.reactions().delete(UUID);
    }

    @Override
    protected ReactionRepresentation doUpdate(String uuid, ReactionRepresentation o) {
        o.setUuid(uuid);
        doDelete(o.getUuid());
        try {
            doCreate(o);
        } catch (URISyntaxException ex) {
            return null;
        }
        return o;
    }

    @Override
    protected List<ReactionRepresentation> prepareList() {
        ArrayList<ReactionRepresentation> list = new ArrayList<ReactionRepresentation>();
        for (Reaction r : api.reactions().findAll()) {
            list.add(new ReactionRepresentation(r));
        }
        return list;
    }

    @Override
    protected ReactionRepresentation prepareSingle(String uuid) {
        Reaction r = api.reactions().findOne(uuid);
        if (r != null) {
            return new ReactionRepresentation(r);
        }
        return null;
    }
}
