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

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiParam;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

/**
 *
 * @author matteo
 */
@Path("rooms")
@Api(value = "/rooms", description = "Operations on rooms", position = 1)
public class RoomResource extends AbstractResource<Zone> {

    final private String envUUID;
    final private EnvironmentLogic env;

    protected RoomResource(String endUUID) {
        this.envUUID = endUUID;
        this.env = api.environments().get(endUUID);
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
    protected Zone doUpdate(Zone z) {
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

    @Path("/{id}/objects/")
    public ObjectResource objects(
            @ApiParam(value = "Room to fetch objects from", required = true)
            @PathParam("id") String room) {
        return new ObjectResource(envUUID, room);
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
