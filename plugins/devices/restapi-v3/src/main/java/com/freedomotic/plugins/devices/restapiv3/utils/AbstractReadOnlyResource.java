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
package com.freedomotic.plugins.devices.restapiv3.utils;

import com.freedomotic.api.API;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.filters.ForbiddenException;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 * @param <T>
 */
public abstract class AbstractReadOnlyResource<T> implements ResourceReadOnlyInterface<T> {

    public static final Logger LOG = LoggerFactory.getLogger(AbstractReadOnlyResource.class.getName());
    protected final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    protected final static API api = INJECTOR.getInstance(API.class);
    protected String authContext = "*";

    /**
     *
     * @return
     */
    @OPTIONS
    @Override
    public Response options() {
        return Response.ok().build();
    }

    /**
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a list of items", position = 10)
    @Override
    public Response list() {
        if (api.getAuth().isPermitted(authContext + ":read")) {
            return Response.ok(prepareList()).build();
        }
        throw new ForbiddenException("user: " + api.getAuth().getSubject().getPrincipal() + " cannot read any" + authContext);
    }

    /**
     * @param UUID
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a single item", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Item not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "ID of item to fetch", required = true)
            @PathParam("id") String UUID) {
        if (api.getAuth().isPermitted(authContext + ":read:" + UUID)) {
            T item = prepareSingle(UUID);
            if (item != null) {
                return Response.ok(item).build();
            }
            throw new ItemNotFoundException("Cannot find item: " + UUID);
        }
        throw new ForbiddenException("User " + api.getAuth().getSubject().getPrincipal() + " cannot read " + authContext + " " + UUID);
    }

    /**
     *
     * @return
     */
    abstract protected List<T> prepareList();

    /**
     *
     * @param uuid
     * @return
     */
    abstract protected T prepareSingle(String uuid);

}
