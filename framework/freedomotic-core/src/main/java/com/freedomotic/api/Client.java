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
package com.freedomotic.api;

import com.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico Nicoletti
 */
public interface Client {

    /**
     *
     * @param name
     */
    public void setName(String name);

    /**
     *
     * @return
     */
    public String getDescription();

    /**
     *
     * @param description
     */
    public void setDescription(String description);

    /**
     *
     * @return
     */
    public Config getConfiguration();

    /**
     *
     * @return
     */
    public String getName();

    /**
     *
     * @return
     */
    public String getType();

    /**
     *
     */
    public void start();

    /**
     *
     */
    public void stop();

    /**
     * Completely unloads a plugin destroying all the locked resources (eg:
     * messaging channel)
     */
    public void destroy();

    /**
     *
     * @return
     */
    public boolean isRunning();

    /**
     *
     */
    @Deprecated
    public void showGui();

    /**
     *
     */
    @Deprecated
    public void hideGui();
}
