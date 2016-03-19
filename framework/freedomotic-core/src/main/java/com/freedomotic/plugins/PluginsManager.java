/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins;

import com.freedomotic.api.Client;
import com.freedomotic.exceptions.PluginLoadingException;
import java.io.File;
import java.net.URL;

/**
 * Loads bundles from external jar files at runtime. Loaded bundles are then
 * added to the {@link ClientStorage}
 *
 * @author Enrico Nicoletti
 */
public interface PluginsManager {

    int TYPE_DEVICE = 0;
    int TYPE_EVENT = 2;
    int TYPE_OBJECT = 1;

    /**
     * Install a plugins bundle downloading it from remote URL
     *
     * @param fromURL
     * @return
     */
    boolean installBoundle(URL fromURL);

    /**
     * Uninstalls a given plugin. The plugin can be part of a bundle, in this
     * case the entire boundle will be uninstalled
     *
     * @param c
     * @return
     */
    boolean uninstallBundle(Client c);

    /**
     * Loads all plugins of a given type (device, object, event) taken from
     * their default folder.
     *
     * @param TYPE
     * @throws com.freedomotic.exceptions.PluginLoadingException
     */
    void loadAllPlugins(int TYPE) throws PluginLoadingException;

    /**
     * Loads all plugins from filesystem regardless their type
     *
     * @throws com.freedomotic.exceptions.PluginLoadingException
     */
    void loadAllPlugins() throws PluginLoadingException;

    /**
     * Load a single plugin package from a given directory. This directory
     * should be the root path of the plugin package, not a directory containing
     * more than one plugin package.
     *
     * @param directory
     * @throws PluginLoadingException
     */
    void loadSingleBoundle(File directory) throws PluginLoadingException;

}
