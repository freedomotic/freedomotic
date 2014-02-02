/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace;

import com.freedomotic.marketplace.util.DrupalRestHelper;
import org.openide.util.lookup.ServiceProvider;

import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author GGPT
 */
@ServiceProvider(service = IMarketPlace.class)
public class FreedomoticMarketPlace implements IMarketPlace {

    private ArrayList<IPluginPackage> packageList;
    private ArrayList<IPluginCategory> categoryList;

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
        packageList = new ArrayList<IPluginPackage>();
        packageList.addAll(DrupalRestHelper.retrieveAllPlugins());
    }

    @Override
    public ArrayList<IPluginCategory> getAvailableCategories() {
        return categoryList;
    }

    @Override
    public ArrayList<IPluginPackage> getAvailablePackages(IPluginCategory category) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
