/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;


import it.freedomotic.core.ResourcesManager;
import it.freedomotic.restapi.server.FreedomRestServer;
import it.freedomotic.restapi.server.interfaces.ImageResource;
import it.freedomotic.util.Info;
import java.io.File;
import org.restlet.resource.ServerResource;



/**
 *
 * @author gpt
 */
public class ImageResourceServerResource extends ServerResource implements ImageResource{

    private static volatile String imageFilePath;    	
    
    //TODO: this is a first implementation. Use a restlet redirector instead.
    @Override
    public void doInit() {                           
        String fileName =(String) getRequest().getAttributes().get("filename");       
        imageFilePath = ResourcesManager.getFile(new File(Info.getResourcesPath()),fileName).getPath();              
    }
       
    
    @Override
    public String getImagePath() {        
        String path = (imageFilePath.split(Info.getResourcesPath()))[1];
        redirectSeeOther(FreedomRestServer.RESOURCES_PATH+path);
        return path;    
    }
    
}
