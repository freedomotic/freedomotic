/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.marketplace.IMarketPlace;
import com.freedomotic.marketplace.MarketPlaceService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 *
 * @author matteo
 */
@Path("marketplace")
@Singleton
@Api(value = "marketplace", description = "Marketplace providers, categories and plugins", position = 100)
public class MarketplaceResource {

    @Path("/providers")
//    @ApiOperation("Manage marketplace providers")
    public MarketplaceProvidersResource providers() {
        return new MarketplaceProvidersResource();
    }

    @Path("/categories")
//    @ApiOperation("Manage plugin installation from marketplace(s)")
    public MarketplaceCategoryResource categories() {
        return new MarketplaceCategoryResource();
    }

    @Path("/plugins")
//    @ApiOperation("Manage plugin installation from marketplace(s)")
    public MarketplacePluginsResource plugins() {
        return new MarketplacePluginsResource();
    }

    @POST
    @Path("/update")
    @ApiOperation(value = "Update providers' data - reload data from all of them")
    public Response update() {
        for (IMarketPlace provider : MarketPlaceService.getInstance().getProviders()) {
            provider.updateCategoryList();
            provider.updateAllPackageList();
        }
        return Response.accepted().build();
    }
}
