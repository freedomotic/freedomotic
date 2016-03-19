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
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.exceptions.PluginLoadingException;
import java.io.File;
import java.util.List;

/**
 * Loader service for loading plugin types deployed on local filesystem
 *
 * @author Enrico Nicoletti
 */
interface BoundleLoader {

    /**
     * Loads all plugins. The concrete factories will have to implement these
     * methods.
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
