/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.marketplace.MarketPlaceService;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractReadOnlyResource;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import com.wordnik.swagger.annotations.ApiResponse;
import com.wordnik.swagger.annotations.ApiResponses;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
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
@Api(value="marketPlacePlugins", description="Manage marketplace plugin packages")
public class MarketplacePluginsResource extends AbstractReadOnlyResource<IPluginPackage> {

    MarketPlaceService mps = MarketPlaceService.getInstance();
    ArrayList<IPluginCategory> catList = mps.getCategoryList();
    ArrayList<IPluginPackage> plugList;
    
    public MarketplacePluginsResource() {
        this.plugList = mps.getPackageList();
    }
    
    public MarketplacePluginsResource(ArrayList<IPluginPackage> plist){
        this.plugList = plist;
    }
    
    /**
     *
     * @return
     */
    @GET
    @ApiOperation(value = "Show the list of registered remote marketplace providers")
    @Override
    public Response list(){
        return super.list();
    }
    
    @Override
    protected List<IPluginPackage> prepareList() {
        return plugList;
    }

    @GET
    @ApiOperation(value = "Get a single plugin package metadata")
    @Override
    @Path("/{id}")
    public Response get( @ApiParam(value = "Name of plugin to fetch", required = true)
            @PathParam("id") String uuid) {
        return super.get(uuid);
    }

    @Override
    protected IPluginPackage prepareSingle(String uuid) {
        for (IPluginPackage pp : plugList) {
            if (pp.getTitle().equalsIgnoreCase(uuid)) {
                return pp;
            }
        }
        return null;
    }

}
