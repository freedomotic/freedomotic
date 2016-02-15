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
package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.plugins.devices.restapiv3.representations.UserRepresentation;
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
public class UserTest extends AbstractTest<UserRepresentation>{

    
    @Override
    void init() throws UriBuilderException, IllegalArgumentException {
        SimpleRole r = new SimpleRole();
        r.setName("admin");
        r.add(new WildcardPermission("*"));
        getApi().getAuth().addRole(r);
        
        User u = new User("user","password","admin",getApi().getAuth());
        setItem(new UserRepresentation(u));
        
        initPath(UserResource.class);
        setListType(new GenericType<List<UserRepresentation>>(){});
        setSingleType(new GenericType<UserRepresentation>(){});
        testDELETE = false;
    }

    @Override
    void putModifications(UserRepresentation orig) {
        orig.getProperties().setProperty("property", "value");
    }

    @Override
    void putAssertions(UserRepresentation pre, UserRepresentation post) {
        assertEquals("PUT - Check property",pre.getProperties().getProperty("property"),post.getProperties().getProperty("property"));
    }

    @Override
    void getAssertions(UserRepresentation obj) {
        assertEquals("GET - Name check", getItem().getName(), obj.getName());
    }

    @Override
    void listAssertions(List<UserRepresentation> list) {
        assertEquals("LIST - get Name", list.get(0).getName(), getItem().getName());
    }

    @Override
    String getUuid(UserRepresentation obj) {
        return obj.getName();
    }
    
}
