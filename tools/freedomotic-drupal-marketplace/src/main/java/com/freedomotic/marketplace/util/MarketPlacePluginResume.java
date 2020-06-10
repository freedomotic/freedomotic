/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.marketplace.util;

import com.freedomotic.marketplace.IPluginPackage;

/**
 * POJO class that represents a plugin resume page from the Drupal Marketplace
 *
 * @author Gabriel Pulido de Torres
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
