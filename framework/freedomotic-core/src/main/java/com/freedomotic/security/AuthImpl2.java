/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.app.AppConfig;
import com.freedomotic.util.Info;
import com.google.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
class AuthImpl2 implements Auth {

    private static final Logger LOG = Logger.getLogger(AuthImpl2.class.getName());
    private static boolean realmInited = false;
    private static final UserRealm baseRealm = new UserRealm();
    private static final PluginRealm pluginRealm = new PluginRealm();
    private static final ArrayList<Realm> realmCollection = new ArrayList<Realm>();
    @Inject
    AppConfig config;

    /**
     *
     * @return
     */
    @Override
    public boolean isInited() {
        return realmInited;
    }

    /**
     *
     */
    @Override
    public void initBaseRealm() {
        DefaultSecurityManager securityManager = null;
        if (!realmInited && config.getBooleanProperty("KEY_SECURITY_ENABLE", true)) {
            baseRealm.init();
            pluginRealm.init();

            securityManager = new DefaultSecurityManager();
            //securityManager = injector.getInstance(DefaultSecurityManager.class);

            realmCollection.add(baseRealm);
            realmCollection.add(pluginRealm);
            securityManager.setRealms(realmCollection);

            SecurityUtils.setSecurityManager(securityManager);
            realmInited = true;
        }

    }

    /**
     *
     * @param subject
     * @param password
     * @return
     */
    @Override
    public boolean login(String subject, char[] password) {
        String pwdString = String.copyValueOf(password);
        return login(subject, pwdString);
    }

    /**
     *
     * @param subject
     * @param password
     * @return
     */
    @Override
    public boolean login(String subject, String password) {
        UsernamePasswordToken token = new UsernamePasswordToken(subject, password);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
            currentUser.getSession().setTimeout(-1);
            return true;
        } catch (Exception e) {
            LOG.warning(e.getLocalizedMessage());
            return false;
        }
    }

    /**
     *
     */
    @Override
    public void logout() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();
    }

    /**
     *
     * @param permission
     * @return
     */
    @Override
    public boolean isPermitted(String permission) {
        if (realmInited) {
            return SecurityUtils.getSubject().isPermitted(permission);
        } else {
            return true;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Subject getSubject() {
        if (isInited()) {
            return SecurityUtils.getSubject();
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    @Override
    public Object getPrincipal() {
        if (isInited()) {
            return SecurityUtils.getSubject().getPrincipal();
        } else {
            return null;
        }
    }

    /**
     *
     * @param plugin
     * @param action
     */
    @Override
    public void pluginExecutePrivileged(Plugin plugin, Runnable action) {
        executePrivileged(plugin.getClassName(), action);
    }

    private void executePrivileged(String classname, Runnable action) {
        if (isInited()) {
            //LOG.info("Executing privileged for plugin: " + classname);
            PrincipalCollection plugPrincipals = new SimplePrincipalCollection(classname, pluginRealm.getName());
            Subject plugSubject = new Subject.Builder().principals(plugPrincipals).authenticated(true).buildSubject();
            plugSubject.getSession().setTimeout(-1);
            plugSubject.execute(action);
        } else {
            action.run();
        }
    }

    /**
     *
     * @param plugin
     * @param permissions
     */
    @Override
    public void setPluginPrivileges(Plugin plugin, String permissions) {

        if (!pluginRealm.accountExists(plugin.getClassName())) {
            // check whether declared permissions correspond the ones requested at runtime
            if (plugin.getConfiguration().getStringProperty("permissions", getPluginDefaultPermission()).equals(permissions)) {
                LOG.log(Level.INFO, "Setting permissions for plugin {0}: {1}", new Object[]{plugin.getClassName(), permissions});
                pluginRealm.addPlugin(plugin.getClassName(), permissions);
                //pluginRealm.addAccount(plugin.getClassName(), UUID.randomUUID().toString(), plugrole);
                //pluginRealm.addRole(plugrole);

            } else {
                LOG.log(Level.SEVERE, "Plugin {0} tried to request incorrect privileges", plugin.getName());
            }
        }
    }

    /**
     *
     * @return @deprecated
     */
    @Deprecated
    @Override
    public String getPluginDefaultPermission() {
        return PluginRealm.DEFAULT_PERMISSION;
    }

    /**
     *
     * @param rm
     */
    @Override
    public void addRealm(Realm rm) {
        if (!realmCollection.contains(rm)) {
            realmCollection.add(rm);
        }
    }

    /**
     *
     * @param rm
     */
    public void deleteRealm(Realm rm) {
        if (!rm.equals(baseRealm) && !rm.equals(pluginRealm)) {
            realmCollection.remove(rm);
        }
    }

    /**
     *
     * @param userName
     * @return
     */
    @Override
    public boolean bindFakeUser(String userName) {
        if (baseRealm.accountExists(userName)) {
            PrincipalCollection principals = new SimplePrincipalCollection(userName, UserRealm.USER_REALM_NAME);
            Subject subj = new Subject.Builder().principals(principals).buildSubject();
            ThreadState threadState = new SubjectThreadState(subj);
            threadState.bind();
            return true;
        }
        return false;
    }

    @Override
    public boolean addUser(String userName, String password, String role) {
        if (getUser(userName) == null) {
            baseRealm.addUser(new User(userName, password, role, this));
            return true;
        }
        return false;
    }

    @Override
    public boolean addRole(SimpleRole role) {
        if (getRole(role.getName()) == null) {
            baseRealm.addRole(role);
            return true;
        }
        return false;
    }

    @Override
    public User getCurrentUser() {
        return (User) baseRealm.getUser(getSubject().getPrincipal().toString());
    }

    @Override
    public Map<String, User> getUsers() {
        return baseRealm.getUsers();
    }

    @Override
    public SimpleRole getRole(String name) {
        return baseRealm.getRole(name);
    }

    @Override
    public Map<String, SimpleRole> getRoles() {
        return baseRealm.getRoles();
    }

    @Override
    public void save() {
        try {
            baseRealm.save(Info.PATHS.PATH_CONFIG_FOLDER);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void load() {
        baseRealm.load(Info.PATHS.PATH_CONFIG_FOLDER);
    }

    @Override
    public User getUser(String username) {
        return getUsers().get(username);
    }

    @Override
    public boolean deleteUser(String userName) {
        if (getUser(userName) != null) {
            baseRealm.removeUser(userName);
            return true;
        }
        return false;
    }

    @Override
    public Realm getUserRealm() {
        return baseRealm;
    }

    @Override
    public boolean deleteRole(String roleName) {
        if (getRole(roleName) != null) {
            baseRealm.removeRole(roleName);
            if (getRole(roleName) == null) {
                return true;
            }
        }
        return false;
    }

}
