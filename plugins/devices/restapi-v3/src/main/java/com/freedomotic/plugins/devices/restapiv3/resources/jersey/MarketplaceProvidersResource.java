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

import com.freedomotic.marketplace.IMarketPlace;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractReadOnlyResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Api(value="marketplaceProviders", description="Manage marketplace providers")
public class MarketplaceProvidersResource extends AbstractReadOnlyResource<IMarketPlace> {

    MarketPlaceService mps = MarketPlaceService.getInstance();
    ArrayList<IPluginCategory> catList = mps.getCategoryList();

    /**
     *
     * @return
     */
    @GET
    @ApiOperation(value = "Show the list of registered remote marketplace providers")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    public Response list(){
        return super.list();
    }
    
    @Override
    protected List<IMarketPlace> prepareList() {
        return mps.getProviders();
    }

    @POST
    @Path("/update")
    @ApiOperation(value = "Update providers' data - reload data from all of them")
    public Response update() {
        for (IMarketPlace provider : mps.getProviders()) {
            provider.updateCategoryList();
            provider.updateAllPackageList();
        }
        return Response.accepted().build();
    }

    private void check(int id) {
        if (id >= mps.getProviders().size()) {
            throw new ItemNotFoundException("Selected provider is not available");
        }
    }

    @GET
    @ApiOperation(value = "Show info about specific provider")
    @Produces(MediaType.APPLICATION_JSON)
    @Override
    @Path("/{id}")
    public Response get(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") String uuid){
        return super.get(uuid);
    }
    @Override
    protected IMarketPlace prepareSingle( String uuid) {
        Integer id;
        try {
            id = Integer.parseInt(uuid);
        } catch (Exception e) {
            return null;
        }
        check(id);
        return mps.getProviders().get(id);
    }

    @GET
    @Path("/{id}/categories")
    public MarketplaceCategoryResource listCategories(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id) {
        check(id);
        return new MarketplaceCategoryResource((ArrayList<IPluginCategory>) mps.getProviders().get(id).getAvailableCategories());
    }

/*    @GET
    @Path("/{id}/categories/{cat}/plugins")
    @ApiOperation(value = "Show a list of plugins from the specified category")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listPluginsFromCategoryAndProvider(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id,
            @ApiParam(value = "Name of plugins category to fetch", required = true)
            @PathParam("cat") String cat) {
        check(id);
        for (IPluginCategory category : catList) {
            if (category.getName().equalsIgnoreCase(cat)) {
                return Response.ok(mps.getProviders().get(id).getAvailablePackages(category)).build();
            }
        }
        throw new ItemNotFoundException();
    }
    
    */

    @GET
    @Path("/{id}/plugins")
    public MarketplacePluginsResource listPluginsFromProvider(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id) {
        check(id);
        return new MarketplacePluginsResource((ArrayList<IPluginPackage>) mps.getProviders().get(id).getAvailablePackages());
    }

    @POST
    @Path("/{id}/update")
    @ApiOperation(value = "Update provider's data - reload data from it")
    public Response update(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id) {
        check(id);
        for (IPluginCategory cat : mps.getProviders().get(id).getAvailableCategories()) {
            cat.retrievePluginsInfo();
        }
        return Response.accepted().build();
    }
}
