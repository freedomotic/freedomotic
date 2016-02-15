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
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.TriggerResource;
import com.freedomotic.reactions.Trigger;
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
public class TriggerTest extends AbstractTest<Trigger> {

    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        setItem(new Trigger());
        getItem().setName("TestTrg");
        getItem().setUUID(getUuid());
        getItem().setChannel("test.trigger.channel");
        initPath(TriggerResource.class);
        setListType(new GenericType<List<Trigger>>() {
        });
        setSingleType(new GenericType<Trigger>() {
        });
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
        assertEquals("Single test - UUID", getItem().getUUID(), c2.getUUID());
        assertEquals("Single test - NAME", getItem().getName(), c2.getName());
    }

    @Override
    protected void listAssertions(List<Trigger> cl) {
        assertEquals("UUID test", getItem().getUUID(), cl.get(0).getUUID());
        assertEquals("Name test", getItem().getName(), cl.get(0).getName());
    }

    @Override
    protected String getUuid(Trigger c) {
        return c.getUUID();
    }

}
