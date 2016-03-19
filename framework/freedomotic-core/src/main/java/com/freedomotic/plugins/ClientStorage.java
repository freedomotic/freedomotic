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
import com.freedomotic.api.Plugin;
import com.freedomotic.exceptions.RepositoryException;
import java.io.File;
import java.util.List;

/**
 *
 * @author Enrico Nicoletti
 */
public interface ClientStorage {
    /*
     * Checks if a plugin is already installed, if is an obsolete or newer
     * version
     */

    /**
     *
     * @param name
     * @param version
     * @return
     */
    int compareVersions(String name, String version);

    /**
     *
     * @param template
     * @return
     * @throws RepositoryException
     */
    Client createObjectPlaceholder(final File template)
            throws RepositoryException;

    /**
     * Creates a placeholder plugin and adds it to the list of added plugins.
     * This plugin is just a mock object to inform the user that an object with
     * complete features is expected here. It can be used for example to list a
     * fake plugin that informs the user the real plugin cannot be added.
     *
     * @param simpleName
     * @param type
     * @param description
     * @return
     */
    Plugin createPluginPlaceholder(final String simpleName, final String type, final String description);

    /**
     *
     * @param c
     */
    void remove(Client c);

    /**
     *
     * @param c
     */
    void add(Client c);

    /**
     *
     * @param name
     * @return
     */
    Client get(String name);

    /**
     *
     * @param protocol
     * @return
     */
    Client getClientByProtocol(String protocol);

    /**
     *
     * @return
     */
    List<Client> getClients();

    /**
     *
     * @param filterType
     * @return
     */
    List<Client> getClients(String filterType);

    /**
     *
     * @param input
     * @return
     */
    boolean isLoaded(Client input);
}
