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
package com.freedomotic.marketplace;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author GGPT
 */
@XmlRootElement
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public interface IMarketPlace {

    /**
     *
     * @return
     */
    List<IPluginCategory> getAvailableCategories();

    /**
     *
     * @return
     */
    List<IPluginPackage> getAvailablePackages();

    /**
     *
     * @param category
     * @return
     */
    List<IPluginPackage> getAvailablePackages(IPluginCategory category);

    /**
     *
     */
    public void updateAllPackageList();

    /**
     *
     */
    public void updateCategoryList();
}
