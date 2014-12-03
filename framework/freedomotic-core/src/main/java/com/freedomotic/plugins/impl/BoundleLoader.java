/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.exceptions.PluginLoadingException;
import java.io.File;
import java.util.List;

/**
 * Loader service for loading plugin types deployed on local filesystem
 *
 * @author enrico
 */
interface BoundleLoader {

    /**
     * Loads all plugins. The concrete
     * factories will have to implement these methods.
     */
    List<Client> loadBoundle() throws PluginLoadingException;

    /**
     * Returns the root path in which the plugin is deployed on local
     * filesystem.
     *
     * @return the root path of where the plugin is deployed
     */
    File getPath();
}
