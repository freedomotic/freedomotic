/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.service;

import javax.swing.ImageIcon;

/**
 *
 * @author gpt
 */
public interface IPluginPackage {
       
    /**
     * @return the title
     */
    public String getTitle();

    /**
     * @return the uri
     */
    public String getURI();

    /**
     * @return the path
     */
    public String getFilePath();
 
    /**
     * Returns the path of the file matching the core version
     * Empty string if it doesn't have any file matching the version
     */
    public String getFilePath(String version);
    
    /**
     * @return the type
     */
    public String getType();

    /**
     * @return the description
     */
    public String getDescription();    

    /**
     * @return the icon
     */
    public ImageIcon getIcon();
    
}
