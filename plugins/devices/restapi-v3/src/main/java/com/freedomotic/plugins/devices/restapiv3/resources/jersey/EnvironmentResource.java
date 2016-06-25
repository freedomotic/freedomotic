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
import com.freedomotic.model.environment.Environment;
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
 * Environment Resource
 *
 * @author Matteo Mazzoni
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

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete an environment", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Environment not found")
    })
    public Response delete(
            @ApiParam(value = "UUID of environment to delete (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return super.delete(UUID);
    }

    public EnvironmentResource() {
        authContext = "environments";
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
        @ApiResponse(code = 304, message = "Environment not modified")
    })
    @ApiOperation(value = "Update an environment", position = 40)
    public Response update(
            @ApiParam(value = "UUID of environment to update (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID, Environment s) {
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
    @ApiOperation(value = "Add a new environment", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New environment added")
    })
    @Override
    public Response create(Environment s) throws URISyntaxException {
        return super.create(s);
    }

    @Override
    protected List<Environment> prepareList() {
        List<Environment> environments = new ArrayList<Environment>();
        for (EnvironmentLogic log : api.environments().findAll()) {
            environments.add(log.getPojo());
        }
        return environments;
    }

    @Override
    protected Environment prepareSingle(String uuid) {
        EnvironmentLogic el = api.environments().findOne(uuid);
        if (el != null) {
            return el.getPojo();
        }
        return null;
    }

    @Override
    protected boolean doDelete(String UUID) {
        EnvironmentLogic env = api.environments().findOne(UUID);
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
    protected Environment doUpdate(String UUID, Environment eo) {
        EnvironmentLogic el = INJECTOR.getInstance(EnvironmentLogic.class);
        eo.setUUID(UUID);
        el.setPojo(eo);
        if (api.environments().modify(UUID, el) != null) {
            return el.getPojo();
        } else {
            return null;
        }
    }

    @Path("/{id}/rooms")
    public RoomResource rooms(
            @ApiParam(value = "UUID of environment to fetch rooms from (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return new RoomResource(UUID);
    }

    @Path("/{id}/things")
    public ThingResource things(
            @ApiParam(value = "UUID of environment to fetch things from (e.g. df28cda0-a866-11e2-9e96-0800200c9a66)", required = true)
            @PathParam("id") String UUID) {
        return new ThingResource(UUID);
    }

    @Override
    protected URI doCopy(String UUID) {
        EnvironmentLogic found = api.environments().findOne(UUID);
        EnvironmentLogic el = api.environments().copy(found);
        return createUri(el.getPojo().getUUID());
    }
}
