/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.restapiv3;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.plugins.devices.restapiv3.resources.atmosphere.AtmosphereObjectChangeResource;
import com.freedomotic.plugins.devices.restapiv3.resources.atmosphere.AtmospherePluginChangeResource;
import com.freedomotic.plugins.devices.restapiv3.resources.atmosphere.AtmosphereZoneChangeResource;
import com.freedomotic.reactions.Command;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.ws.rs.core.UriBuilder;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;

public class RestAPIv3 extends Protocol {

    private static final Logger LOG = Logger.getLogger(RestAPIv3.class.getName());
    private static final String RESOURCE_PKG = "com.freedomotic.plugins.devices.restapiv3.resources";
    public static final String JERSEY_RESOURCE_PKG = RESOURCE_PKG + ".jersey";
    public static final String ATMOSPHRE_RESOURCE_PKG = RESOURCE_PKG + ".atmosphere";

    public static URI BASE_URI;
    public static final String API_VERSION = "v3";

    @Inject
    private RestJettyServer jServer;
    @Inject
    private AtmosphereObjectChangeResource atmosphereObjectChangeResource;
    @Inject
    private AtmosphereZoneChangeResource atmosphereZoneChangeResource;
    @Inject
    private AtmospherePluginChangeResource atmospherePluginChangeResource;
    
    // Hold a preconfigurd static web security manager which can be used by Shiro
    public static DefaultWebSecurityManager defaultWebSecurityManager;

    public RestAPIv3() {
        super("RestAPI-v3", "/restapi-v3/restapiv3-manifest.xml");
        setPollingWait(-1);
        // Create the preconfigured security manager
        createDefaultWebSecurityManager();
    }

    @Override
    protected void onRun() {

    }

    @Override
    protected void onStart() {
        setDescription("Plugin is starting...");
        String protocol = configuration.getBooleanProperty("enable-ssl", false) ? "https" : "http";
        int port = configuration.getBooleanProperty("enable-ssl", false) ? configuration.getIntProperty("https-port", 9113) : configuration.getIntProperty("http-port", 9111);

        BASE_URI = UriBuilder.fromUri(protocol + "://" + configuration.getStringProperty("listen-address", "localhost") + "/").path(API_VERSION).port(port).build();

        LOG.log(Level.INFO, "RestAPI v3 plugin is started at {0}", BASE_URI);

        try {
            jServer.setMaster(this);
            jServer.startServer();
            setDescription("API is available at " + BASE_URI.toString().substring(0, BASE_URI.toString().length() - 2));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

        addEventListener("app.event.sensor.object.behavior.change");
        addEventListener("app.event.sensor.environment.zone.change");
        addEventListener("app.event.sensor.plugin.change");

    }

    @Override
    protected void onStop() {
        LOG.info("RestAPI v3 plugin is stopped ");
        setDescription("Plugin stopped");
        try {
            jServer.stopServer();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onCommand(Command c) {
        LOG.log(Level.INFO, "RestAPI v3 plugin receives a command called {0} with parameters {1}", new Object[]{c.getName(), c.getProperties().toString()});

    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        if (event instanceof ObjectHasChangedBehavior) {
            atmosphereObjectChangeResource.broadcast(event);
        } else if (event instanceof ZoneHasChanged) {
            atmosphereZoneChangeResource.broadcast(event);
        } else if (event instanceof PluginHasChanged) {
            atmospherePluginChangeResource.broadcast(event);
        }
    }

    public final void createDefaultWebSecurityManager() {
        defaultWebSecurityManager = new DefaultWebSecurityManager(this.getApi().getAuth().getUserRealm());
    }

}
