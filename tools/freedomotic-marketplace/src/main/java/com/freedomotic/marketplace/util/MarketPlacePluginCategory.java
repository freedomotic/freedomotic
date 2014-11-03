/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.util;

import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author gpt
 */
@XmlRootElement
public class MarketPlacePluginCategory implements IPluginCategory {

    private String tid;
    private String name;
    private String description;
    private String uri;
    private transient List<IPluginPackage> plugins = new ArrayList<IPluginPackage>();

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
    public List<IPluginPackage> retrievePluginsInfo() {
        plugins.clear();
        plugins.addAll(DrupalRestHelper.retrievePluginsByCategory(tid));
        return plugins;
    }

    @Override
    public List<IPluginPackage> listPlugins() {
        return plugins;
    }
}
