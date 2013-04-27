/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.restapi.server.interfaces.EnvironmentsResource;
import it.freedomotic.restapi.server.interfaces.ZonesResource;
import java.util.ArrayList;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 * @author gpt
 */
public class EnvironmentsServerResource extends ServerResource implements EnvironmentsResource { 

    private static volatile ArrayList<Environment> environments;  
        
    @Override
    protected void doInit() throws ResourceException{
        
        environments = new ArrayList<Environment>();
        for(EnvironmentLogic env: EnvironmentPersistence.getEnvironments())            
        {
            environments.add(env.getPojo());  
        }        
                     
    }
        
    @Override
    public String retrieveXml() {   
        String ret = "";
        XStream xstream =FreedomXStream.getXstream(); 
        ret = xstream.toXML(environments);
        return ret;                
    }
    
       @Override
    public String retrieveJson() {        
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);                
        ret = xstream.toXML(environments);        
        return ret;
    }
    
    @Override
    public ArrayList<Environment> retrieveEnvironments() {
        return environments;
    }
}