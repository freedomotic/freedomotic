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

import com.freedomotic.plugins.devices.japi.resources.TriggerResource;
import com.freedomotic.reactions.Trigger;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;

/**
 *
 * @author matteo
 */
public class TriggerTest extends AbstractTest<Trigger> {

    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        container = api.triggers();
        item = new Trigger();
        item.setName("TestTrg");
        item.setUUID(uuid);
        item.setChannel("test.trigger.channel");
        initPath(TriggerResource.class);
        listType = new GenericType<List<Trigger>>() {
        };
        singleType = new GenericType<Trigger>() {
        };
    }

    @Override
    protected void putModifications(Trigger c2) {
        c2.setName("Modified Name");
    }

    @Override
    protected void putAssertions(Trigger pre, Trigger post) {
        assertEquals("PUT - name check", pre.getName(), post.getName());
    }

    @Override
    protected void getAssertions(Trigger c2) {
        assertEquals("Single test - UUID", item.getUUID(), c2.getUUID());
        assertEquals("Single test - NAME", item.getName(), c2.getName());
    }

    @Override
    protected void listAssertions(List<Trigger> cl) {
        assertEquals("UUID test", item.getUUID(), cl.get(0).getUUID());
        assertEquals("Name test", item.getName(), cl.get(0).getName());
    }

    @Override
    protected String getUuid(Trigger c) {
        return c.getUUID();
    }

}
