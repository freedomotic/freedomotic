/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.filesystem;

import com.freedomotic.exceptions.PluginLoadingException;
import java.io.File;
import java.net.URL;

/**
 *
 * @author enrico
 */
public interface PluginsManager {
    int TYPE_DEVICE = 0;
    int TYPE_EVENT = 2;
    int TYPE_OBJECT = 1;

    /**
     * Install a plugins boundle downloading it from remote URL
     */
    boolean installBoundle(URL fromURL);

    /**
     * Loads all plugins of a given type (device, object, event) taken from
     * their default folder.
     *
     * @param TYPE
     */
    void loadAllPlugins(int TYPE) throws PluginLoadingException;

    /**
     * Loads all plugins from filesystem regardless their type
     *
     * @param TYPE
     */
    void loadAllPlugins() throws PluginLoadingException;

    /**
     * Load a single plugin package from a given directory. This directory should be the
     * root path of the plugin package, not a directory containing more than one
     * plugin package.
     *
     * @param directory
     * @throws PluginLoadingException
     */
    void loadSingleBoundle(File directory) throws PluginLoadingException;
    
}
