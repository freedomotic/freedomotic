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

package it.freedomotic.restapi;

import it.freedomotic.api.*;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.restapi.server.FreedomRestServer;
import it.freedomotic.util.Info;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.simple.HttpServerHelper;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.SecretVerifier;


/**
 *
 * @author gpt
 */

public class RestApi extends it.freedomotic.api.Protocol {

    int SERVER_PORT = 8111;
    Component component;
    Restlet rest;
    Application app;
    private static API freedomoticApi;

    public RestApi() {
        super("RestApi", "/restapi/restapi-manifest.xml");
        setPollingWait(-1);
	//Avoid The reset of the logging.
	//see Issue Core-132 http://freedomotic.myjetbrains.com/youtrack/issue/Core-132
	System.setProperty("java.util.logging.config.file", "none");
    }
    
    /**
     * Expose Freedomotic APIs as a static reference for restapi internal use
     * @return the Freedomotic APIs reference
     */
    public static API getFreedomoticApi(){
        return freedomoticApi;
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
            
            // Guard the restlet with BASIC authentication.
            ChallengeAuthenticator guard = new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "testRealm");
            // Instantiates a Verifier of identifier/secret couples based on a simple Map.
            guard.setVerifier(new SecretVerifier(){
                
                    @Override
                    public int verify(String identifier, char[] secret) {
                        if (getApi().getAuth().login(identifier, secret)){
                        return RESULT_VALID;
                    } 
                    return RESULT_INVALID;
                }
            });
            
            guard.setNext(new FreedomRestServer(Info.getResourcesPath()));
            
            Engine.getInstance().getRegisteredServers().clear();
            Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(server));
            component.getClients().add(Protocol.FILE);
            component.getDefaultHost().attachDefault(guard);
            //component.getDefaultHost().attach(new FreedomRestServer(Info.getResourcesPath()));
            component.start();
            freedomoticApi = getApi();
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
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onEvent(EventTemplate event) {
    }
}
