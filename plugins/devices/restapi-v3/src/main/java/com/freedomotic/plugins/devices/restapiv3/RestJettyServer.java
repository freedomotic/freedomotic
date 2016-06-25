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
package com.freedomotic.plugins.devices.restapiv3;

/**
 *
 * @author Matteo Mazzoni
 */
import com.freedomotic.api.Plugin;
import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.API_VERSION;
import com.freedomotic.plugins.devices.restapiv3.auth.ShiroListener;
import com.freedomotic.plugins.devices.restapiv3.filters.GuiceServletConfig;
import com.freedomotic.settings.Info;
import com.google.inject.servlet.GuiceFilter;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.inject.Inject;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.atmosphere.cpr.AtmosphereServlet;
import org.eclipse.jetty.http.ssl.SslContextFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RestJettyServer extends Server {

    private static final Logger LOG = LoggerFactory.getLogger(RestJettyServer.class.getName());

    private Server webServer;
    private Plugin master;
    
    @Inject
    private GuiceServletConfig guiceServletConfig;

    public RestJettyServer() {
    }

    public void startServer() throws Exception {
        webServer = new Server();
        LOG.info("Starting RestAPI Server...");

        /**
         * TODO WHEN MOVING TO JETTY 9 refactor connectors code and add spdy
         * support
         * http://download.eclipse.org/jetty/stable-9/xref/org/eclipse/jetty/embedded/SpdyConnector.html
         *
         */
        if (!master.configuration.getBooleanProperty("enable-ssl", false)) {
            SelectChannelConnector selectChannelConnector = new SelectChannelConnector();
            selectChannelConnector.setPort(master.configuration.getIntProperty("http-port", 9111));
            webServer.addConnector(selectChannelConnector);

        } else {
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePassword(master.configuration.getStringProperty("KEYSTORE_SERVER_PWD", "freedomotic"));

            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(
                    new FileInputStream(master.getFile().getParent() + "/data/" + master.configuration.getStringProperty("KEYSTORE_SERVER_FILE", "keystore_server")),
                    master.configuration.getStringProperty("KEYSTORE_SERVER_PWD", "freedomotic").toCharArray());
            sslContextFactory.setKeyStore(keyStore);
            SslSelectChannelConnector sslSelectChannelConnector = new SslSelectChannelConnector(sslContextFactory);
            sslSelectChannelConnector.setPort(master.configuration.getIntProperty("https-port", 9113));
            webServer.addConnector(sslSelectChannelConnector);

        }

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");

        // atmpsphere servlet 
        ServletHolder atmosphereServletHolder = new ServletHolder(AtmosphereServlet.class);
        atmosphereServletHolder.setInitParameter("jersey.config.server.provider.packages", RestAPIv3.ATMOSPHRE_RESOURCE_PKG);
        atmosphereServletHolder.setInitParameter("org.atmosphere.websocket.messageContentType", "application/json");
        atmosphereServletHolder.setInitParameter("org.atmosphere.cpr.AtmosphereInterceptor","org.atmosphere.interceptor.ShiroInterceptor");
//        atmosphereServletHolder.setInitParameter("org.atmosphere.cpr.broadcasterClass", "org.atmosphere.jersey.JerseyBroadcaster");
        atmosphereServletHolder.setAsyncSupported(true);
        atmosphereServletHolder.setInitParameter("org.atmosphere.useWebSocket", "true");
        atmosphereServletHolder.setInitOrder(2);
        context.addServlet(atmosphereServletHolder, "/" + API_VERSION + "/ws/*");

        // jersey servlet
        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("javax.ws.rs.Application", JerseyApplication.class.getCanonicalName());
        jerseyServletHolder.setInitParameter("jersey.config.server.wadl.disableWadl","true");
        jerseyServletHolder.setInitOrder(1);
        context.addServlet(jerseyServletHolder, "/" + API_VERSION + "/*");
        

        // cors filter
        if (master.configuration.getBooleanProperty("enable-cors", false)) {
            FilterHolder corsFilterHolder = new FilterHolder(CrossOriginFilter.class);
            corsFilterHolder.setInitParameter("allowedOrigins", master.configuration.getStringProperty("Access-Control-Allow-Origin", "*"));
            corsFilterHolder.setInitParameter("allowedMethods", master.configuration.getStringProperty("Access-Control-Allow-Methods", "GET,PUT,HEAD,POST,DELETE"));
            corsFilterHolder.setInitParameter("allowedHeaders",
                    master.configuration.getStringProperty("Access-Control-Allow-Headers",
                            "Accept,Accept-Version,Authorization,Content-Length,Content-MD5,Content-Type,Date,"
                            + "Origin,X-Access-Token,X-Api-Version,X-CSRF-Token,X-File-Name,X-Requested-With"));
            corsFilterHolder.setInitParameter("allowCredentials", "true");
            context.addFilter(corsFilterHolder, "/*", null);
        }

        // shiro filter
        if (master.getApi().getAuth().isInited()) {
            context.addEventListener(new ShiroListener());
            context.addFilter(ShiroFilter.class, "/*", null);
        }

        // guice filter
        context.addEventListener(guiceServletConfig);
        context.addFilter(GuiceFilter.class, "/*", null);
    
        //static files handler        
        String staticDir = master.configuration.getStringProperty("serve-static", "swagger");
        context.setResourceBase(new File(master.getFile().getParent() + "/data/" + staticDir + "/").getAbsolutePath());
        context.addServlet(DefaultServlet.class, "/*");

        // serve resource files (images and so on)
        ServletHolder resHolder = new ServletHolder("static-home", DefaultServlet.class);
        resHolder.setInitParameter("resourceBase", Info.PATHS.PATH_RESOURCES_FOLDER.getAbsolutePath());
        resHolder.setInitParameter("dirAllowed", "true");
        resHolder.setInitParameter("pathInfoOnly", "true");
        context.addServlet(resHolder, "/res/*");

        HandlerList handlers = new HandlerList();
        handlers.addHandler(context);
        handlers.addHandler(new DefaultHandler());

        webServer.setHandler(handlers);
        webServer.start();

        LOG.info("Started RestAPI Server");
    }

    public void stopServer() throws Exception {
        LOG.info("Stopping RestAPI Server...");
        webServer.stop();
        LOG.info("Stopped RestAPI Server");
    }

    void setMaster(RestAPIv3 master) {
        this.master = master;
    }

}
