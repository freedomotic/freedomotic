/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace;

import it.freedomotic.marketplace.util.DrupalRestHelper;
import it.freedomotic.service.IMarketPlace;
import it.freedomotic.service.PluginPackage;
import java.util.ArrayList;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author GGPT
 */
@ServiceProvider(service = IMarketPlace.class)
public class FreedomoticMarketPlace implements IMarketPlace{
    private ArrayList<PluginPackage> packageList;
    
    public FreedomoticMarketPlace() {
        packageList = new ArrayList<PluginPackage>();
        updatePackageList();    
    }
      
    @Override
    public ArrayList<PluginPackage> getAvailablePackages() {
        return packageList;
    }

    @Override
    public void updatePackageList() {
        packageList = DrupalRestHelper.retrievePackageList();
    }
    
}
