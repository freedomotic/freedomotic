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

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.marketplace.IMarketPlace;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import static com.freedomotic.plugins.devices.restapiv3.resources.jersey.MarketplaceResource.api;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractReadOnlyResource;
import com.freedomotic.settings.Info;
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
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
@Api(value = "marketplaceProviders", description = "Manage marketplace providers")
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
    public Response list() {
        return super.list();
    }

    @Override
    protected List<IMarketPlace> prepareList() {
        return mps.getProviders();
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
            @PathParam("id") String uuid) {
        return super.get(uuid);
    }

    @Override
    protected IMarketPlace prepareSingle(String uuid) {
        Integer id;
        try {
            id = Integer.parseInt(uuid);
        } catch (Exception e) {
            return null;
        }
        check(id);
        return mps.getProviders().get(id);
    }

    @Path("/{id}/categories")
    public MarketplaceCategoryResource listCategories(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id) {
        check(id);
        return new MarketplaceCategoryResource((ArrayList<IPluginCategory>) mps.getProviders().get(id).getAvailableCategories());
    }

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

    @POST
    @Path("/{id}/upgrade")
    @ApiOperation(value = "Upgrade plugins with most recent version available on a marketplace")
    public Response upgrade(
            @ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id) {
        for (Client c : api.getClients("plugin")) {
            Plugin p = (Plugin) c;
            for (IPluginPackage pp : mps.getProviders().get(id).getAvailablePackages()) {
                String freedomoticVersion = Info.getMajor() + "." + Info.getMinor();
                if (pp.getFilePath(freedomoticVersion) != null
                        && !pp.getFilePath(freedomoticVersion).isEmpty()
                        && pp.getTitle() != null) {
                    String packageVersion = MarketplaceResource.extractVersion(new File(pp.getFilePath(freedomoticVersion)).getName());
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

    @POST
    @Path("/{id}/plugins/install/{nid}")
    @ApiOperation(value = "Download and install a plugin, given its node id")
    @ApiResponses(value = {
        @ApiResponse(code = 202, message = "Plugin installation succeded")
    })
    @Produces(MediaType.APPLICATION_JSON)
    public Response installPlugin(@ApiParam(value = "Index of marketplace provider", required = true)
            @PathParam("id") int id,
            @ApiParam(value = "Node id of plugin to install - this is a id relative to selected provider", required = true)
            @PathParam("nid") String nid) {
        boolean done = false;
        String freedomoticVersion = Info.getMajor() + "." + Info.getMinor();
        String pluginPath = "";
        PluginsManager plugMgr = api.getPluginManager();
        for (IPluginPackage pp : mps.getPackageList()) {
            if (pp.getURI().endsWith(nid)) {
                pluginPath = pp.getFilePath(freedomoticVersion);
                break;
            }
        }
        if (pluginPath != null && !pluginPath.isEmpty()) {
            try {
                done = plugMgr.installBoundle(new URL(pluginPath));
            } catch (MalformedURLException ex) {
                done = false;
            }
        }
        if (done) {
            return Response.accepted().build();
        }
        return Response.serverError().entity(pluginPath + "\n" + URLDecoder.decode(pluginPath)).build();
    }
}
