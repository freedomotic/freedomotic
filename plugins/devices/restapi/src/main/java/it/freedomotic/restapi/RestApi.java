/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi;

import it.freedomotic.api.Actuator;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.restapi.server.FreedomRestServer;
import it.freedomotic.util.Info;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.simple.HttpServerHelper;

/**
 *
 * @author gpt
 */
public class RestApi extends Actuator {

    int SERVER_PORT = 8111;
    Component component;
    Restlet rest;
    Application app;

    public RestApi() {
        super("RestApi", "/es.gpulido.restapi/restapi-manifest.xml");
    }

    @Override
    public void onStart() {
        try {
            super.onStart();
            component = new Component();
            component.getClients().add(Protocol.FILE);
            //TODO: To test with the restlet 2.1 Maybe the maxTotalConnections could be avoided
            // see: http://restlet-discuss.1400322.n2.nabble.com/rejectedExecution-td4513620.html 
            //component.getServers().add(Protocol.HTTP, SERVER_PORT);
            Server server = new Server(Protocol.HTTP, SERVER_PORT);
            component.getServers().add(server);
            server.getContext().getParameters().add("maxTotalConnections", "50");
            //end TODO
            Engine.getInstance().getRegisteredServers().clear();
            Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(server));
            component.getClients().add(Protocol.FILE);
            component.getDefaultHost().attach(new FreedomRestServer(Info.getResourcesPath()));
            component.start();
        } catch (Exception ex) {
            Logger.getLogger(RestApi.class.getName()).log(Level.SEVERE, null, ex);
        }
       
    }

    @Override
    public void onStop() {
        try {
            component.stop();
        } catch (Exception ex) {
            Logger.getLogger(RestApi.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
