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
    LOADED(0),
    /**
     *
     */
    INITED(1),
    /**
     *
     */
    STOPPED(2),
    /**
     *
     */
    STARTING(3),
    /**
     *
     */
    FAILED(4),
    /**
     *
     */
    RUNNING(5),
    /**
     *
     */
    STOPPING(6),
    /**
     *
     */
    INSTALLING(7),
    /**
     *
     */
    UNINSTALLING(8);
    private static Throwable throwable;

    private final int code;
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
