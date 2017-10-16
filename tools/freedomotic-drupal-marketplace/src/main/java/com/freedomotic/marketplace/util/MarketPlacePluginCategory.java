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
package com.freedomotic.marketplace.util;

import com.freedomotic.marketplace.IPluginCategory;
import com.freedomotic.marketplace.IPluginPackage;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Gabriel Pulido de Torres
 */
@XmlRootElement
public class MarketPlacePluginCategory implements IPluginCategory {

    private String tid;
    private String name;
    private String description;
    private String uri;
    private transient List<IPluginPackage> plugins = new ArrayList<>();

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
