/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.restapi.server.interfaces.TriggersResource;
import java.util.ArrayList;
import java.util.Iterator;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 * @author gpt
 */
public class TriggersServerResource extends ServerResource implements TriggersResource{

    private static volatile ArrayList<Trigger> triggers;  
        
    @Override
    protected void doInit() throws ResourceException{        
        triggers =  TriggerPersistence.getTriggers();            
    }
        
    @Override
    public String retrieveXml() {   
        String ret = "";
        XStream xstream =FreedomXStream.getXstream(); 
        ret = xstream.toXML(triggers);
        return ret;                
    }
    
        @Override
    public String retrieveJson() {        
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);                
        ret = xstream.toXML(triggers);        
        return ret;
    }

  
}
