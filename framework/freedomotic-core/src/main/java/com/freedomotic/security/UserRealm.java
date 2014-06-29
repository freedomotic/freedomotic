/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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

package com.freedomotic.security;

import com.freedomotic.persistence.FreedomXStream;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.realm.SimpleAccountRealm;

/**
 *
 * @author matteo
 */
public class UserRealm extends SimpleAccountRealm {
    
    private final static Logger LOG = Logger.getLogger(UserRealm.class.getCanonicalName());
    public final static String USER_REALM_NAME = "com.freedomotic.security";
    
    public UserRealm(){
        setName(USER_REALM_NAME);
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
        HashMap<String,User> accounts = new HashMap<String,User>();
        for (String userName : users.keySet()){
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

    private int loadRoles( File file) {
        roles.clear();
        XStream xstream = FreedomXStream.getXstream();
        SimpleRole[] ra = (SimpleRole[]) xstream.fromXML(file);
        for (SimpleRole r : ra){
            roles.put(r.getName(), r);
        }
       return 0;
    }

    
    private boolean saveRoles( File file) throws IOException{
        XStream xstream = FreedomXStream.getXstream();
        SimpleRole[] ra = new SimpleRole[]{};
        ra = getRoles().values().toArray(ra);
        
        String xml = xstream.toXML(ra);
        FileWriter fstream;
        BufferedWriter out = null;

        try {
            LOG.info("Serializing roles to " + file);
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            out.write(xml);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
        return true;
    }

    
    private int loadUsers( File file) {
        users.clear();
        XStream xstream = FreedomXStream.getXstream();
        User[] ua = (User[]) xstream.fromXML(file);
        for (User user : ua){
            users.put(user.getPrincipals().getPrimaryPrincipal().toString(), user);
        }
        return 0;
    }

    private boolean saveUsers( File file) throws IOException {
       XStream xstream = FreedomXStream.getXstream();
        User[] ua = new User[]{};
        ua = getUsers().values().toArray(ua);
        String xml = xstream.toXML(ua);
        FileWriter fstream;
        BufferedWriter out = null;

        try {
            LOG.info("Serializing users to " + file);
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            out.write(xml);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
        return true;
    }

    public void load(File file){
        loadRoles(new File(file+"/roles.xml"));
        loadUsers(new File(file+"/users.xml"));
    }
    
    public void save(File file) throws IOException{
        saveUsers(new File(file+"/users.xml"));
        saveRoles(new File(file+"/roles.xml"));
    }
    
    public void removeUser(String userName){
        User u = getUser(userName);
        u.setObjectPermissions(null);
        u.setRoles(null);
        u.setCredentialsExpired(true);
        u.setLocked(true);
        getUsers().remove(userName);
    }
}
