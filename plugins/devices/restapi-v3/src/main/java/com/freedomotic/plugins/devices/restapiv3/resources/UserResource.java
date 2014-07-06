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
package com.freedomotic.plugins.devices.restapiv3.resources;

import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.security.User;
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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("users")
@Api(value = "users", description = "Manages users", position = 300)
public class UserResource extends AbstractResource<User> {

    @Override
    protected URI doCopy(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected URI doCreate(User o) throws URISyntaxException {
        api.getAuth().addUser(o.getName(), o.getCredentials().toString(), "guests");
        return createUri(o.getName());
    }

    @Override
    protected boolean doDelete(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected User doUpdate(User o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected List<User> prepareList() {
        ArrayList<User> ul = new ArrayList<User>();
        ul.addAll(api.getAuth().getUsers().values());
        return ul;
    }

    @Override
    protected User prepareSingle(String uuid) {
        return api.getAuth().getUser(uuid);
    }

    @GET
    @Path("/_")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    @ApiOperation(value = "Get current user", position = 0)
    public Response getCurrentUser() {
        return Response.seeOther(createUri(api.getAuth().getCurrentUser().getName())).build();
    }

    @Path("/{id}/properties")
    public PropertyResource props(
            @ApiParam(value = "User to fetch properties from", required = true)
            @PathParam("id") String userName) {
        return new PropertyResource(userName);
    }
    
    @Path("/{id}/roles")
    public RoleResource roles(
            @ApiParam(value = "User to fetch properties from", required = true)
            @PathParam("id") String userName) {
        return new RoleResource(userName);
    }
    
    @Api(value = "users/roles", description = "Manages user's roles", position = 301)
    public class RoleResource{
        String userName;
        User user;

        public RoleResource(String userName) {
            this.userName = userName;
            this.user = api.getAuth().getUser(userName);
        }
        
        @GET
        @ApiOperation(value = "List roles", position = 10)
        public Response list(){
            return Response.ok(user.getRoles()).build();
        }
        
        @POST
         @ApiOperation(value = "Add a role", position = 20)
        public Response add( 
                @ApiParam(value = "Role to add", required = true)
                @PathParam("name") String roleName){
            user.addRole(roleName);
            return Response.accepted().build();
        }
        
        @DELETE
        @Path("/{name}")
        @ApiOperation(value = "Delete a role", position = 30)
       public Response delete(
                @ApiParam(value = "Role to delete", required = true)
                @PathParam("name") String name){
           user.getRoles().remove(name);
            return Response.accepted().build();
        }
        

    }

    @Api(value = "users/properties", description = "Manages user's properties", position = 302)
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
                @ApiParam(value = "key to retrieve", required = true)
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
            @ApiResponse(code = 404, message = "Trying to moify a non-existent property"),
            @ApiResponse(code = 200, message = "Property modified")
        })
        public Response updateSingle(
                @ApiParam(value = "key to retrieve", required = true)
                @PathParam("key") String key,
                @ApiParam(value = "value to assign", required = true)
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
            @ApiResponse(code = 400, message = "Trying ad an already exsisting property - use PUT instead")
        })
        public Response createSingle(
                @ApiParam(value = "key to retrieve", required = true)
                @PathParam("key") String key,
                @ApiParam(value = "value to assign", required = true)
                @PathParam("value") String value) {
            if (user.getProperty(key) == null) {
                user.setProperty(key, value);
                return Response.created(null).build();
            }
            return Response.notAcceptable(null).build();
        }
    }
    
}
