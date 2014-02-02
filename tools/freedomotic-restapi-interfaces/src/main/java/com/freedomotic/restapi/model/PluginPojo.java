/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.restapi.model;

/**
 *
 * @author gpt
 */
public class PluginPojo {

    private String name;
    private boolean isRunning;

    public PluginPojo(String name, boolean isRunning) {
        this.setName(name);
        this.setIsRunning(isRunning);
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the isRunning
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * @param isRunning the isRunning to set
     */
    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
