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

import com.freedomotic.persistence.FreedomXStream;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.realm.SimpleAccountRealm;

/**
 *
 * @author Matteo Mazzoni
 */
public class UserRealm extends SimpleAccountRealm {

    private final static Logger LOG = LoggerFactory.getLogger(UserRealm.class.getCanonicalName());
    public final static String USER_REALM_NAME = "com.freedomotic.security";
    private final static String PASSWORD_HASHING_ALGORITHM = "SHA-256";

    public UserRealm() {
        setName(USER_REALM_NAME);
        HashedCredentialsMatcher matcher = new HashedCredentialsMatcher(PASSWORD_HASHING_ALGORITHM);
        matcher.setStoredCredentialsHexEncoded(false);
        setCredentialsMatcher(matcher);
    }

    public void addUser(User account) {
        super.add(account);
    }

    public void addRole(SimpleRole role) {
        super.add(role);
    }

    @Override
    public User getUser(String username) {
        return (User) super.getUser(username);
    }

    public Map<String, User> getUsers() {
        HashMap<String, User> accounts = new HashMap<String, User>();
        for (String userName : users.keySet()) {
            accounts.put(userName, (User) users.get(userName));
        }
        return accounts;
    }

    public Map<String, SimpleRole> getRoles() {
        return roles;
    }

    @Override
    public SimpleRole getRole(String rolename) {
        return super.getRole(rolename); //To change body of generated methods, choose Tools | Templates.
    }

    private int loadRoles(File file) {
        roles.clear();
        XStream xstream = FreedomXStream.getXstream();
        SimpleRole[] ra = (SimpleRole[]) xstream.fromXML(file);
        for (SimpleRole r : ra) {
            roles.put(r.getName(), r);
        }
        return 0;
    }

    private boolean saveRoles(File file) throws IOException {
        SimpleRole[] ra = new SimpleRole[]{};
        ra = getRoles().values().toArray(ra);
        LOG.info("Serializing roles to {}", file);
        FreedomXStream.toXML(ra, file);
        return true;
    }

    private int loadUsers(File file) {
        users.clear();
        XStream xstream = FreedomXStream.getXstream();
        User[] ua = (User[]) xstream.fromXML(file);
        for (User user : ua) {
            users.put(user.getPrincipals().getPrimaryPrincipal().toString(), user);
        }
        return 0;
    }

    private boolean saveUsers(File file) throws IOException {
        User[] ua = new User[]{};
        ua = getUsers().values().toArray(ua);
        LOG.info("Serializing users to {}", file);
        FreedomXStream.toXML(ua, file);

        return true;
    }

    public void load(File file) {
        loadRoles(new File(file + "/roles.xml"));
        loadUsers(new File(file + "/users.xml"));
    }

    public void save(File file) throws IOException {
        saveUsers(new File(file + "/users.xml"));
        saveRoles(new File(file + "/roles.xml"));
    }

    public void removeUser(String userName) {
        User u = getUser(userName);
        u.setObjectPermissions(null);
        u.getRoles().clear();
        u.setCredentialsExpired(true);
        u.setLocked(true);
        users.remove(userName);
    }

    public void removeRole(String roleName) {
        SimpleRole r = getRole(roleName);
        for (User u : getUsers().values()) {
            u.removeRole(roleName);
            u.setObjectPermissions(null);
            for (String rs : u.getRoles()) {
                SimpleRole nr = getRole(rs);
                u.addObjectPermissions(nr.getPermissions());
            }
        }
        getRoles().remove(roleName);
    }
}
