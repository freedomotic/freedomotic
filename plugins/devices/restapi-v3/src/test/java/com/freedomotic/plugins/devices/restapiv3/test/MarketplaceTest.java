/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.marketplace.IMarketPlace;
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
import org.junit.Test;

/**
 *
 * @author MazzoniM
 */
public class MarketplaceTest extends JerseyTest{
    
    MediaType representation = MediaType.APPLICATION_JSON_TYPE;
    String PATH = UriBuilder.fromResource(MarketplaceResource.class).build().toString();
    GenericType<IMarketPlace> marketSingleType = new GenericType<IMarketPlace>(){};
    GenericType<ArrayList<IMarketPlace>> marketListType = new GenericType<ArrayList<IMarketPlace>>(){};
    
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
    
    @Test
    public void providersList() {
    ArrayList<IMarketPlace> ml = target(PATH).path("providers").request(representation).get(marketListType);
       // assertEquals("List size", 1, ml.size());    
    }
}
