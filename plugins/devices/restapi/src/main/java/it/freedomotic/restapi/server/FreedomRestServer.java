/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server;

import it.freedomotic.restapi.server.resources.*;
import org.restlet.Application;
import org.restlet.Component;
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
public class FreedomRestServer extends Application{
    
    private static final String FILE_AND_SLASHES = "file:///";    
    private String resourcesPath = "";
    public static final String FREEDOMOTIC_PATH = "/v2";
    public static final String ENVIRONMENT_PATH = "/v2/environments";
    public static final String RESOURCES_PATH = "/v2/resources";
    public FreedomRestServer() 
    {
        setName("Freedomotic API WebServer");
        setDescription("Restfull API server for the freedom enviroment");
        setOwner("freedomotic");
        setAuthor("Freedomotic dev team");
        getMetadataService().addExtension("object", MediaType.APPLICATION_JAVA_OBJECT);
         getMetadataService().addExtension("gwt_object", MediaType.APPLICATION_JAVA_OBJECT_GWT);
        this.resourcesPath = resourcesPath+"/";
    }
    public FreedomRestServer(String resourcesPath)
    {
        this();
        this.resourcesPath = resourcesPath+"/";    
    }        
    
 
/**
     * Returns the root Restlet of this application.
     */
    @Override
    public Restlet createInboundRoot() {
        Router router = new Router(getContext());
        router.attach(ENVIRONMENT_PATH+"/", EnvironmentsServerResource.class);
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
        //Expose the resources dir as static server
        Directory dir = new Directory(getContext(), FILE_AND_SLASHES+resourcesPath);
        dir.setListingAllowed(true);
        //System.out.println("FILE_AND_SLASHES+resourcesPath "+  FILE_AND_SLASHES+resourcesPath);
        router.attach(RESOURCES_PATH+"/", dir);
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
