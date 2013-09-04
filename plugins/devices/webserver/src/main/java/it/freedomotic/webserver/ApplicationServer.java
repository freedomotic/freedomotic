/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */

package it.freedomotic.webserver;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ssl.SslSocketConnector;
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
    private static String plugPath = "/plugins/devices/es.gpulido.webserver/data";
    private static String keystoreType;

    public ApplicationServer() {
        super("ApplicationServer", "/webserver/applicationserver-manifest.xml");

        port = configuration.getIntProperty("PORT", 8080);
        webapp_dir = configuration.getStringProperty("WEBAPP_DIR", "/webapps/gwt_client");
        war_file = configuration.getStringProperty("WAR_FILE", "Freedomotic.war");
        enableSSL  =configuration.getBooleanProperty("ENABLE_SSL", true);
        sslPort= configuration.getIntProperty("SSL_PORT", 8443);
        keystorePassword = configuration.getStringProperty("KEYSTORE_PASSWORD", "password");
        keystoreFile= configuration.getStringProperty("KEYSTORE_FILE", "keystore");
        keystoreType= configuration.getStringProperty("KEYSTORE_TYPE", "JKS");
        
        //TODO: check that the war file is correct.
    }

    @Override
    public void onStart() {
        try {
            String dir = Info.getApplicationPath() + plugPath + webapp_dir;
            server = new Server(port);
            if (enableSSL){
            SslSocketConnector SSLConnector = new SslSocketConnector();
            SSLConnector.setPort(sslPort);
            SSLConnector.setMaxIdleTime(30000);
            SSLConnector.setKeystore(Info.getApplicationPath() + plugPath + File.separator+ keystoreFile);
            SSLConnector.setKeyPassword(keystorePassword);
            SSLConnector.setPassword(keystorePassword);
            SSLConnector.setKeystoreType(keystoreType);
            server.addConnector(SSLConnector);
            
            }
//        WebAppContext context = new WebAppContext();        
//        context.setDescriptor(dir+"/WEB-INF/web.xml");
//        context.setResourceBase(dir);
//        context.setContextPath(WEBAPP_CTX);
//        context.setParentLoaderPriority(true);  
//        server.setHandler(context);


            WebAppContext webapp = new WebAppContext();
            webapp.setContextPath(WEBAPP_CTX);
            webapp.setWar(dir + "/" + war_file);
            server.setHandler(webapp);
            
            //print the URL to visit as plugin description
            InetAddress addr = InetAddress.getLocalHost();
            String hostname = addr.getHostName();
            //strip away the '.war' extension and put all togheter
            URL url = new URL("http://"+hostname+":"+port+"/"+war_file.substring(0, war_file.lastIndexOf(".")));
            setDescription("Visit " + url.toString());


            server.start();
        } catch (Exception ex) {
            Logger.getLogger(ApplicationServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onStop() {
        try {
            server.stop();
            setDescription(configuration.getStringProperty("description", this.getName()));
        } catch (Exception ex) {
            Logger.getLogger(ApplicationServer.class.getName()).log(Level.SEVERE, null, ex);
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
}