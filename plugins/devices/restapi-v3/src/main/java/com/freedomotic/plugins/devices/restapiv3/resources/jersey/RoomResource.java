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

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.wordnik.swagger.annotations.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Room Resource
 *
 * @author Matteo Mazzoni
 */
@Path("rooms")
@Api(value = "/rooms", description = "Operations on rooms", position = 1)
public class RoomResource extends AbstractResource<Zone> {

    private String envUUID;
    private EnvironmentLogic env;

    public RoomResource() {
        authContext = "rooms";
        // set env and envUUID to the current environment
        this.env = api.environments().findAll().get(0);
        this.envUUID = env.getPojo().getUUID();
    }

    protected RoomResource(String envUUID) {
        this.envUUID = envUUID;
        this.env = api.environments().findOne(envUUID);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all rooms", position = 10)
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
    @ApiOperation(value = "Get a room", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Room not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of room to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete a room", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Room not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of room to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
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
        @ApiResponse(code = 304, message = "Room not modified")
    })
    @ApiOperation(value = "Update a room", position = 40)
    public Response update(
            @ApiParam(value = "UUID of room to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, Zone s) {
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
    @ApiOperation(value = "Add a new room", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New room added")
    })
    @Override
    public Response create(Zone s) throws URISyntaxException {
        return super.create(s);
    }

    @Override
    protected boolean doDelete(String ID) {
        try {
            env.removeZone(env.getZoneByUuid(ID));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected URI doCreate(Zone z) throws URISyntaxException {
        Room r = new Room(z);
        try {
            env.addRoom(r);
        } catch (Exception e) {
        }
        return createUri(envUUID);
    }

    @Override
    protected Zone doUpdate(String uuid, Zone z) {
        z.setUuid(uuid);
        Room zl = new Room(z);
        env.removeZone(env.getZoneByUuid(z.getUuid()));
        env.addRoom(zl);
        return z;
    }

    @Override
    protected List<Zone> prepareList() {
        List<Zone> rl = new ArrayList<Zone>();
        for (Room r : this.env.getRooms()) {
            rl.add(r.getPojo());
        }
        return rl;
    }

    @Override
    protected Zone prepareSingle(String uuid) {
        ZoneLogic zl = env.getZoneByUuid(uuid);
        if (zl != null) {
            return zl.getPojo();
        } else {
            return null;
        }
    }

    @Path("/{id}/things/")
    public ThingResource objects(
            @ApiParam(value = "UUID of room to fetch things from", required = true)
            @PathParam("id") String room) {
        return new ThingResource(envUUID, room);
    }

    @Override
    protected URI doCopy(String UUID) {
        Zone copy = new Zone();
        Zone orig = env.getZoneByUuid(UUID).getPojo();
        copy.setName("Copy of " + orig.getName());
        copy.setAsRoom(orig.isRoom());
        copy.setShape(orig.getShape());
        copy.setTexture(orig.getTexture());
        env.addRoom(new Room(copy));
        return createUri(copy.getUuid());
    }
}
