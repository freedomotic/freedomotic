/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.exceptions;

/**
 *
 * @author enrico
 */
public class PluginLoadingException extends FreedomoticException {

    String pluginName;

    /**
     * Creates a new instance of
     * <code>PluginLoadingException</code> without detail message.
     */
    public PluginLoadingException() {
    }

    /**
     *
     * @return
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     *
     * @param pluginName
     */
    public final void setPluginName(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Constructs an instance of
     * <code>PluginLoadingException</code> with the specified detail message.
     *
     * @param msg the detail message.
     */
    public PluginLoadingException(String msg) {
        super(msg);
    }

    /**
     *
     * @param message
     * @param cause
     */
    public PluginLoadingException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     *
     * @param message
     * @param pluginName
     * @param cause
     */
    public PluginLoadingException(String message, String pluginName, Throwable cause) {
        super(message, cause);
        setPluginName(pluginName);
    }

    /**
     *
     * @param cause
     */
    public PluginLoadingException(Throwable cause) {
        super(cause);
    }
}
