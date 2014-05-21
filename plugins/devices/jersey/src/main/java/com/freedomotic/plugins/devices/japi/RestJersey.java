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
package com.freedomotic.plugins.devices.japi;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.plugins.devices.japi.filters.AuthenticationExceptionMapper;
import com.freedomotic.plugins.devices.japi.filters.CorsFilter;
import com.freedomotic.plugins.devices.japi.filters.SecurityFilter;
import com.freedomotic.plugins.devices.japi.resources.ObjectChangeResource;
import com.freedomotic.plugins.devices.japi.resources.PluginChangeResource;
import com.freedomotic.plugins.devices.japi.resources.ZoneChangeResource;
import com.freedomotic.plugins.devices.japi.utils.FDObjectMapper;
import com.freedomotic.reactions.Command;
import com.freedomotic.util.Info;
import com.wordnik.swagger.jaxrs.config.BeanConfig;
import java.io.File;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
//import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

public class RestJersey
        extends Protocol {

    private static final Logger LOG = Logger.getLogger(RestJersey.class.getName());
    public static final String RESOURCE_PKG = "com.freedomotic.plugins.devices.japi.resources";
    private static final String SWAGGER_PKG = "com.wordnik.swagger.jersey.listing";
    final int POLLING_WAIT;
    private HttpServer server;
    public static URI BASE_URI;
    private static URI SWAGGER_URI;
    private static final String API_VERSION = "v3";

    public RestJersey() {
        //every plugin needs a name and a manifest XML file
        super("RestJersey", "/jersey/jersey-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/com.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 2000);
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(-1); //millisecs interval between hardware device status reads

    }

    @Override
    protected void onRun() {

    }

    @Override
    protected void onStart() {

        String protocol = configuration.getBooleanProperty("enable-ssl", false) ? "https" : "http";
        String staticDir = configuration.getStringProperty("serve-static", "none");
        int port = configuration.getBooleanProperty("enable-ssl", false) ? configuration.getIntProperty("https-port", 9113) : configuration.getIntProperty("http-port", 9111);

        BASE_URI = UriBuilder.fromUri(protocol + "://" + configuration.getStringProperty("listen-address", "localhost") + "/").path(API_VERSION).port(port).build();
        SWAGGER_URI = UriBuilder.fromPath(API_VERSION).build();

        LOG.log(Level.INFO, "Jersey RestAPI plugin is started at {0}", BASE_URI);

        final ResourceConfig resourceConfig;
        
        if (staticDir.equalsIgnoreCase("swagger")) {

            // prepare resources (include every resource inside a certain package)
            resourceConfig = new ResourceConfig().packages(RESOURCE_PKG, SWAGGER_PKG);

            // swagger config
            BeanConfig config = new BeanConfig();
            config.setResourcePackage(RESOURCE_PKG);
            config.setVersion(API_VERSION);
            config.setBasePath(SWAGGER_URI.toString());
            config.setScan(true);
            config.setTitle("FD API");
            config.setDescription("Freedomotic API documentation");
        } else {
            resourceConfig = new ResourceConfig().packages(RESOURCE_PKG);
        }
        
        // enable json and xml support
        resourceConfig.registerClasses(JacksonFeature.class);
  
        // resourceConfig.registerClasses(MoxyXmlFeature.class);
        //resourceConfig.register(FDObjectMapper.class);
        
        // enable CORS 
        if (configuration.getBooleanProperty("enable-cors", false)) {
            resourceConfig.registerClasses(CorsFilter.class);
        }

        // enable log
        if (configuration.getBooleanProperty("debug", false)) {
            LoggingFilter lf = new LoggingFilter(LOG, configuration.getBooleanProperty("debug-entity", false));
            resourceConfig.register(lf);
            resourceConfig.property(ServerProperties.TRACING, "ALL");
            resourceConfig.property(ServerProperties.TRACING_THRESHOLD, "TRACE");
        }

        // enable auth feature, is FD security is enabled
        if (getApi().getAuth().isInited()) {
            resourceConfig.registerClasses(AuthenticationExceptionMapper.class);
            resourceConfig.register(new SecurityFilter(getApi()));
        }

        if (configuration.getBooleanProperty("enable-ssl", false)) {
            // Grizzly ssl configuration
            SSLContextConfigurator sslContext = new SSLContextConfigurator();
            sslContext.setKeyStoreFile(new File(this.getFile().getParent() + "/data/" + configuration.getStringProperty("KEYSTORE_SERVER_FILE", "keystore_server")).getAbsolutePath()); // contains server keypair
            sslContext.setKeyStorePass(configuration.getStringProperty("KEYSTORE_SERVER_PWD", "freedomotic"));

            server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig, true,
                    new SSLEngineConfigurator(sslContext).setClientMode(false));
        } else {
            server = GrizzlyHttpServerFactory.createHttpServer(BASE_URI, resourceConfig);
        }

        if (!staticDir.equalsIgnoreCase("none")) {
            // serve static files on directoryspecified in manifest
            StaticHttpHandler staticFiles = new StaticHttpHandler(new File(this.getFile().getParent() + "/data/" + staticDir + "/").getAbsolutePath());
            server.getServerConfiguration().addHttpHandler(staticFiles);
            LOG.log(Level.INFO, "Serving static files from: {0}", staticFiles.getDefaultDocRoot().getAbsolutePath());
        }

        StaticHttpHandler resourceFiles = new StaticHttpHandler(Info.PATH_RESOURCES_FOLDER.getAbsolutePath());
        server.getServerConfiguration().addHttpHandler(resourceFiles, "/res/");
        LOG.log(Level.INFO, "Serving RESOURCE files from: {0}", resourceFiles.getDefaultDocRoot().getAbsolutePath());

        addEventListener("app.event.sensor.object.behavior.change");
        addEventListener("app.event.sensor.environment.zone.change");
        addEventListener("app.event.sensor.plugin.change");

    }

    @Override
    protected void onStop() {
        LOG.info("Jersey RestAPI plugin is stopped ");
        try {
            server.shutdownNow();
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onCommand(Command c) {
        LOG.log(Level.INFO, "Jersey RestAPI plugin receives a command called {0} with parameters {1}", new Object[]{c.getName(), c.getProperties().toString()});

    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        if (event instanceof ObjectHasChangedBehavior) {
            ObjectChangeResource.broadcast(event);
        } else if (event instanceof ZoneHasChanged) {
            ZoneChangeResource.broadcast(event);
        } else if (event instanceof PluginHasChanged) {
            PluginChangeResource.broadcast(event);
        }
    }

}
