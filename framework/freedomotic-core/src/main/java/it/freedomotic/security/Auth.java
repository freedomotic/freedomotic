/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.security;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.Info;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.annotation.RequiresPermissions;
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
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class Auth {
    
    private static String BASEREALMNAME = "it.freedomotic.security";
    private static String PLUGINREALMNAME = "it.freedomotic.plugins.security";
    public static boolean realmInited = false;
    private static PropertiesRealm baseRealm = new PropertiesRealm();
    private static SimpleAccountRealm pluginRealm = new SimpleAccountRealm(PLUGINREALMNAME);
    private static String DEFAULT_PERMISSION = "*";
    private static ArrayList<Realm> realmCollection = new ArrayList<Realm>();
    
    public static void initBaseRealm() {
        DefaultSecurityManager securityManager = null;
        if (!realmInited && Freedomotic.config.getBooleanProperty("KEY_SECURITY_ENABLE", true)) {
            baseRealm.setName(BASEREALMNAME);
            baseRealm.setResourcePath(Info.getApplicationPath() + File.separator + "config" + File.separator + "security.properties");
            baseRealm.init();

            pluginRealm.init();

            securityManager = new DefaultSecurityManager();
            realmCollection.add(baseRealm);
            realmCollection.add(pluginRealm);
            securityManager.setRealms(realmCollection);
            
            realmInited = true;
        }
        SecurityUtils.setSecurityManager(securityManager);
    }

    public static boolean login(String subject, char[] password) {
        String pwdString = String.copyValueOf(password);
        return login(subject, pwdString);
    }

    public static boolean login(String subject, String password) {
        UsernamePasswordToken token = new UsernamePasswordToken(subject, password);
        token.setRememberMe(true);
        Subject currentUser = SecurityUtils.getSubject();
        try {
            currentUser.login(token);
            return true;
        } catch (Exception e) {
            Freedomotic.logger.warning(e.getLocalizedMessage());
            return false;
        }
    }

    public void logout() {
        Subject currentUser = SecurityUtils.getSubject();
        currentUser.logout();

    }

    public static boolean isPermitted(String permission) {
        if (realmInited) {
            return SecurityUtils.getSubject().isPermitted(permission);
        } else {
            return true;
        }
    }

    public static Subject getSubject() {
        if (realmInited) {
            return SecurityUtils.getSubject();
        } else {
            return null;
        }
    }

    public static Object getPrincipal() {
        if (realmInited) {
            return SecurityUtils.getSubject().getPrincipal();
        } else {
            return null;
        }
    }

    public static void pluginExecutePrivileged(Plugin plugin, Runnable action) {
        executePrivileged(plugin.getClassName(), action);
    }

    private static void executePrivileged(String classname, Runnable action) {
        if (Auth.realmInited) {
            //Freedomotic.logger.info("Executing privileged for plugin: " + classname);
            PrincipalCollection plugPrincipals = new SimplePrincipalCollection(classname, pluginRealm.getName());
            Subject plugSubject = new Subject.Builder().principals(plugPrincipals).buildSubject();
            plugSubject.execute(action);
        } else {
            action.run();
        }
    }

    public static void setPluginPrivileges(Plugin plugin, String permissions) {
        if (!pluginRealm.accountExists(plugin.getClassName())) {
            // check whether declared permissions correspond the ones requested at runtime
            if (plugin.getConfiguration().getStringProperty("permissions", getPluginDefaultPermission()).equals(permissions)){
            Freedomotic.logger.info("Setting permissions for plugin " + plugin.getClassName() + ": " + permissions);
            String plugrole = UUID.randomUUID().toString();
            
            pluginRealm.addAccount(plugin.getClassName(), UUID.randomUUID().toString(), plugrole);
            pluginRealm.addRole(plugrole + "=" + permissions);

            } else {
                Freedomotic.logger.severe("Plugin "+plugin.getName() +" tried to request incorrect privileges" );
            }
        }
    }
    
    @Deprecated
    public static String getPluginDefaultPermission(){
        return DEFAULT_PERMISSION;
    }
    
    @RequiresPermissions("auth:realms:create") 
    public static void addRealm(Realm rm){
        if (!realmCollection.contains(rm)){
            realmCollection.add(rm);
        }
    }
    
    @RequiresPermissions("auth:realms:delete") 
    public static void deleteRealm(Realm rm){
        if (!rm.equals(baseRealm) && !rm.equals(pluginRealm)){
                realmCollection.remove(rm);
        }
    }
    
    @RequiresPermissions("auth:fakeUser")
    public static boolean bindFakeUser(String userName){
        if (baseRealm.accountExists(userName)) {
            PrincipalCollection principals = new SimplePrincipalCollection(userName, BASEREALMNAME);
            Subject subj = new Subject.Builder().principals(principals).buildSubject();
            ThreadState threadState = new SubjectThreadState(subj);
            threadState.bind();
            return true;
        }
        return false;
    }
}
