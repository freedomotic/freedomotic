/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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

import com.freedomotic.marketplace.util.DrupalRestHelper;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;
import java.util.ArrayList;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Gabriel Pulido de Torres
 */
@ServiceProvider(service = IMarketPlace.class)
@XmlRootElement
public final class FreedomoticMarketPlace implements IMarketPlace {

    private final ArrayList<IPluginPackage> packageList;
    private final ArrayList<IPluginCategory> categoryList;

    public FreedomoticMarketPlace() {
        packageList = new ArrayList<IPluginPackage>();
        categoryList = new ArrayList<IPluginCategory>();
        updateCategoryList();

    }

    @Override
    public ArrayList<IPluginPackage> getAvailablePackages() {
        return packageList;
    }

    @Override
    public void updateAllPackageList() {
        packageList.clear();
        packageList.addAll(DrupalRestHelper.retrieveAllPlugins());
    }

    @Override
    public ArrayList<IPluginCategory> getAvailableCategories() {
        return categoryList;
    }

    @Override
    @XmlTransient
    public ArrayList<IPluginPackage> getAvailablePackages(IPluginCategory category) {
        return (ArrayList<IPluginPackage>) category.listPlugins();
    }

    @Override
    public void updateCategoryList() {
        categoryList.clear();
        categoryList.addAll(DrupalRestHelper.retrieveCategories());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        FreedomoticMarketPlace market = new FreedomoticMarketPlace();

    }
}
