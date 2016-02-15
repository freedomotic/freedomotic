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
package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.marketplace.IMarketPlace;
import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.MarketplaceResource;
import com.freedomotic.plugins.devices.restapiv3.utils.ThrowableExceptionMapper;
import java.util.ArrayList;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author matteo
 */
public class MarketplaceTest extends JerseyTest{
    
    MediaType representation = MediaType.APPLICATION_JSON_TYPE;
    String PATH = UriBuilder.fromResource(MarketplaceResource.class).build().toString();
    
    @Override
    protected Application configure() {
    enable(TestProperties.LOG_TRAFFIC);
    enable(TestProperties.DUMP_ENTITY);
    ResourceConfig rc = new ResourceConfig().register(MarketplaceResource.class);
        rc.registerClasses(JacksonFeature.class);
      //  rc.registerClasses(MoxyXmlFeature.class);
        rc.register(ThrowableExceptionMapper.class);
        return rc;
    }
    
    GenericType<IMarketPlace> marketSingleType = new GenericType<IMarketPlace>(){};
    GenericType<ArrayList<IMarketPlace>> marketListType = new GenericType<ArrayList<IMarketPlace>>(){};
    
    @Test
    public void providersList() {
    ArrayList<IMarketPlace> ml = target(PATH).path("providers").request(representation).get(marketListType);
    // assertEquals("List size", 1, ml.size());    
    }
    
    GenericType<IPluginCategory> categorySingleType = new GenericType<IPluginCategory>(){};
    GenericType<ArrayList<IPluginCategory>> categoryListType = new GenericType<ArrayList<IPluginCategory>>(){};
    
    @Test
    public void categoriesTest(){
    ArrayList<IPluginCategory> pc = target(PATH).path("categories").request(representation).get(categoryListType);
    }
    
    GenericType<IPluginPackage> pluginSingleType = new GenericType<IPluginPackage>(){};
    GenericType<ArrayList<IPluginPackage>> pluginListType = new GenericType<ArrayList<IPluginPackage>>(){};
    
    @Test
    public void pluginsTest(){
        ArrayList<IPluginPackage> pp = target(PATH).path("plugins").request(representation).get(pluginListType);
    }
    
}
