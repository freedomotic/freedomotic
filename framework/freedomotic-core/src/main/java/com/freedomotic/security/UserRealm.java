/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://freedomotic.com
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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.persistence.FreedomXStream;
import com.thoughtworks.xstream.XStream;

/**
 * An extension of the SimpleAccountRealm class defined in Shiro dependency. It
 * defines the Security Realm for Freedomotic users.
 * 
 * @see org.apache.shiro.realm.SimpleAccountRealm
 * 
 * @author Matteo Mazzoni
 */
public class UserRealm extends SimpleAccountRealm {

	/**
	 * Name of the Freedomotic Users realm
	 */
	public static final String USER_REALM_NAME = "com.freedomotic.security";
	private static final Logger LOG = LoggerFactory.getLogger(UserRealm.class.getCanonicalName());

	/**
	 * Builds an instance of UserRealm and instantiates an HashedCredentialMatcher
	 * 
	 * @return an instance of UserRealm
	 */
	public UserRealm() {
		setName(USER_REALM_NAME);
		HashedCredentialsMatcher matcher = new HashedCredentialsMatcher("SHA-256");
		matcher.setStoredCredentialsHexEncoded(false);
		setCredentialsMatcher(matcher);
	}

	/**
	 * Adds the account parameter to this realm
	 * 
	 * @param account
	 */
	public void addUser(User account) {
		super.add(account);
	}

	/**
	 * Adds the role parameter to this realm
	 * 
	 * @param role
	 */
	public void addRole(SimpleRole role) {
		super.add(role);
	}

	/**
	 * Removes the user identified by the username from this current realm
	 * 
	 * @param username
	 */
	public void removeUser(String username) {
		User u = getUser(username);
		u.setObjectPermissions(null);
		u.getRoles().clear();
		u.setCredentialsExpired(true);
		u.setLocked(true);
		users.remove(username);
	}

	/**
	 * Removes the role identified by the rolename from this current realm
	 * 
	 * @param rolename
	 */
	public void removeRole(String rolename) {
		for (User u : getUsers().values()) {
			u.removeRole(rolename);
			u.setObjectPermissions(null);
			for (String rs : u.getRoles()) {
				SimpleRole nr = getRole(rs);
				u.addObjectPermissions(nr.getPermissions());
			}
		}
		getRoles().remove(rolename);
	}

	/**
	 * Returns the User identified by the username passed as parameter.
	 * 
	 * @return User identified by the username
	 */
	@Override
	public User getUser(String username) {
		return (User) super.getUser(username);
	}

	/**
	 * Returns the SimpleRole identified by the rolename passed as parameter.
	 * 
	 * @return SimpleRole identified by the rolename
	 */
	@Override
	public SimpleRole getRole(String rolename) {
		return super.getRole(rolename);
	}

	/**
	 * Returns all the users currently defined for this UserRealm
	 * 
	 * @return the map of users
	 */
	public Map<String, User> getUsers() {
		HashMap<String, User> accounts = new HashMap<>();
		for (Map.Entry<String, SimpleAccount> user : users.entrySet()) {
			accounts.put(user.getKey(), (User) user.getValue());
		}
		return accounts;
	}

	/**
	 * Returns all the roles currently defined for this UserRealm
	 * 
	 * @return the map of roles
	 */
	public Map<String, SimpleRole> getRoles() {
		return roles;
	}
	
	/**
	 * Loads all the users and roles contained in the directory. <b>WARNING: the
	 * files in the directory must be names respectively <code>roles.xml</code> and
	 * <code>users.xml</code>
	 * 
	 * @param directory
	 *            containing the data
	 */
	public void load(File directory) {
		loadRoles(new File(directory, "roles.xml"));
		loadUsers(new File(directory, "users.xml"));
	}

	/**
	 * SAves all the users and roles of this realm in the directory. <b>WARNING: the
	 * files saved in the directory will be named respectively
	 * <code>roles.xml</code> and <code>users.xml</code>
	 * 
	 * @param directory
	 *            containing the data
	 */
	public void save(File directory) {
		saveUsers(new File(directory, "users.xml"));
		saveRoles(new File(directory, "roles.xml"));
	}

	/*
	 * Loads all the roles contained in the file and puts them in this realm
	 * 
	 * @param file
	 * @return 0 if the operation goes well
	 */
	private int loadRoles(File file) {
		roles.clear();
		XStream xstream = FreedomXStream.getXstream();
		SimpleRole[] ra = (SimpleRole[]) xstream.fromXML(file);
		for (SimpleRole r : ra) {
			roles.put(r.getName(), r);
		}
		return 0;
	}

	/*
	 * Persists all the roles of this realm in the file
	 * 
	 * @param file
	 *            where the roles are persisted
	 * @return true if the operation goes well
	 */
	private boolean saveRoles(File file) {
		SimpleRole[] ra = new SimpleRole[] {};
		ra = getRoles().values().toArray(ra);
		LOG.info("Serializing roles to \"{}\"", file);
		FreedomXStream.toXML(ra, file);
		return true;
	}

	/*
	 * Loads all the users contained in the file and puts them in this realm
	 * 
	 * @param file
	 * @return 0 if the operation goes well
	 */
	private int loadUsers(File file) {
		users.clear();
		XStream xstream = FreedomXStream.getXstream();
		User[] ua = (User[]) xstream.fromXML(file);
		for (User user : ua) {
			users.put(user.getPrincipals().getPrimaryPrincipal().toString(), user);
		}
		return 0;
	}

	/*
	 * Persists all the roles of this realm in the file
	 * 
	 * @param file
	 *            where the roles are persisted
	 * @return true if the operation goes well
	 * @throws IOException
	 */
	private boolean saveUsers(File file) {
		User[] ua = new User[] {};
		ua = getUsers().values().toArray(ua);
		LOG.info("Serializing users to \"{}\"", file);
		FreedomXStream.toXML(ua, file);

		return true;
	}
}
