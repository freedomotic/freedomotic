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
package com.freedomotic.plugins.devices.jersey.test;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.plugins.devices.japi.resources.EnvironmentResource;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author matteo
 */
public class ZoneTest extends AbstractTest<Zone>{

    Environment e ;
    EnvironmentLogic el;


    
    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        container = api.environments();
        item = new Zone();
        item.setName("Test Zone");
        item.setAsRoom(true);
        item.setDescription("Before editing");
        e = new Environment();
        e.setName("Test env for zone");
        e.setUUID(uuid);
        el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(e);
        el.init();
        //EnvironmentPersistence.add(el, false);
        api.environments().create(el);
       
        initPath(UriBuilder.fromResource(EnvironmentResource.class).path(e.getUUID()).path("/rooms").build().toString());
        listType = new GenericType<List<Zone>>(){};
        singleType = new GenericType<Zone>(){};        
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
        assertEquals("Single test - NAME", item.getName(), obj.getName());
    }

    @Override
    protected void listAssertions(List<Zone> list) {
         assertEquals("Name test", item.getName(), list.get(0).getName());
    }

    @Override
    protected String getUuid(Zone obj) {
        return obj.getUuid();
    }
    
}
