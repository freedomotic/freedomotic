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
import com.freedomotic.settings.AppConfig;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.apache.shiro.realm.text.PropertiesRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;

/**
 *
 * @author Matteo Mazzoni
 */
class AuthImpl implements Auth {

    private final static String BASE_REALM_NAME = "com.freedomotic.security";
    private final static String PLUGIN_REALM_NAME = "com.freedomotic.plugins.security";
    private boolean realmInited = false;
    private PropertiesRealm baseRealm = new PropertiesRealm();
    private SimpleAccountRealm pluginRealm = new SimpleAccountRealm(PLUGIN_REALM_NAME);
    private String DEFAULT_PERMISSION = "*";
    private ArrayList<Realm> realmCollection = new ArrayList<Realm>();
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
            baseRealm.setName(BASE_REALM_NAME);
            baseRealm.setResourcePath(new File(Info.PATHS.PATH_WORKDIR + "/config/security.properties").getAbsolutePath());
            baseRealm.init();

            pluginRealm.init();

            securityManager = new DefaultSecurityManager();
            //securityManager = injector.getInstance(DefaultSecurityManager.class);

            realmCollection.add(baseRealm);
            realmCollection.add(pluginRealm);
            securityManager.setRealms(realmCollection);

            realmInited = true;
        }
        SecurityUtils.setSecurityManager(securityManager);
    }

    /**
     *
     * @param subject
     * @param password
     * @return
     */
    @Override
    public boolean login(String subject, char[] password, boolean rememberMe) {
        String pwdString = String.copyValueOf(password);
        return login(subject, pwdString, rememberMe);
    }

    /**
     *
     * @param subject
     * @param password
     * @return
     */
    @Override
    public boolean login(String subject, String password, boolean rememberMe) {
        UsernamePasswordToken token = new UsernamePasswordToken(subject, password);
        token.setRememberMe(rememberMe);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
            currentUser.getSession().setTimeout(-1);
            return true;
        } catch (Exception e) {
            LOG.warn(e.getLocalizedMessage());
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
    public Runnable pluginBindRunnablePrivileges(Plugin plugin, Runnable action) {
        return executePrivileged(plugin.getClassName(), action);
    }

    private Runnable executePrivileged(String classname, Runnable action) {
        if (isInited()) {
            //LOG.info("Executing privileged for plugin: " + classname);
            PrincipalCollection plugPrincipals = new SimplePrincipalCollection(classname, pluginRealm.getName());
            Subject plugSubject = new Subject.Builder().principals(plugPrincipals).buildSubject();
            plugSubject.getSession().setTimeout(-1);
            plugSubject.execute(action);
        } else {
            action.run();
        }
        return null;
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
                LOG.info("Setting permissions for plugin {}: {}", new Object[]{plugin.getClassName(), permissions});
                String plugrole = UUID.randomUUID().toString();

                pluginRealm.addAccount(plugin.getClassName(), UUID.randomUUID().toString(), plugrole);
                pluginRealm.addRole(plugrole + "=" + permissions);
            } else {
                LOG.error("Plugin {} tried to request incorrect privileges", plugin.getName());
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
        return DEFAULT_PERMISSION;
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
            PrincipalCollection principals = new SimplePrincipalCollection(userName, BASE_REALM_NAME);
            Subject subj = new Subject.Builder().principals(principals).buildSubject();
            ThreadState threadState = new SubjectThreadState(subj);
            threadState.bind();
            return true;
        }
        return false;
    }
    private static final Logger LOG = LoggerFactory.getLogger(AuthImpl.class.getName());

    @Override
    public void load() {
    }

    @Override
    public void save() {
    }

    @Override
    public boolean addUser(String userName, String password, String salt, String role) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean addRole(SimpleRole role) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getCurrentUser() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, User> getUsers() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SimpleRole getRole(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<String, SimpleRole> getRoles() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public User getUser(String username) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean deleteUser(String userName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Realm getUserRealm() {
        return baseRealm;
    }

    @Override
    public boolean deleteRole(String roleName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
