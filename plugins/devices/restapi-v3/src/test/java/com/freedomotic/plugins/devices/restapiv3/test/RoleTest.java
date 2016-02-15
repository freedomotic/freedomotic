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
import com.freedomotic.plugins.devices.restapiv3.representations.RoleRepresentation;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.RoleResource;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class RoleTest extends AbstractTest<RoleRepresentation>{

    @Inject 
    RoleRepresentation role;
    
    @Override
    void init() throws UriBuilderException, IllegalArgumentException {
        setItem(role);
        getItem().setName("TestRole");
        getItem().getPermissions().add("read:*");
        initPath(RoleResource.class);
        setListType(new GenericType<List<RoleRepresentation>>(){});
        setSingleType(new GenericType<RoleRepresentation>(){});
    }

    @Override
    void putModifications(RoleRepresentation orig) {
        orig.getPermissions().clear();
        orig.getPermissions().add("write:*");
    }

    @Override
    void putAssertions(RoleRepresentation pre, RoleRepresentation post) {
        assertEquals("PUT - perms check", pre.getPermissions().get(0),  post.getPermissions().get(0));
    }

    @Override
    void getAssertions(RoleRepresentation obj) {
        assertEquals("GET - Name check", getItem().getName(), obj.getName());
    }

    @Override
    void listAssertions(List<RoleRepresentation> list) {
        assertEquals("LIST - get Name", list.get(0).getName(), getItem().getName());
    }

    @Override
    String getUuid(RoleRepresentation obj) {
        return obj.getName();
    }

 
}
