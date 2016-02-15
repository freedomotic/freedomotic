/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.restapi.server;

import com.freedomotic.restapi.server.resources.EnvironmentServerResource;
import com.freedomotic.restapi.server.resources.EnvironmentsServerResource;
import com.freedomotic.restapi.server.resources.HardwareCommandsServerResource;
import com.freedomotic.restapi.server.resources.ImageResourceServerResource;
import com.freedomotic.restapi.server.resources.ObjectServerResource;
import com.freedomotic.restapi.server.resources.ObjectsServerResource;
import com.freedomotic.restapi.server.resources.PluginsServerResource;
import com.freedomotic.restapi.server.resources.TriggersServerResource;
import com.freedomotic.restapi.server.resources.UserCommandsServerResource;
import com.freedomotic.restapi.server.resources.UserServerResource;
import com.freedomotic.restapi.server.resources.ZoneServerResource;
import com.freedomotic.restapi.server.resources.ZonesServerResource;
import com.freedomotic.util.Info;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.simple.HttpServerHelper;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;

/**
 *
 * @author gpt
 */
public class FreedomRestServer extends Application {

    private static final String FILE_AND_SLASHES = "file:///";
    private String resourcesPath = "";
    public static final String FREEDOMOTIC_PATH = "/v2";
    public static final String ENVIRONMENT_PATH = "/v2/environments";
    public static final String RESOURCES_PATH = "/v2/resources";
    public static final String USER_PATH = "/v2/user";
            
    public FreedomRestServer() 
    {
        setName("Freedomotic API WebServer");
        setDescription("Restfull API server for the freedom enviroment");
        setOwner("freedomotic");
        setAuthor("Freedomotic dev team");
        getMetadataService().addExtension("object", MediaType.APPLICATION_JAVA_OBJECT);
        getMetadataService().addExtension("gwt_object", MediaType.APPLICATION_JAVA_OBJECT_GWT);

    }

    public FreedomRestServer(String resourcesPath, Context ctx) {
        this();
        this.resourcesPath = resourcesPath ;
        setContext(ctx);
    }

    /**
     * Returns the root Restlet of this application.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach(ENVIRONMENT_PATH, EnvironmentsServerResource.class);
        router.attach(ENVIRONMENT_PATH+"/{number}", EnvironmentServerResource.class);
        router.attach(ENVIRONMENT_PATH+"/{env}/zones/", ZonesServerResource.class);
        router.attach(ENVIRONMENT_PATH+"/{env}/zones/{number}", ZoneServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/objects/", ObjectsServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/objects/{name}", ObjectServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/plugins/", PluginsServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/commands/hardware/", HardwareCommandsServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/commands/user/", UserCommandsServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/triggers/", TriggersServerResource.class);
        router.attach(FREEDOMOTIC_PATH+"/resources/{filename}", ImageResourceServerResource.class);
        router.attach(USER_PATH + "/{useraction}", UserServerResource.class);
        //Expose the resources dir as static server
        Directory dir = new Directory(getContext(), FILE_AND_SLASHES + resourcesPath);
        dir.setListingAllowed(true);
        System.out.println("Restapi resources is serving: " + FILE_AND_SLASHES + resourcesPath);
        router.attach(RESOURCES_PATH , dir);
        
        return router;
    }

    public static void main(String[] args) throws Exception {
           
            Component component = new Component();
            component.getClients().add(Protocol.FILE);
            //TODO: To test with the restlet 2.1 Maybe the maxTotalConnections could be avoided
            // see: http://restlet-discuss.1400322.n2.nabble.com/rejectedExecution-td4513620.html 
            //component.getServers().add(Protocol.HTTP, SERVER_PORT);
            Server server = new Server(Protocol.HTTP, 8111);                     
            component.getServers().add(server);
            server.getContext().getParameters().add("maxTotalConnections", "50");
            //end TODO
            Engine.getInstance().getRegisteredServers().clear();
            Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(server)); 
            component.getClients().add(Protocol.FILE);                
            component.getDefaultHost().attach(new FreedomRestServer());
            component.start();
    }    
}
