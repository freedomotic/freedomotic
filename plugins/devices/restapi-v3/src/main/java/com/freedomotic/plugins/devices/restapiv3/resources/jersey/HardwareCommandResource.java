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
import com.freedomotic.reactions.Command;
import com.wordnik.swagger.annotations.*;
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
@Path("commands/hardware")
@Api(value = "hardwareCommands", description = "Operations on hardware commands", position = 6)
public class HardwareCommandResource extends AbstractResource<Command> {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all hardware commands", position = 10)
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
    @ApiOperation(value = "Get a hardware command", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Hardware command not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of hardware command to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete a hardware command", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Hardware command not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of hardware command to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66", required = true)
            @PathParam("id") String UUID) {
        return super.delete(UUID);
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
    @ApiOperation(value = "Add a new hardware command", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New hardware command added")
    })
    @Override
    public Response create(Command s) throws URISyntaxException {
        return super.create(s);
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
        @ApiResponse(code = 304, message = "Hardware command not modified")
    })
    @ApiOperation(value = "Update a hardware command", position = 40)
    public Response update(
            @ApiParam(value = "UUID of hardware command to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, Command s) {
        return super.update(UUID, s);
    }

    public HardwareCommandResource() {
        authContext = "commands";
    }

    @Override
    protected URI doCreate(Command c) throws URISyntaxException {
        c.setHardwareLevel(true);
        api.commands().create(c);
        return createUri(c.getUuid());
    }

    @Override
    protected boolean doDelete(String UUID) {
        return api.commands().delete(UUID);
    }

    @Override
    protected Command doUpdate(String uuid, Command c) {
        c.setUUID(uuid);
        return api.commands().modify(uuid, c);
    }

    @Override
    protected List<Command> prepareList() {
        List<Command> cl = new ArrayList<Command>();
        cl.addAll(api.commands().findHardwareCommands());
        return cl;
    }

    @Override
    protected Command prepareSingle(String uuid) {
        return api.commands().findOne(uuid);
    }

    @Override
    protected URI doCopy(String uuid) {
        Command found = api.commands().findOne(uuid);
        Command c = api.commands().copy(found);
        return createUri(c.getUuid());
    }
}
