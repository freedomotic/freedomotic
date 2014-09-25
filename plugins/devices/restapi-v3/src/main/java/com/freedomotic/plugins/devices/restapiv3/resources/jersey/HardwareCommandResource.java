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

import com.freedomotic.model.environment.Environment;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.reactions.Command;
import com.wordnik.swagger.annotations.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
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
        Command item = prepareSingle(UUID);
        if (item != null) {
            return Response.ok(item).build();
        }
        throw new ItemNotFoundException();
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
    protected Command doUpdate(Command c) {
        return api.commands().modify(c.getUuid(), c);
    }

    @Override
    protected List<Command> prepareList() {
        List<Command> cl = new ArrayList<Command>();
        cl.addAll(api.commands().getHardwareCommands());
        return cl;
    }

    @Override
    protected Command prepareSingle(String uuid) {
        return api.commands().get(uuid);
    }

    @Override
    protected URI doCopy(String uuid) {
        Command c = api.commands().copy(uuid);
        return createUri(c.getUuid());
    }
}
