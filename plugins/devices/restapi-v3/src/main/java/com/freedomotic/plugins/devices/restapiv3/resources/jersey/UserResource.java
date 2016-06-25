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

import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.representations.UserRepresentation;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.security.User;
import com.freedomotic.security.UserRealm;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("users")
@Api(value = "users", description = "Manage users", position = 300)
public class UserResource extends AbstractResource<UserRepresentation> {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "List all users", position = 10)
    @Override
    public Response list() {
        return super.list();
    }

    @Override
    protected URI doCopy(String UUID) {
        User u = api.getAuth().getUser(UUID);
        UserRepresentation ur = new UserRepresentation(u);
        ur.setName("copyOf" + ur.getName());
        ur.setPassword("");
        try {
            return doCreate(ur);
        } catch (Exception e) {
        }
        return null;
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
    @ApiOperation(value = "Add a new user", position = 30)
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "New user added")
    })
    @Override
    public Response create(UserRepresentation s) throws URISyntaxException {
        return super.create(s);
    }

    @Override
    protected URI doCreate(UserRepresentation o) throws URISyntaxException {
        User u = new User(o.getName(), o.getPassword(), api.getAuth());
        u.setRoles(o.getRoles());
        for (Object key : o.getProperties().keySet()) {
            u.setProperty(key.toString(), o.getProperties().getProperty(key.toString()));
        }
        UserRealm ur = (UserRealm) api.getAuth().getUserRealm();
        ur.addUser(u);
        if (api.getAuth().getUser(o.getName()) != null) {
            return createUri(o.getName());
        }
        return null;
    }

    @Override
    @DELETE
    @Path("/{id}")
    @ApiOperation(value = "Delete an user", position = 50)
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "User not found")
    })
    public Response delete(
            @ApiParam(value = "User to delete (e.g. admin, guest)", required = true)
            @PathParam("id") String UUID) {
        return super.delete(UUID);
    }

    @Override
    protected boolean doDelete(String UUID) {
        if (!api.getAuth().getCurrentUser().getName().equals(UUID)) {
            return api.getAuth().deleteUser(UUID);
        } else {
            throw new ForbiddenException("Users cannot delete themselves!!");
        }
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
        @ApiResponse(code = 304, message = "User not modified")
    })
    @ApiOperation(value = "Update an user", position = 40)
    public Response update(
            @ApiParam(value = "User to update (e.g. admin, guest)", required = true)
            @PathParam("id") String UUID, UserRepresentation s) {
        return super.update(UUID, s);
    }

    @Override
    protected UserRepresentation doUpdate(String uuid, UserRepresentation o) {
        o.setName(uuid);
        try {
            User u = api.getAuth().getUser(uuid);
            u.setRoles(o.getRoles());
            u.getProperties().clear();
            if (o.getPassword() != null && !o.getPassword().isEmpty()) {
                u.setPassword(o.getPassword());
            }
            u.getProperties().clear();
            for (Object key : o.getProperties().keySet()) {
                u.setProperty(key.toString(), o.getProperties().getProperty(key.toString()));
            }
            return new UserRepresentation(u);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected List<UserRepresentation> prepareList() {
        ArrayList<UserRepresentation> ul = new ArrayList<UserRepresentation>();
        for (User u : api.getAuth().getUsers().values()) {
            ul.add(new UserRepresentation(u));
        }
        return ul;
    }

    /**
     * @param UUID
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get an user", position = 20)
    @Path("/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "User not found")
    })
    @Override
    public Response get(
            @ApiParam(value = "User to fetch (e.g. admin, guest)", required = true)
            @PathParam("id") String UUID) {
        return super.get(UUID);
    }

    @Override
    protected UserRepresentation prepareSingle(String uuid) {
        User u = api.getAuth().getUser(uuid);
        return (u == null) ? null : new UserRepresentation(u);
    }

    @GET
    @Path("/_")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get current user", position = 0)
    public Response getCurrentUser() {
        return Response.seeOther(createUri(api.getAuth().getCurrentUser().getName())).build();
    }

    @POST
    @Path("/_/logout")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Get current user", position = 0)
    public Response logout() {
        api.getAuth().logout();
        return Response.accepted().build();
    }

    @POST
    @Path("/_/login")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @ApiOperation(value = "Login a user", position = 0)
    public Response login(
            @FormParam("name") String name,
            @FormParam("password") String password,
            @FormParam("rememberMe") boolean rememberMe) {
        if (api.getAuth().login(name, password, rememberMe)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

    }

    @Path("/{id}/properties")
    public PropertyResource props(
            @ApiParam(value = "User to fetch properties from", required = true)
            @PathParam("id") String userName) {
        return new PropertyResource(userName);
    }

    @Path("/{id}/roles")
    public UserRoleResource roles(
            @ApiParam(value = "User to fetch properties from", required = true)
            @PathParam("id") String userName) {
        return new UserRoleResource(userName);
    }

    @Path("/{id}/ispermitted/{action}")
    @GET
    @ApiOperation(value = "Check user's permissions")
    public Response isPermitted(
            @ApiParam(value = "User to check permission from", required = true)
            @PathParam("id") String userName,
            @ApiParam(value = "Action to check user's permission against", required = true)
            @PathParam("action") String action) {
        if (api.getAuth().getUser(userName).isPermitted(action)) {
            return Response.ok().build();
        } else {
            return Response.status(Response.Status.FORBIDDEN).build();
        }
    }

    @Api(value = "userRoles", description = "Manage user's roles", position = 301)
    public class UserRoleResource {

        String userName;
        User user;

        public UserRoleResource(String userName) {
            this.userName = userName;
            this.user = api.getAuth().getUser(userName);
        }

        @GET
        @ApiOperation(value = "List roles", position = 10)
        public Response list() {
            return Response.ok(user.getRoles()).build();
        }

        @POST
        @ApiOperation(value = "Add a role", position = 20)
        public Response add(
                @ApiParam(value = "Role to add", required = true)
                @PathParam("name") String roleName) {
            user.addRole(roleName);
            return Response.accepted().build();
        }

        @DELETE
        @Path("/{name}")
        @ApiOperation(value = "Delete a role", position = 30)
        @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Role not found")
        })
        public Response delete(
                @ApiParam(value = "Role to delete", required = true)
                @PathParam("name") String name) {
            user.getRoles().remove(name);
            return Response.accepted().build();
        }
    }

    @Api(value = "userProperties", description = "Manage user's properties", position = 302)
    public class PropertyResource {

        String userName;
        User user;

        public PropertyResource(String userName) {
            this.userName = userName;
            this.user = api.getAuth().getUser(userName);
        }

        @GET
        @ApiOperation(value = "Get every property", position = 10)
        public Response list() {
            return Response.ok(user.getProperties()).build();
        }

        @GET
        @Path("/{key}")
        @ApiOperation(value = "Get a single property", position = 20)
        public Response get(
                @ApiParam(value = "Key to retrieve", required = true)
                @PathParam("key") String key) {
            return Response.ok(user.getProperty(key)).build();
        }

        @PUT
        @ApiOperation(value = "Modify the whole properties with a new set", position = 30)
        public Response updateAll(Properties p) {
            this.user.getProperties().clear();
            for (String line : p.stringPropertyNames()) {
                user.setProperty(line, p.getProperty(line));
            }
            return Response.accepted(user.getProperties()).build();
        }

        @PUT
        @Path("/{key}/{value}")
        @ApiOperation(value = "Modify a single property", position = 40)
        @ApiResponses(value = {
            @ApiResponse(code = 404, message = "Trying to modify a non-existent property"),
            @ApiResponse(code = 200, message = "Property modified")
        })
        public Response updateSingle(
                @ApiParam(value = "Key to retrieve", required = true)
                @PathParam("key") String key,
                @ApiParam(value = "Value to assign", required = true)
                @PathParam("value") String value) {
            if (user.getProperty(key) != null) {
                user.setProperty(key, value);
                return Response.ok(user.getProperty(key)).build();
            }
            return Response.status(Response.Status.NOT_FOUND).build();

        }

        @POST
        @Path("/{key}/{value}")
        @ApiOperation(value = "Add a property", position = 50)
        @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Trying of adding an already exsisting property - use PUT instead")
        })
        public Response createSingle(
                @ApiParam(value = "Key to retrieve", required = true)
                @PathParam("key") String key,
                @ApiParam(value = "Value to assign", required = true)
                @PathParam("value") String value) {
            if (user.getProperty(key) == null) {
                user.setProperty(key, value);
                return Response.created(null).build();
            }
            return Response.notAcceptable(null).build();
        }
    }
}
