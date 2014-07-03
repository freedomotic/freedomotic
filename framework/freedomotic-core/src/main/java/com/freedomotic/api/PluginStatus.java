/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import java.util.EnumSet;

/**
 *
 * @author matteo
 */
public enum PluginStatus {

    /**
     *
     */
    STOPPED(0),
    /**
     *
     */
    STARTING(1),
    /**
     *
     */
    FAILED(2),
    /**
     *
     */
    RUNNING(3),
    /**
     *
     */
    STOPPING(4),
    /**
     *
     */
    INSTALLING(5),
    /**
     *
     */
    UNINSTALLING(6);

    private int code;
    private static final EnumSet<PluginStatus> allowedToStartStatuses = EnumSet.of(STOPPED);
    private static final EnumSet<PluginStatus> destroyStatuses = EnumSet.of(STOPPED, STOPPING);

    private PluginStatus(int code) {
        this.code = code;
    }

    public boolean isAllowedToStart() {
        return allowedToStartStatuses.contains(this);
    }
    
    public boolean isRunning(){
        return equals(RUNNING);
    }
}
