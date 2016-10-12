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

import com.freedomotic.settings.AppConfig;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.google.inject.Inject;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.subject.Subject;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author Matteo Mazzoni
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class RealmTest {

    @Inject
    AppConfig config;
    @Inject
    AuthImpl2 auth;

    @Before
    public void prepare() {
        config.load();
        auth.initBaseRealm();
        SimpleRole role = new SimpleRole();
        role.setName("administrators");
        role.add(new WildcardPermission("sys:*"));
        auth.addRole(role);
        auth.addUser("system", "cQY3W9HCU1MpYV16/SVKFeciDoxOOkyR9cgi/XUEHig=", "FZ4hpINh3HLLyYcgQAy/HTWcZbNX/7R3Tn/YYYsXpQ0=" , "administrators");
        auth.getUser("system").setProperty("language", "auto");
    }

    @Test
    public void TestRole() {
        assertEquals(1, auth.getRoles().size());
        assertEquals("administrators", auth.getRole("administrators").getName());
        Permission[] p = new Permission[]{};
        p = auth.getRole("administrators").getPermissions().toArray(p);
        assertEquals(true, p[0].implies(new WildcardPermission("sys:*")));
    }

    @Test
    public void TestUserData() {
        assertEquals(1, auth.getUsers().size());
        assertEquals("auto", auth.getUser("system").getProperty("language"));
        assertEquals(null, auth.getUser("system").getProperty("pipp"));
    }

    @Test
    public void TestUserRoles() {
        PrincipalCollection principals = new SimplePrincipalCollection("system", UserRealm.USER_REALM_NAME);
        Subject SysSubject = new Subject.Builder().principals(principals).buildSubject();
        assertEquals("user is SYSTEM ", "system", SysSubject.getPrincipal());
        assertEquals("user has role ADMINISTRATOR ", true, SysSubject.hasRole("administrators"));
        assertEquals("user doesn't have role ADMINDSFSDFSEFR ", false, SysSubject.hasRole("admidsfsdfsefr"));
    }

    @Test
    public void TestLogin() {
        auth.logout();
        assertEquals("Testing anonymous user prior to authenticate", false, auth.getSubject().isAuthenticated());
        auth.login("system", "password", true);
        assertEquals("Testing if user is authenticated", true, auth.getSubject().isAuthenticated());
        assertEquals("Testing name of authenticated user", "system", auth.getSubject().getPrincipal());
        auth.logout();
        assertEquals(false, auth.getSubject().isAuthenticated());
    }

    @Test
    public void TestUserRoleToPermissions() {
        auth.logout();
        auth.login("system", "password", true);
        assertEquals("Checking whether 'system' user is permitted 'sys:*'", true, auth.getSubject().isPermitted("sys:*"));
        assertEquals("user is permitted 'sys:*' ", true, auth.getUser("system").isPermitted("sys:*"));
        assertEquals("user is not permitted '*' ", false, auth.getUser("system").isPermitted("*"));
        auth.logout();
    }
}
