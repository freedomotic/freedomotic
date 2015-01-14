/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.restapiv3.representations;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author matteo
 */
@XmlRootElement
public class PermissionCheckRepresentation {
    private final String userName;
    private final String permission;
    private final boolean allowed;

    public PermissionCheckRepresentation(String userName, String permission, boolean allowed) {
        this.userName = userName;
        this.permission = permission;
        this.allowed = allowed;
    }

    public String getUserName() {
        return userName;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isAllowed() {
        return allowed;
    }
    
    
}
