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

import com.freedomotic.api.Plugin;
import java.util.Map;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.Subject;

/**
 *
 * @author Matteo Mazzoni
 */
public interface Auth {

    /**
     *
     */
    void initBaseRealm();

    /**
     *
     * @return
     */
    boolean isInited();

    /**
     *
     * @param key
     * @return
     */
    boolean isPermitted(String key);

    /**
     *
     * @param subject
     * @param password
     * @return
     */
    boolean login(String subject, char[] password, boolean rememberMe);

    /**
     *
     * @param subject
     * @param password
     * @param rememberMe
     * @return
     */
    boolean login(String subject, String password, boolean rememberMe);

    /**
     *
     */
    void logout();

    /**
     *
     * @return
     */
    Subject getSubject();

    /**
     *
     * @return
     */
    Object getPrincipal();

    /**
     *
     * @param plugin
     * @param action
     */
    Runnable pluginBindRunnablePrivileges(Plugin plugin, Runnable action);

    /**
     *
     * @param plugin
     * @param permissions
     */
    void setPluginPrivileges(Plugin plugin, String permissions);

    /**
     *
     * @return
     */
    String getPluginDefaultPermission();

    /**
     *
     * @param rm
     */
    @RequiresPermissions("auth:realms:create")
    void addRealm(Realm rm);

    /**
     *
     * @param userName
     * @return
     */
    @RequiresPermissions("auth:fakeUser")
    boolean bindFakeUser(String userName);

    void load();

    void save();

    /**
     * Adds a new user to the realm.
     * @param userName
     * @param password hashed password base64 encoded
     * @param salt base64 encoded random salt
     * @param role user role
     * @return true if the user is added or false if the user already exists
     */
    @RequiresPermissions("auth:users:create")
    boolean addUser(String userName, String password, String salt, String role);

    @RequiresPermissions("auth:roles:create")
    boolean addRole(SimpleRole role);

    User getCurrentUser();

    @RequiresPermissions("auth:users:read")
    User getUser(String username);

    @RequiresPermissions("auth:users:read")
    Map<String, User> getUsers();

    @RequiresPermissions("auth:roles:read")
    SimpleRole getRole(String name);

    @RequiresPermissions("auth:roles:read")
    Map<String, SimpleRole> getRoles();

    @RequiresPermissions("auth:users:delete")
    boolean deleteUser(String userName);

    @RequiresPermissions("auth:roles:delete")
    boolean deleteRole(String roleName);

    Realm getUserRealm();
}
