/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.exceptions;

/**
 *
 * @author enrico
 */
public class PluginShutdownException extends Exception {
    
    public PluginShutdownException(String msg) {
        super(msg);
    }


    public PluginShutdownException(String message, Throwable cause) {
        super(message, cause);
    }
}
