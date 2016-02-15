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

package com.freedomotic.restapi;

import com.freedomotic.api.*;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.restapi.server.FreedomRestServer;
import com.freedomotic.restapi.server.OriginFilter;
import com.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.ext.crypto.DigestVerifier;
import org.restlet.ext.crypto.internal.HttpDigestVerifier;
import org.restlet.ext.simple.HttpServerHelper;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.LocalVerifier;
import org.restlet.security.SecretVerifier;
import org.restlet.security.Verifier;
import static org.restlet.security.Verifier.RESULT_INVALID;
import static org.restlet.security.Verifier.RESULT_VALID;
import org.restlet.util.Series;

/**
 *
 * @author gpt
 */

public class RestApi extends com.freedomotic.api.Protocol {

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
     *
     * @return the Freedomotic APIs reference
     */
    public static API getFreedomoticApi() {
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
            
            // enable SSL 
            Server SSLserver = new Server(Protocol.HTTPS, configuration.getIntProperty("SSL_PORT", SERVER_PORT + 2 ));
            component.getServers().add(SSLserver);
            Series<Parameter> parameters = SSLserver.getContext().getParameters();
            parameters.add("sslContextFactory", "org.restlet.ext.ssl.PkixSslContextFactory");
            // Certificate's data is taken from config file
            parameters.add("keystorePath", new File(this.getFile().getParent() + "/data/" + configuration.getStringProperty("KEYSTORE_FILE", "keystore")).getAbsolutePath());
            parameters.add("keystorePassword", configuration.getStringProperty("KEYSTORE_PASSWORD", "password"));
            parameters.add("keyPassword", configuration.getStringProperty("KEYSTORE_PASSWORD", "password"));
            parameters.add("keystoreType", configuration.getStringProperty("KEYSTORE_TYPE", "JKS")); 
            // end enable SSL
            
            // Engine.getInstance().getRegisteredServers().clear();
            // Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(server));
            // Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(SSLserver));
            component.getClients().add(Protocol.FILE);
            
            OriginFilter originFilter = new OriginFilter(component.getContext().createChildContext(),this);
            Application FDapp =  new FreedomRestServer(Info.PATHS.PATH_RESOURCES_FOLDER.getAbsolutePath(), component.getContext().createChildContext());  
            
            if (getApi().getAuth().isInited()) {
                // Instantiates a Verifier of identifier/secret couples based on Freedomotic Auth
                Verifier v = new SecretVerifier() {
                    @Override
                    public int verify(String identifier, char[] secret) {
                        if (getApi().getAuth().login(identifier, secret)) {
                            return RESULT_VALID;
                        }
                        return RESULT_INVALID;
                    }
                };
                // Guard the restlet with BASIC authentication.
                ChallengeAuthenticator guard = new ChallengeAuthenticator(component.getContext().createChildContext(), false, ChallengeScheme.HTTP_BASIC, "testRealm", v);
                
                // WIP: Guard the restlet with DIGEST authentication.
                DigestAuthenticator dguard = new DigestAuthenticator(component.getContext().createChildContext(), "DigestRealm", configuration.getStringProperty("DIGEST_SECRET", "s3cr3t"));
                dguard.setOptional(true);
                // TODO: set proper verifier before enabling DIGEST AUTH.                
                
                originFilter.setNext(guard);
                guard.setNext(FDapp);
                
            } else {
                originFilter.setNext(FDapp);
            }

            component.getDefaultHost().attachDefault(originFilter);
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
