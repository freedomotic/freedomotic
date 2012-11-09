/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

import it.freedomotic.service.IPluginCategory;
import it.freedomotic.service.IPluginPackage;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author gpt
 */
public class MarketPlacePluginCategory implements IPluginCategory{
    private String tid;
    private String name;        
    private String description;
    private String uri;
    private transient List<IPluginPackage> plugins;
    /**
     * @return the tid
     */
    public String getTid() {
        return tid;
    }   
        
    /**
     * @return the description
     */    
    public String getDescription() {
        return description;
    }               
     /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }   

    @Override
    public Integer getId() {
        return Integer.parseInt(tid);
    }
        
    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<IPluginPackage> getPlugins() {
        if (plugins == null)
        {
            plugins = new ArrayList<IPluginPackage>();
            plugins.addAll(DrupalRestHelper.retrievePluginsByCategory(tid));
        }
        return plugins;        
    }
    
    
}
