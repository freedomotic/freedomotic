/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import it.freedomotic.api.Client;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.plugins.AddonManager;
import it.freedomotic.restapi.server.interfaces.PluginsResource;
import java.util.ArrayList;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 *
 * @author gpt
 */
public class PluginsServerResource extends ServerResource implements PluginsResource{

    private static volatile ArrayList<PluginPojo> plugins;
        
    @Override
    protected void doInit() throws ResourceException{
        plugins = new ArrayList<PluginPojo>();
        for (Client c : Freedomotic.clients.getClients()) {
            if (c.getType().equalsIgnoreCase("plugin")) {
                plugins.add(new PluginPojo(c.getName(),c.isRunning()));
            }
        }
           
    }
        
    @Override
    public String retrieveXml() {   
        String ret = "";
        XStream xstream = xstream = new XStream();
        xstream.registerConverter(new PluginConverter());
        ret = xstream.toXML(plugins);
        return ret;                
    }
    
        @Override
    public String retrieveJson() {        
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);                
        ret = xstream.toXML(plugins);        
        return ret;
    }

  
}
