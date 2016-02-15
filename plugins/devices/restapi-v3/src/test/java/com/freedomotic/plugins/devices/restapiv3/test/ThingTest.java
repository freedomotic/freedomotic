/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.BooleanBehavior;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.RangedIntBehavior;
import com.freedomotic.model.object.Representation;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.ThingResource;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author matteo
 */
@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class ThingTest extends AbstractTest<EnvObject> {

    @Inject
    Environment e;

    @Inject
    EnvironmentLogic el;

    @Inject
    EnvObject obj;

    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        e.setName("Test env for zone");
        e.setUUID(getUuid());

        el.setPojo(e);
        el.init();
        getApi().environments().create(el);

        setItem(obj);
        getItem().setName("TestObject");
        getItem().setUUID(getUuid());
        getItem().setHierarchy("com.freedomotic.things.impl.ElectricDevice");
        getItem().setType("EnvObject.ElectricDevice");
        getItem().setEnvironmentID(e.getUUID());
        Representation r = new Representation();
        r.setOffset(0, 0);
        r.setTangible(true);
        FreedomPolygon s = new FreedomPolygon();
        s.append(0, 0);
        s.append(0, 1);
        s.append(1, 1);
        s.append(1, 0);
        r.setShape(s);
        getItem().getRepresentations().add(r);
        getItem().setCurrentRepresentation(0);
        BooleanBehavior b = new BooleanBehavior();
        b.setName("powered");
        b.setValue(true);
        getItem().getBehaviors().add(b);
        initPath(ThingResource.class);
        setListType(new GenericType<List<EnvObject>>() {
        });
        setSingleType(new GenericType<EnvObject>() {
        });
    }

    @Override
    protected void putModifications(EnvObject orig) {
        orig.setActAs("virtual");
        RangedIntBehavior ri = new RangedIntBehavior();
        ri.setName("power_consumption");
        ri.setMin(0);
        ri.setMax(100);
        ri.setScale(1);
        ri.setStep(1);
        orig.getBehaviors().add(ri);
    }

    @Override
    protected void putAssertions(EnvObject pre, EnvObject post) {
        assertEquals("PUT - name check", pre.getName(), post.getName());
        assertEquals("PUT - ActAs check", pre.getActAs(), post.getActAs());
        assertEquals("PUT - Consumption", pre.getBehavior("power_consumption").getName(), post.getBehavior("power_consumption").getName());
    }

    @Override
    protected void getAssertions(EnvObject obj) {
        assertEquals("Single test - UUID", getItem().getUUID(), obj.getUUID());
        assertEquals("Single test - NAME", getItem().getName(), obj.getName());
    }

    @Override
    protected void listAssertions(List<EnvObject> list) {
        assertEquals("UUID test", getItem().getUUID(), list.get(0).getUUID());
        assertEquals("Name test", getItem().getName(), list.get(0).getName());
    }

    @Override
    protected String getUuid(EnvObject obj) {
        return obj.getUUID();
    }

    @Test
    public void testPositionChange() {
        init();
        Entity<EnvObject> cmdEntity = Entity.entity(getItem(), getRepresentation());

        final Response response1 = target(getPATH()).request().post(cmdEntity);
        assertEquals("POST response HTTP status code not as expected", Response.Status.CREATED.getStatusCode(), response1.getStatus());

        final Response response = target(getPATH()).path(getUuid(getItem()) + "/move/10/10").request().post(null);
        assertEquals("Move POST response HTTP status code not as expected", Response.Status.ACCEPTED.getStatusCode(), response.getStatus());
        EnvObject objPre = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).get(getSingleType());
        assertEquals("X position after move", 10, objPre.getRepresentations().get(0).getOffset().getX());
        assertEquals("Y position after move", 10, objPre.getRepresentations().get(0).getOffset().getY());
        
      //  Response resDELETE = target(getPATH()).path(getUuid(getItem())).request(getRepresentation()).delete();
      //  assertEquals("DELETE test", Response.Status.OK.getStatusCode(), resDELETE.getStatus());

    }

}
