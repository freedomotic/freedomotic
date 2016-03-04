/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.security;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;

/**
 *
 * @author matteo
 */
public final class User extends SimpleAccount {

    private static final Logger LOG = Logger.getLogger(User.class.getName());

    private final Properties properties = new Properties();

    private final Auth auth;

    /**
     *
     * @param principal
     * @param credentials
     * @param roleName
     * @param auth
     */
    public User(Object principal, Object credentials, String roleName, Auth auth) {
        this(principal, credentials, auth);
        addRole(roleName);
    }

    /**
     *
     * @param principal
     * @param credentials
     * @param auth
     */
    public User(Object principal, Object credentials, Auth auth) {
        super(principal, credentials, UserRealm.USER_REALM_NAME);
        this.auth = auth;
    }

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     *
     * @param key
     * @param value
     * @return
     */
    public Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    /**
     *
     * @return
     */
    public String getName() {
        return getPrincipals().getPrimaryPrincipal().toString();
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<String> getRoles() {
        return super.getRoles();
    }

    /**
     *
     * @param roles
     */
    @Override
    public void setRoles(Set<String> roles) {
        for (String roleName : roles) {
            SimpleRole role = auth.getRole(roleName);
            if (role != null) {
                addObjectPermissions(role.getPermissions());
            }
        }
        super.setRoles(roles);

    }

    /**
     *
     * @param password
     */
    public void setPassword(String password) {
        setCredentials(password);
    }

    /**
     *
     * @param roleName
     */
    @Override
    public void addRole(String roleName) {
        SimpleRole role = auth.getRole(roleName);
        if (role != null) {
            super.addRole(role.getName());
            addObjectPermissions(role.getPermissions());
            LOG.log(Level.INFO, "Adding role {0} to user {1}: {2}", new Object[]{role.getName(), getName(), role.getPermissions()});
        } else {
            LOG.log(Level.SEVERE, "Cannot find role: {0}", roleName);
        }
    }

    /**
     *
     * @param roleName
     */
    public void removeRole(String roleName) {
        try {
            getObjectPermissions().removeAll(auth.getRole(roleName).getPermissions());
        } catch (Exception e) {

        }
        getRoles().remove(roleName);
    }

    /**
     *
     * @param perm
     * @return
     */
    public boolean isPermitted(String perm) {
        for (Permission p : this.getObjectPermissions()) {
            if (p.implies(new WildcardPermission(perm))) {
                return true;
            }
        }
        return false;
    }
}
