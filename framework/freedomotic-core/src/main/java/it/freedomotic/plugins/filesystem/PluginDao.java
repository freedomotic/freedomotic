/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.filesystem;

import it.freedomotic.api.Client;
import it.freedomotic.exceptions.PluginLoadingException;
import java.io.File;
import java.util.List;

/**
 * DAO pattern for loading plugin types deployed on local filesystem
 *
 * @author enrico
 */
interface PluginDao {

    /**
     * Loads all plugins. The concrete
     * factories will have to implement these methods.
     */
    List<Client> loadAll() throws PluginLoadingException;

    /**
     * Returns the root path in which the plugin is deployed on local
     * filesystem.
     *
     * @return the root path of where the plugin is deployed
     */
    File getPath();
}
