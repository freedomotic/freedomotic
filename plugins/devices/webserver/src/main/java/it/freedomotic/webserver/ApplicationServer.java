/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.webserver;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.Info;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jetty.server.Server;
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

    public ApplicationServer() {
        super("ApplicationServer", "/es.gpulido.webserver/applicationserver-manifest.xml");

        port = configuration.getIntProperty("PORT", 8080);
        webapp_dir = configuration.getStringProperty("WEBAPP_DIR", "/webapps/gwt_client");
        war_file = configuration.getStringProperty("WAR_FILE", "Freedomotic.war");
        //TODO: check that the war file is correct.
    }

    @Override
    public void onStart() {
        try {
            String dir = Info.getApplicationPath() + "/plugins/devices/es.gpulido.webserver/data" + webapp_dir;
            server = new Server(port);
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
            URL url = new URL("http://" + hostname + ":" + port + "/" + war_file.substring(0, war_file.lastIndexOf(".")));
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
