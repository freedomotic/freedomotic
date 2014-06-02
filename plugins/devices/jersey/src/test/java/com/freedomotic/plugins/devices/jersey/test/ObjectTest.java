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
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.model.object.Representation;
import com.freedomotic.plugins.devices.japi.resources.ObjectResource;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author matteo
 */
public class ObjectTest extends AbstractTest<EnvObject> {


   Environment e;
   EnvironmentLogic el;

    
    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        e = new Environment();
        e.setName("Test env for zone");
        e.setUUID(uuid);
        
        el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(e);
        el.init();
        api.environments().create(el);
        
        item = new EnvObject();
        item.setName("TestObject");
        item.setUUID(uuid);
        item.setHierarchy("com.freedomotic.objects.impl.Gate");
        item.setType("EnvObject.Gate");
        item.setEnvironmentID(e.getUUID());
        Representation r = new Representation();
        r.setOffset(0, 0);
        r.setTangible(true);
        FreedomPolygon s = new FreedomPolygon();
        s.append(0, 0);
        s.append(0,1);
        s.append(1,1);
        s.append(1,0);
        r.setShape(s);
        item.getRepresentations().add(r);
        item.setCurrentRepresentation(0);
        RangedIntBehavior ri = new RangedIntBehavior();
        ri.setName("openness");
        ri.setMin(0);
        ri.setMax(100);
        ri.setScale(1);
        ri.setStep(1);
        BooleanBehavior b = new BooleanBehavior();
        b.setName("open");
        b.setValue(true);
        item.getBehaviors().add(b);
        item.getBehaviors().add(ri);
        initPath(ObjectResource.class);
        listType = new GenericType<List<EnvObject>>(){};
        singleType = new GenericType<EnvObject>(){};
    }

    @Override
    protected void putModifications(EnvObject orig) {
        orig.setActAs("virtual");
    }

    @Override
    protected void putAssertions(EnvObject pre, EnvObject post) {
        assertEquals("PUT - name check", pre.getName(), post.getName());
        assertEquals("PUT - ActAs check", pre.getActAs(), post.getActAs());
    }

    @Override
    protected void getAssertions(EnvObject obj) {
        assertEquals("Single test - UUID", item.getUUID(), obj.getUUID());
        assertEquals("Single test - NAME", item.getName(), obj.getName());
    }

    @Override
    protected void listAssertions(List<EnvObject> list) {
        assertEquals("UUID test", item.getUUID(), list.get(0).getUUID());
        assertEquals("Name test", item.getName(), list.get(0).getName());
    }

    @Override
    protected String getUuid(EnvObject obj) {
        return obj.getUUID();
    }
  
}
