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
package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.UserResource;
import com.freedomotic.security.User;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class UserTest extends AbstractTest<User>{

    @Override
    public void test() {
    // tests for this class are currently disabled. Remove this method to re-enable them.
    }

    
    @Override
    void init() throws UriBuilderException, IllegalArgumentException {
        SimpleRole r = new SimpleRole();
        r.setName("admin");
        r.add(new WildcardPermission("*"));
        getApi().getAuth().addRole(r);
        
        User u = new User("user","password","admin",getApi().getAuth());
        setItem(u);
        
        initPath(UserResource.class);
        setListType(new GenericType<List<User>>(){});
        setSingleType(new GenericType<User>(){});
    }

    @Override
    void putModifications(User orig) {
        orig.setProperty("property", "value");
    }

    @Override
    void putAssertions(User pre, User post) {
        assertEquals("PUT - Check property",pre.getProperty("property"),post.getProperty("property"));
    }

    @Override
    void getAssertions(User obj) {
        assertEquals("GET - Name check", getItem().getName(), obj.getName());
    }

    @Override
    void listAssertions(List<User> list) {
        assertEquals("LIST - get Name", list.get(0).getName(), getItem().getName());
    }

    @Override
    String getUuid(User obj) {
        return obj.getName();
    }
    
}
