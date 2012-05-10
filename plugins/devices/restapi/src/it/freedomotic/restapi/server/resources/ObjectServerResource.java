/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

import it.freedomotic.model.object.EnvObject;
import it.freedomotic.persistence.EnvObjectPersistence;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.restapi.server.interfaces.ObjectResource;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;

/**
 *
 * @author gpt
 */
public class ObjectServerResource extends ServerResource implements ObjectResource{
    private static volatile EnvObject envObject;
    String name;
 	
    @Override
    public void doInit() {
        name =Reference.decode((String)getRequest().getAttributes().get("name"));                              
        envObject = EnvObjectPersistence.getObject(name).getPojo();                           
    }

    @Override
    public String retrieveXml() {        
        String ret = "";
        XStream xstream =FreedomXStream.getXstream(); 
        ret = xstream.toXML(envObject);
        return ret;
    }

    @Override
    public String retrieveJson() {        
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);                
        ret = xstream.toXML(envObject);
        System.out.println("json: "+ret);
        return ret;
    }
        
    @Override   
    public EnvObject retrieveObject() {                      
       return ObjectServerResource.envObject;
    }
    
}
