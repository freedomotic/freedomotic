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

import static org.junit.Assert.assertEquals;

import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.testutils.GuiceJUnitRunner;
import com.google.inject.Inject;

/**
 *
 * @author P3trur0 https://flatmap.it
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class UserTest {

    @Inject
    private AppConfig config;
    @Inject
    private AuthImpl2 auth;

    @Before
    public void prepare() {
        config.load();
        auth.initBaseRealm();
        SimpleRole role = new SimpleRole();
        role.setName("administrators");
        role.add(new WildcardPermission("sys:*"));
        auth.addRole(role);
        auth.addUser("system", "cQY3W9HCU1MpYV16/SVKFeciDoxOOkyR9cgi/XUEHig=", "FZ4hpINh3HLLyYcgQAy/HTWcZbNX/7R3Tn/YYYsXpQ0=" , "administrators");
    }
    
    @Test
    public void rolesManagement() {
    	User user = auth.getUser("system");
    	assertEquals(1, user.getRoles().size());
    	user.removeRole("administrators");
    	assertEquals(0, user.getRoles().size());
    	user.addRole("administrators");
    	assertEquals(1, user.getRoles().size());
    }    
}
