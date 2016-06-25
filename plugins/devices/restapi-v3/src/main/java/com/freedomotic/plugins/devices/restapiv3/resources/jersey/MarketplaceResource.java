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

import com.freedomotic.api.API;
import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.marketplace.IMarketPlace;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.settings.Info;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import javax.inject.Singleton;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("marketplace")
@Singleton
@Api(value = "marketplace", description = "Marketplace providers, categories and plugins", position = 100)
public class MarketplaceResource {

    protected final static Injector INJECTOR = Guice.createInjector(new FreedomoticInjector());
    protected final static API api = INJECTOR.getInstance(API.class);

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
        ArrayList<IPluginPackage> packageList = new ArrayList<>();
        for (IMarketPlace provider : MarketPlaceService.getInstance().getProviders()) {
            if (provider.getAvailablePackages().isEmpty()) {
                provider.updateAllPackageList();
            }
            packageList.addAll(provider.getAvailablePackages());
        }

        return new MarketplacePluginsResource(packageList);
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

    @POST
    @Path("/plugins/install/{url}")
    @ApiOperation(value = "Download and install a plugin")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Plugin installation succeded")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response installPlugin(
            @ApiParam(value = "URL of plugin to download and install - this have to be url-encoded", required = true)
            @PathParam("url") String pluginPath) {
        boolean done;
        PluginsManager plugMgr = api.getPluginManager();
        try {
            done = plugMgr.installBoundle(new URL(URLDecoder.decode(pluginPath)));
        } catch (MalformedURLException ex) {
            done = false;
        }
        if (done) {
            return Response.accepted().build();
        }
        return Response.serverError().entity(pluginPath + "\n" + URLDecoder.decode(pluginPath)).build();
    }

    public static String extractVersion(String filename) {
        //suppose filename is something like it.nicoletti.test-5.2.x-1.212.device
        //only 5.2.x-1.212 is needed
        //remove extension
        filename
                = filename.substring(0,
                        filename.lastIndexOf("."));

        String[] tokens = filename.split("-");

        //3 tokens expected
        if (tokens.length == 3) {
            return tokens[1] + "-" + tokens[2];
        } else {
            return filename;
        }
    }

    @POST
    @Path("/upgrade")
    @ApiOperation(value = "Upgrade plugins with most recent version available on marketplaces")
    @Produces(MediaType.APPLICATION_JSON)
    public Response upgrade() {
        MarketPlaceService mps = MarketPlaceService.getInstance();
        for (Client c : api.getClients("plugin")) {
            Plugin p = (Plugin) c;
            for (IPluginPackage pp : mps.getPackageList()) {
                String freedomoticVersion = Info.getMajor() + "." + Info.getMinor();
                if (pp.getFilePath(freedomoticVersion) != null
                        && !pp.getFilePath(freedomoticVersion).isEmpty()
                        && pp.getTitle() != null) {
                    String packageVersion = extractVersion(new File(pp.getFilePath(freedomoticVersion)).getName());
                    int result = api.getClientStorage().compareVersions(pp.getTitle(), packageVersion);
                    switch (result) {
                        case -1:  //older version or not yet installed -> INSTALL
                            break;
                        case 1:  //newer version -> UPGRADE
                            try {
                                api.getPluginManager().installBoundle(new URL(pp.getFilePath(freedomoticVersion)));
                            } catch (MalformedURLException e) {
                            }
                            break;
                    }
                }
            }
        }
        return Response.accepted().build();
    }
}
