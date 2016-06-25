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

import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractReadOnlyResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Api(value = "marketplaceCategory", description = "Manage marketplace plugin categories")
public class MarketplaceCategoryResource extends AbstractReadOnlyResource<IPluginCategory> {

    MarketPlaceService mps = MarketPlaceService.getInstance();
    ArrayList<IPluginCategory> catList;

    public MarketplaceCategoryResource() {
        this.catList = mps.getCategoryList();
    }

    public MarketplaceCategoryResource(ArrayList<IPluginCategory> clist) {
        this.catList = clist;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Show the list of registered remote marketplace providers")
    @Override
    public Response list() {
        return super.list();
    }

    @Override
    protected List<IPluginCategory> prepareList() {
        return catList;
    }

    @Path("/{cat}/plugins")
    @ApiOperation(value = "Show the list of plugins belonging to selected category")
    public MarketplacePluginsResource listPluginsFromCategory(
            @ApiParam(value = "Name of plugins category to fetch", required = true)
            @PathParam("cat") String cat,
            @ApiParam(value = "Retrieve package list automatically, if necessary", required = false)
            @QueryParam("noUpdate") boolean noUpdate) {
        for (IPluginCategory category : catList) {
            if (category.getName().equalsIgnoreCase(cat)) {
                if (mps.getPackageList(category).isEmpty() && !noUpdate) {
                    category.retrievePluginsInfo();
                }
                return new MarketplacePluginsResource(mps.getPackageList(category));
            }
        }
        throw new ItemNotFoundException("Cannot find desired category");
    }

    @GET
    @ApiOperation(value = "Get a category basic data")
    @Override
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{cat}")
    public Response get(
            @ApiParam(value = "Name of plugins category to fetch", required = true)
            @PathParam("cat") String uuid) {
        return super.get(uuid);
    }

    @Override
    public IPluginCategory prepareSingle(String cat) {
        for (IPluginCategory category : catList) {
            if (category.getName().equalsIgnoreCase(cat)) {
                return category;
            }
        }
        return null;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}/update")
    public Response update(
            @ApiParam(value = "Name of plugins category to fetch", required = true)
            @PathParam("cat") String cat) {
        IPluginCategory c = prepareSingle(cat);
        c.retrievePluginsInfo();
        return Response.accepted().build();
    }

}
