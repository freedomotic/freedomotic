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
    public String getUri();

    /**
     * @return the path
     */
    public String getFilePath();
 
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
