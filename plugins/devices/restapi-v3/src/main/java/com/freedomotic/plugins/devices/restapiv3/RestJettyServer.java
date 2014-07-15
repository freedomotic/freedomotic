/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3;

/**
 *
 * @author matteo
 */
import com.freedomotic.api.Plugin;
import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.API_VERSION;
import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.JERSEY_RESOURCE_PKG;
import com.freedomotic.plugins.devices.restapiv3.auth.ShiroListener;
import com.freedomotic.util.Info;
import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.logging.Logger;
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

public final class RestJettyServer extends Server {

    private static final Logger LOG = Logger.getLogger(RestJettyServer.class.getName());

    private static Server webServer;
    private static Plugin master;

    public RestJettyServer(Plugin master) {
        RestJettyServer.master = master;
    }

    public void startServer() throws Exception {
        webServer = new Server();
        LOG.info("Starting RestAPI Server...");

        /**
         * TODO: WHEN MOVING TO JETTY 9 refactor connectors code and add spdy support
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
        atmosphereServletHolder.setAsyncSupported(true);
        atmosphereServletHolder.setInitParameter("org.atmosphere.useWebSocket", "true");
        context.addServlet(atmosphereServletHolder, "/" + API_VERSION + "/ws/*");

        // jersey servlet
        ServletHolder jerseyServletHolder = new ServletHolder(ServletContainer.class);
        jerseyServletHolder.setInitParameter("jersey.config.server.provider.packages", JERSEY_RESOURCE_PKG);
        context.addServlet(jerseyServletHolder, "/" + API_VERSION + "/*");

        // cors filter
        if (master.configuration.getBooleanProperty("enable-cors", false)) {
            FilterHolder corsFilterHolder = new FilterHolder(CrossOriginFilter.class);
            corsFilterHolder.setInitParameter("allowedOrigins", master.configuration.getStringProperty("Access-Control-Allow-Origin", "*"));
            corsFilterHolder.setInitParameter("allowedMethods", master.configuration.getStringProperty("Access-Control-Allow-Methods", "GET,PUT,HEAD,POST,DELETE,OPTIONS"));
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
            context.addFilter(ShiroFilter.class, "/" + API_VERSION + "/*", null);
        }
        
        //static files handler        
        String staticDir = master.configuration.getStringProperty("serve-static", "swagger");
        context.setResourceBase(new File(master.getFile().getParent() + "/data/" + staticDir + "/").getAbsolutePath());
        context.addServlet(DefaultServlet.class, "/*");

        // serve resource files (images and so on)
        ServletHolder resHolder = new ServletHolder("static-home", DefaultServlet.class);
        resHolder.setInitParameter("resourceBase",Info.PATHS.PATH_RESOURCES_FOLDER.getAbsolutePath());
        resHolder.setInitParameter("dirAllowed","true");
        resHolder.setInitParameter("pathInfoOnly","true");
        context.addServlet(resHolder,"/res/*");
        
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

}
