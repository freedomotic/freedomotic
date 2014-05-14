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
package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 */
@Path("commands/user")
@Api(value = "userCommands", description = "Operations on user commands", position=5)
public class UserCommandResource extends AbstractResource<Command> {

    @Override
    protected URI doCreate(Command c) throws URISyntaxException {
        CommandPersistence.add(c);
        return UriBuilder.fromResource(this.getClass()).path(c.getUUID()).build();
    }

    @Override
    protected boolean doDelete(String UUID) {
        Command c = CommandPersistence.getCommand(UUID);
        if (c != null) {
            CommandPersistence.remove(c);
            return true;
        }
        return false;
    }

    @Override
    protected Command doUpdate(Command c) {
        for (Command oldC : CommandPersistence.getUserCommands()) {
            if (oldC.getUUID().equals(c.getUUID())) {
                CommandPersistence.remove(oldC);
                CommandPersistence.add(c);
                return c;
            }
        }
        return null;
    }

    @Override
    protected List<Command> prepareList() {
        List<Command> lc = new ArrayList<Command>();
        lc.addAll(CommandPersistence.getUserCommands());
        return lc;
    }

    @Override
    protected Command prepareSingle(String uuid) {
        Command c = CommandPersistence.getCommand(uuid);
        if (c == null) {
            c = CommandPersistence.getCommandByUUID(uuid);
        }
        return c;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/{id}/run")
    @ApiOperation("Fires a user command")
    public Response fire(
            @ApiParam(value = "Name of Command to execute", required = true)
            @PathParam("id") String UUID) {
        Command c = CommandPersistence.getCommand(UUID);
        if (c != null) {
            Freedomotic.sendCommand(c);
        }
        return Response.accepted(c).build();
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @Path("/runonce")
    @ApiOperation("Fires a custom command")
    public Response fire(Command c) {
        if (c != null) {
            Freedomotic.sendCommand(c);
        }
        return Response.accepted(c).build();
    }

    @Override
    protected URI doCopy(String name) {
        Command c;
        try {
            c = CommandPersistence.getCommand(name).clone();
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(UserCommandResource.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        CommandPersistence.add(c);
        return createUri(c.getName());
    }
}
