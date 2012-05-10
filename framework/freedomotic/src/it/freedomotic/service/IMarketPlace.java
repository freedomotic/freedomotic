/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.service;

import java.util.ArrayList;

/**
 *
 * @author GGPT
 */
public interface IMarketPlace {
    ArrayList<PluginPackage> getAvailablePackages();
    public void updatePackageList();     
}
