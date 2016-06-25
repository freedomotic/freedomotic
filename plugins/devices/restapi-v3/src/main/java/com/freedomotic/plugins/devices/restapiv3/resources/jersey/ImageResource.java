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

import com.freedomotic.core.ResourcesManager;
import com.freedomotic.settings.Info;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.io.File;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("resources")
@Singleton
@Api(value = "/resources", description = "Retrieve images and other binary resource", position = 10)
public class ImageResource {

    private final Map<String, File> cache = new HashMap<String, File>();

    protected File prepareSingle(String fileName) {
        File cached = cache.get(fileName);
        if (cached != null) {
            return cached;
        }
        cached = ResourcesManager.getFile(Info.PATHS.PATH_RESOURCES_FOLDER, fileName);
        if (cached != null) {
            cache.put(fileName, cached);
            return cached;
        } else {
            return null;
        }
    }

    @GET
    @Path("/{id}")
    @ApiOperation("Get an image or a redirect to it")
    @ApiResponses(value = {
        @ApiResponse(code = 404, message = "Image not found")
    })
    @Produces(MediaType.WILDCARD)
    public Response getWithFallback(
            @ApiParam(value = "Name of image file to fetch", required = true)
            @PathParam("id") String fileName,
            @ApiParam(value = "Name of alternate image file to fetch", required = false)
            @QueryParam("fbId") String fallbackFilename
    ) {
        File imageFile = prepareSingle(fileName);
        if (imageFile == null) {
            if (fallbackFilename != null && !fallbackFilename.isEmpty()) {
                imageFile = prepareSingle(fallbackFilename);
            }
        }
        if (imageFile == null) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String path = null;
        if (imageFile.getPath().startsWith(Info.PATHS.PATH_RESOURCES_FOLDER.getPath())) {
            path = imageFile.getPath().substring((Info.PATHS.PATH_RESOURCES_FOLDER.getPath()).length());
            path = path.replace('\\', '/');
            //System.out.println("RESTAPI path: " + path);
        }
        if (path != null) {
            URI newPath = UriBuilder.fromPath("/res").path(path).build();
            return Response.seeOther(newPath).build();
        } else {
            return Response.ok(imageFile).build();
        }
    }

    @OPTIONS
    public Response options() {
        return Response.ok().build();
    }

}
