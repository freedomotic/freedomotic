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
    LOADED,
    INITED,
    STOPPED,
    STARTING,
    FAILED,
    RUNNING,
    STOPPING,
    INSTALLING,
    UNINSTALLING;
    
    //eg: not allowed to start if it is already RUNNING
    private static final EnumSet<PluginStatus> allowedToStartStatuses = EnumSet.of(STOPPED, FAILED);
    //Currently unused
    private static final EnumSet<PluginStatus> destroyStatuses = EnumSet.of(STOPPED, STOPPING);

    public static boolean isAllowedToStart(PluginStatus currentStatus) {
        return allowedToStartStatuses.contains(currentStatus);
    }
    
}
