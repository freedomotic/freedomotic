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

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
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
 * Environment Resource
 *
 * @author matteo
 */
@Path("environments")
@Api(value = "environments", description = "Operations on environments", position = 0)
public class EnvironmentResource extends AbstractResource<Environment> {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all environments", position = 10)
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
    @ApiOperation(value = "Get an environment", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Environment not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "UUID of environment to fetch (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    public EnvironmentResource() {
        authContext = "environments";
    }

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
        EnvironmentLogic el = INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(eo);
        api.environments().create(el);
        return createUri(el.getPojo().getUUID());
    }

    @Override
    protected Environment doUpdate(Environment eo) {
        EnvironmentLogic el = INJECTOR.getInstance(EnvironmentLogic.class);
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
