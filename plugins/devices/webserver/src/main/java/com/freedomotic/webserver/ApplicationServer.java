/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.webserver;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 *
 * @author gpt
 */
public class ApplicationServer extends Protocol {
    //TODO: read from config file

    int port;// = 8080;
    String webapp_dir;
    String war_file;
    public static final String WEBAPP_CTX = "/";
    public static Server server;
    private static boolean enableSSL;
    private static int sslPort;
    private static String keystorePassword;
    private static String keystoreFile;
    private static String keystoreType;

    public ApplicationServer() {
        super("ApplicationServer", "/webserver/applicationserver-manifest.xml");

        port = configuration.getIntProperty("PORT", 8080);
        webapp_dir = configuration.getStringProperty("WEBAPP_DIR", "/webapps/gwt_client");
        war_file = configuration.getStringProperty("WAR_FILE", "");
        enableSSL = configuration.getBooleanProperty("ENABLE_SSL", true);
        sslPort = configuration.getIntProperty("SSL_PORT", 8443);
        keystorePassword = configuration.getStringProperty("KEYSTORE_PASSWORD", "password");
        keystoreFile = configuration.getStringProperty("KEYSTORE_FILE", "keystore");
        keystoreType = configuration.getStringProperty("KEYSTORE_TYPE", "JKS");

        //TODO: check that the war file is correct.
    }

    @Override
    public void onStart() {
        String dir = new File(this.getFile().getParent() + File.separator + webapp_dir).getAbsolutePath();

        server = new Server(port);
        LOG.info("Webserver now listens on port " + port);
        if (enableSSL) {
            KeyStore ks;
            String keyStorePath = this.getFile().getParent() + "/data/" + keystoreFile;
            try {
                ks = KeyStore.getInstance(keystoreType);
                ks.load(new FileInputStream(keyStorePath), keystorePassword.toCharArray());

                SslContextFactory sslContextFactory = new SslContextFactory();
                sslContextFactory.setKeyStore(ks);
                sslContextFactory.setKeyStorePassword(keystorePassword);
                sslContextFactory.setKeyStoreType(keystoreType);

                SslSocketConnector SSLConnector = new SslSocketConnector(sslContextFactory);

                SSLConnector.setPort(sslPort);
                SSLConnector.setMaxIdleTime(30000);
                LOG.info("Webserver now listens on SLL port " + sslPort);
                server.addConnector(SSLConnector);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Cannot load java keystore for reason: ", ex.getLocalizedMessage());
            }

        }
//        WebAppContext context = new WebAppContext();        
//        context.setDescriptor(dir+"/WEB-INF/web.xml");
//        context.setResourceBase(dir);
//        context.setContextPath(WEBAPP_CTX);
//        context.setParentLoaderPriority(true);  
//        server.setHandler(context);

        if (!war_file.isEmpty()) {
            try {
                WebAppContext webapp = new WebAppContext();
                webapp.setContextPath(WEBAPP_CTX);
                webapp.setWar(dir + "/" + war_file);
                server.setHandler(webapp);

                //print the URL to visit as plugin description
                InetAddress addr = InetAddress.getLocalHost();
                String hostname = addr.getHostName();
                //strip away the '.war' extension and put all togheter
                URL url = new URL("http://" + hostname + ":" + port + "/" + war_file.substring(0, war_file.lastIndexOf(".")));
                setDescription("Visit " + url.toString());
                server.start();
            } catch (FileNotFoundException nf) {
                LOG.warning("Cannot find WAR file " + war_file + " into directory " + dir);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
                context.setContextPath("/");
                context.setResourceBase(new File(dir + "/").getAbsolutePath());
                context.addServlet(DefaultServlet.class, "/*");
                server.setHandler(context);
                server.start();
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public void onStop() {
        try {
            server.stop();
            setDescription(configuration.getStringProperty("description", this.getName()));
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }

    }

    @Override
    protected void onRun() {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    private static final Logger LOG = Logger.getLogger(ApplicationServer.class.getName());
}
