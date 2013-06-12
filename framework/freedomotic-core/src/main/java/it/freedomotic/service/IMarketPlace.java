/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.service;

import java.util.List;

/**
 *
 * @author GGPT
 */
public interface IMarketPlace {
    List<IPluginCategory> getAvailableCategories();
    List<IPluginPackage> getAvailablePackages();
    List<IPluginPackage> getAvailablePackages(IPluginCategory category);
    public void updateAllPackageList();
    public void updateCategoryList();
}
