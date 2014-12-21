/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.filters;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.atmosphere.cpr.AtmosphereResourceFactory;
import org.atmosphere.cpr.BroadcasterFactory;
/*import org.atmosphere.guice.AtmosphereGuiceServlet; */

/**
 *
 * @author matteo
 */
public class ApiModule extends AbstractModule {

    @Override
    protected void configure(){
        
    }
/*
    @Provides
    BroadcasterFactory provideBroadcasterFactory(AtmosphereGuiceServlet atmosphereGuiceServlet) {
        return atmosphereGuiceServlet.framework().getBroadcasterFactory();
    }

    @Provides
    AtmosphereResourceFactory provideAtmosphereResourceFactory(AtmosphereGuiceServlet atmosphereGuiceServlet) {
        return atmosphereGuiceServlet.framework().atmosphereFactory();
    }
*/
}
