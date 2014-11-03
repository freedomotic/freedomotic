/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author GGPT
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
