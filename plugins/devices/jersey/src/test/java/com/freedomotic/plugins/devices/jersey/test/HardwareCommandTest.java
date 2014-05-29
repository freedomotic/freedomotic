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

import com.freedomotic.plugins.devices.japi.resources.HardwareCommandResource;
import com.freedomotic.reactions.Command;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;

public class HardwareCommandTest extends AbstractTest<Command>{

    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        item = new Command();
        item.setName("TestCmd");
        item.setUUID(uuid);
        container = api.commands();
        item.setProperty("prop1", "value1");
        item.setReceiver("receiver.channel");
        item.setHardwareLevel(true);
        initPath(HardwareCommandResource.class);
        listType = new GenericType<List<Command>>(){};
        singleType = new GenericType<Command>(){};

    }

    @Override
    protected void putModifications(Command c2) {
         c2.setName("Modified Name");
    }

    @Override
    protected void putAssertions(Command pre, Command post) {
         assertEquals("PUT - name check", pre.getName(), post.getName());
    }

    @Override
    protected void getAssertions(Command c2) {
assertEquals("Single test - UUID", item.getUUID(), c2.getUUID());
        assertEquals("Single test - NAME", item.getName(), c2.getName());
    }

    @Override
    protected void listAssertions(List<Command> cl) {
       assertEquals("UUID test", item.getUUID(), cl.get(0).getUUID());
       assertEquals("Name test", item.getName(), cl.get(0).getName());
    }

    @Override
    protected String getUuid(Command c) {
        return c.getUUID();
    }

    
}