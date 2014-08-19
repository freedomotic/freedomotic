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
package com.freedomotic.plugins.devices.restapiv3.utils;

import com.freedomotic.api.API;
import com.freedomotic.api.Plugin;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 * @param <T>
 */
public abstract class AbstractResource<T> implements ResourceInterface<T> {

    public static final Logger LOG = Logger.getLogger(AbstractResource.class.getName());
    protected static API api = Freedomotic.INJECTOR.getInstance(API.class);
    /**
     *
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get a list of items", position = 10)
    @Override
    public Response list() {
        return Response.ok(prepareList()).build();
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
        T item = prepareSingle(UUID);
        if (item != null) {
            return Response.ok(item).build();
        }
        throw new ItemNotFoundException();
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
        @ApiResponse(code = 304, message = "Item not modified")
    })
    @ApiOperation(value = "Updates a item", position = 40)
    public Response update(
            @ApiParam(value = "ID of item to update", required = true)
            @PathParam("id") String UUID,
            T s) {
        try {
            LOG.info("Aquiring modified element");
            T z = doUpdate(s);
            if (z != null) {
                LOG.info("Everything was corerctly computed ");
                return Response.ok().build();
            } else {
                LOG.info("There was a error, so nothing's changed");
                return Response.notModified().build();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE,"Cannot update a item",e);
            return Response.notModified().build();
        }
    }

    @OPTIONS
    @Override
    public Response options() {
        return Response.ok().build();
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
    @ApiOperation(value = "Creates a new item", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New item created")
    })
    @Override
    public Response create(T s) throws URISyntaxException {
        try {
            return Response.created(doCreate(s)).build();
        } catch (URISyntaxException e) {
            LOG.log(Level.SEVERE, null, e);
            return Response.serverError().build();
        }
    }

    /**
     *
     * @param UUID
     * @return
     */
    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Deletes a item", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Item not found")
    })
    public Response delete(
            @ApiParam(value = "ID of item to delete", required = true)
            @PathParam("id") String UUID) {
        if (doDelete(UUID)) {
            return Response.ok().build();
        } else {
            throw new ItemNotFoundException();
        }
    }

    @Override
    @POST
    @Path("/{id}/copy")
    @ApiOperation(value = "Copies a item", position = 35)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Source item not found")
    })
    public Response copy(
            @ApiParam(value = "ID of item to copy", required = true)
            @PathParam("id") String UUID) {
        try {
            URI ref = doCopy(UUID);
            if (ref != null) {
                return Response.created(ref).build();
            } else {
                return Response.serverError().build();
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    protected URI createUri(String resId) {
        return UriBuilder.fromResource(this.getClass()).path(resId).build();
    }

    /**
     *
     * @param UUID
     * @return
     */
    abstract protected URI doCopy(String UUID);

    /**
     *
     * @param o
     * @return
     * @throws URISyntaxException
     */
    abstract protected URI doCreate(T o) throws URISyntaxException;

    /**
     *
     * @param UUID
     * @return
     */
    abstract protected boolean doDelete(String UUID);

    /**
     *
     * @param o
     * @return
     */
    abstract protected T doUpdate(T o);

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
