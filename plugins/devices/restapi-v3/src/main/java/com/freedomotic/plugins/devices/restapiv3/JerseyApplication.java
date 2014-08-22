/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.freedomotic.plugins.devices.restapiv3;

import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.JERSEY_RESOURCE_PKG;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author matteo
 */
public class JerseyApplication extends ResourceConfig {
    
    public JerseyApplication(){
        packages(JERSEY_RESOURCE_PKG);
        // register(DeclarativeLinkingFeature.class);
    }
}
