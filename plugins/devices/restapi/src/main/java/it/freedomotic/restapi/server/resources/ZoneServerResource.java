/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import it.freedomotic.environment.EnvironmentPersistence;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.restapi.server.interfaces.ZoneResource;

import org.restlet.resource.ServerResource;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */
public class ZoneServerResource extends ServerResource implements ZoneResource{
    private static volatile Zone zone;    	
    @Override
    public void doInit() { 
        int env =Integer.parseInt((String)getRequest().getAttributes().get("env"));
        int number =Integer.parseInt((String)getRequest().getAttributes().get("number"));        
        zone = EnvironmentPersistence.getEnvironments().get(env).getPojo().getZone(number);                           
    }

    @Override
    public String retrieveXml() {        
        String ret = "";
        XStream xstream =FreedomXStream.getXstream(); 
        ret = xstream.toXML(zone);
        return ret;
    }
    
    @Override
    public String retrieveJson() {        
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);                
        ret = xstream.toXML(zone);        
        return ret;
    }
    
    @Override   
    public Zone retrieveZone() {       
       return zone;
    }  
    
}

