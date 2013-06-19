/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.service;

import java.util.List;

/**
 *
 * @author gpt
 */
public interface IPluginCategory {
      /**
     * @return the id
     */
    public Integer getId();

    /**
     * @return the name
     */
    public String getName();

    public List<IPluginPackage> getPlugins();
    
}
