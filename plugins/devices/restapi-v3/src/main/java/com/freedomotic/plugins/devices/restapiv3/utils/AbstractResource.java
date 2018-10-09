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

import com.freedomotic.plugins.devices.restapiv3.filters.ForbiddenException;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 * @author Matteo Mazzoni
 * @param <T>
 */
public abstract class AbstractResource<T> extends AbstractReadOnlyResource<T> implements ResourceInterface<T> {

    /**
     * Update an item by ID
     *
     * @param uuid id of item to update
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
    @ApiOperation(value = "Update an item", position = 40)
    public Response update(
            @ApiParam(value = "ID of item to update", required = true)
            @PathParam("id") String uuid,
            T s) {
        if (API.getAuth().isPermitted(authContext + ":update:" + uuid)) {
            try {
                LOG.info("Acquiring modified element");
                T z = doUpdate(uuid, s);
                if (z != null) {
                    LOG.info("Everything was correctly computed");
                    return Response.ok().build();
                }
                LOG.info("There was a error, so nothing's changed");
                return Response.notModified().build();
            } catch (Exception e) {
                LOG.error("Cannot update an item", e);
                return Response.notModified().build();
            }
        }
        throw new ForbiddenException(USER_LITERAL + API.getAuth().getSubject().getPrincipal() + " cannot modify " + authContext + " " + uuid);
    }

    /**
     * Create a new item
     *
     * @param s
     * @return
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new item", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New item created")
    })
    @Override
    public Response create(T s) {
        if (API.getAuth().isPermitted(authContext + ":create")) {
            try {
                return Response.created(doCreate(s)).build();
            } catch (URISyntaxException e) {
                LOG.error(e.getMessage());
                return Response.serverError().build();
            }
        }
        throw new ForbiddenException(USER_LITERAL + API.getAuth().getSubject().getPrincipal() + " cannot create any " + authContext);
    }

    /**
     * Delete an item by id
     *
     * @param uuid id of item to delete
     * @return
     */
    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete an item", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Item not found")
    })
    public Response delete(
            @ApiParam(value = "ID of item to delete", required = true)
            @PathParam("id") String uuid) {
        if (API.getAuth().isPermitted(authContext + ":create") && API.getAuth().isPermitted(authContext + ":read:" + uuid)) {
            if (doDelete(uuid)) {
                return Response.ok().build();
            }
            throw new ItemNotFoundException("Cannot find item: " + uuid);
        }
        throw new ForbiddenException(USER_LITERAL + API.getAuth().getSubject().getPrincipal() + " cannot copy " + authContext + " with UUID " + uuid);
    }

    /*
     * Copy an item by id
     *
     * @param uuid id of item to copy
     */
    @Override
    @POST
    @Path("/{id}/copy")
    @ApiOperation(value = "Copy an item", position = 35)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Source item not found")
    })
    public Response copy(
            @ApiParam(value = "ID of item to copy", required = true)
            @PathParam("id") String uuid) {
        URI ref = doCopy(uuid);
        if (ref != null) {
            return Response.created(ref).build();
        }
        throw new ItemNotFoundException("Cannot find item: " + uuid);

    }

    protected URI createUri(String resId) {
        return UriBuilder.fromResource(this.getClass()).path(resId).build();
    }

    /**
     *
     * @param uuid
     * @return
     */
    abstract protected URI doCopy(String uuid);

    /**
     *
     * @param o
     * @return
     * @throws URISyntaxException
     */
    protected abstract URI doCreate(T o) throws URISyntaxException;

    /**
     *
     * @param uuid
     * @return
     */
    protected abstract boolean doDelete(String uuid);

    /**
     *
     * @param o
     * @return
     */
    protected abstract T doUpdate(String uuid, T o);
}
