/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

import it.freedomotic.service.IPluginPackage;
import java.util.ArrayList;
import java.util.List;

/**
 * POJO class that represents a plugin resume page from the Drupal Marketplace
 *
 * @author GGPT
 */
public class MarketPlacePluginResume {

    private String title;
    private String uri;
    //not serialized data
    private transient IPluginPackage plugin;

    /**
     * @return the uri
     */
    public String getUri() {
        return uri;
    }

    /**
     * @return the title
     */
    public String gettitle() {
        return title;
    }

    public IPluginPackage getPlugin() {
        if (plugin == null) {
            plugin = DrupalRestHelper.retrievePluginPackage(uri);
        }
        return plugin;
    }
}
