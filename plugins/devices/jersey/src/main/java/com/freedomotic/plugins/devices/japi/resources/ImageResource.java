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

import com.freedomotic.core.ResourcesManager;
import com.freedomotic.plugins.devices.japi.utils.ResourceInterface;
import com.freedomotic.util.Info;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 */
@Path("resources")
@Api(value = "/resources", description = "Retrieves images and other binary resource", position = 10)
public class ImageResource implements ResourceInterface<File> {
 
    protected File prepareSingle(String fileName) {
        return ResourcesManager.getFile(Info.PATH_RESOURCES_FOLDER, fileName);
        
    }

    @Override
    @GET
    @Path("/{id}")
     @ApiOperation("Get an image or a redirect to it")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Image not found")
    })
    @Produces(MediaType.WILDCARD)
    public Response get(
            @ApiParam(value = "name of image file to fetch", required = true)
            @PathParam("id") String fileName) {
         File imageFile = prepareSingle(fileName);
         String path = null;
         if (imageFile.getPath().startsWith(Info.PATH_RESOURCES_FOLDER.getPath())) {
            path = imageFile.getPath().substring((Info.PATH_RESOURCES_FOLDER.getPath()).length());
            path = path.replace('\\', '/');
            System.out.println("RESTAPI path: " + path);
        }
        if (path != null) {
        URI newPath = UriBuilder.fromPath("/res").path(path).build();
        return Response.seeOther(newPath).build();
        }
        else {
            return Response.ok(imageFile).build();
        }
    }
    

    @Override
    public Response list() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response delete(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    

    @Override
    public Response create(File s) throws URISyntaxException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response update(String UUID, File s) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response copy(String UUID) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Response options() {
        return Response.ok().build();
    }
 
   
}
