/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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
package com.freedomotic.plugins.devices.restapiv3.test;

import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.EnvironmentResource;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class ZoneTest extends AbstractTest<Zone>{

    @Inject
    Environment e ;
    
    @Inject
    EnvironmentLogic el;

    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        setItem(new Zone());
        getItem().setName("Test Zone");
        getItem().setAsRoom(true);
        getItem().setDescription("Before editing");
        e.setName("Test env for zone");
        e.setUUID(getUuid());
        el.setPojo(e);
        el.init();
        getApi().environments().create(el);
       
        initPath(UriBuilder.fromResource(EnvironmentResource.class).path(e.getUUID()).path("/rooms").build().toString());
        setListType(new GenericType<List<Zone>>(){});
        setSingleType(new GenericType<Zone>(){});        
    }

    @Override
    protected void putModifications(Zone orig) {
        orig.setName("New zone name");
    }

    @Override
    protected void putAssertions(Zone pre, Zone post) {
        assertEquals("PUT - name check", pre.getName(), post.getName());
    }

    @Override
    protected void getAssertions(Zone obj) {
        assertEquals("Single test - NAME", getItem().getName(), obj.getName());
    }

    @Override
    protected void listAssertions(List<Zone> list) {
         assertEquals("Name test", getItem().getName(), list.get(0).getName());
    }

    @Override
    protected String getUuid(Zone obj) {
        return obj.getUuid();
    }
    
}
