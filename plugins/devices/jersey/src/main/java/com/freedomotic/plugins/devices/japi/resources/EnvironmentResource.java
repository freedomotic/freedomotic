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
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.model.environment.Environment;
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
 * Environment Resource
 *
 * @author matteo
 */
@Path("environments")
@Api(value = "environments", description = "Operations on environments", position = 0)
public class EnvironmentResource extends AbstractResource<Environment> {

    @Override
    protected List<Environment> prepareList() {
        List<Environment> environments = new ArrayList<Environment>();
        for (EnvironmentLogic log : api.environments().list()) {
            environments.add(log.getPojo());
        }
        return environments;
    }

    @Override
    protected Environment prepareSingle(String uuid) {
        EnvironmentLogic el = api.environments().get(uuid);
        if (el != null) {
            return el.getPojo();
        }
        return null;
    }

    @Override
    protected boolean doDelete(String UUID) {
        EnvironmentLogic env = api.environments().get(UUID);
        if (env != null) {
            return api.environments().delete(UUID);
        } else {
            return false;
        }
    }

    @Override
    protected URI doCreate(Environment eo) throws URISyntaxException {
        EnvironmentLogic el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(eo);
        api.environments().create(el);
        return createUri(el.getPojo().getUUID());
    }

    @Override
    protected Environment doUpdate(Environment eo) {
        EnvironmentLogic el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(eo);
        if (api.environments().modify(eo.getUUID(), el) != null) {
            return el.getPojo();
        } else {
            return null;
        }
    }

    @Path("/{id}/rooms")
    public RoomResource rooms(
            @ApiParam(value = "Environment to fetch rooms from", required = true)
            @PathParam("id") String UUID) {
        return new RoomResource(UUID);
    }

    @Path("/{id}/objects")
    public ObjectResource objects(
            @ApiParam(value = "Environment to fetch objects from", required = true)
            @PathParam("id") String UUID) {
        return new ObjectResource(UUID);
    }

    @Override
    protected URI doCopy(String UUID) {
        EnvironmentLogic el = api.environments().copy(UUID);
        return createUri(el.getPojo().getUUID());
    }

}
