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

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.environment.Room;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author matteo
 */
@Path("environments/{envid}/rooms")
public class RoomResource extends AbstractResource<Zone> {

    final private String endUUID;
    final private EnvironmentLogic env;
    protected RoomResource(@PathParam("envid") String endUUID) {
        this.endUUID = endUUID;
        this.env = EnvironmentPersistence.getEnvByUUID(this.endUUID);  
    }

    @Override
    protected boolean doDelete(String ID) {
        boolean found = false;
        for (Room r : env.getRooms()) {
            if (r.getPojo().getName().equalsIgnoreCase(ID)) {
                env.removeZone(r);
                found = true;
                break;
            }
        }
        return found;
    }

    @Override
    protected URI doCreate(Zone z) throws URISyntaxException {
        Room r = new Room (z);
        env.addRoom(r);
        return UriBuilder.fromResource(this.getClass()).path("/" + z.getName()).build(endUUID);
    }

    @Override
    protected Zone doUpdate(Zone z) {
        Room  zl = new Room(z);
        env.removeZone(env.getZone(z.getName()));
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
        return env.getZone(uuid).getPojo();
    }

}
