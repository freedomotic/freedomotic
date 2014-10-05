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
public class PluginRuntimeException extends Exception {

    public PluginRuntimeException(String msg) {
        super(msg);
    }

    public PluginRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
