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
import com.freedomotic.model.environment.Environment;
import com.freedomotic.plugins.devices.restapiv3.resources.jersey.EnvironmentResource;
import com.google.inject.Inject;
import java.util.List;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.UriBuilderException;
import static org.junit.Assert.assertEquals;
import org.junit.runner.RunWith;

@RunWith(GuiceJUnitRunner.class)
@GuiceJUnitRunner.GuiceInjectors({FreedomoticInjector.class})
public class EnvironmentTest extends AbstractTest<Environment> {
 
    @Inject 
    private Environment env;
    
    @Override
    public void init() throws UriBuilderException, IllegalArgumentException {
        setItem(env);
        getItem().setName("TestEnv");
        getItem().setUUID(getUuid());
        initPath(EnvironmentResource.class);
        setListType(new GenericType<List<Environment>>(){});
        setSingleType(new GenericType<Environment>(){});
    }

    @Override
    protected void putModifications(Environment c2) {
        c2.setName("Modified Name");
    }

    @Override
    protected void putAssertions(Environment pre, Environment post) {
        assertEquals("PUT - name check", pre.getName(), post.getName());
    }

    @Override
    protected void getAssertions(Environment c2) {
        assertEquals("Single test - UUID", getItem().getUUID(), c2.getUUID());
        assertEquals("Single test - NAME", getItem().getName(), c2.getName());
    }

    @Override
    protected void listAssertions(List<Environment> cl) {
        assertEquals("UUID test", getItem().getUUID(), cl.get(0).getUUID());
        assertEquals("Name test", getItem().getName(), cl.get(0).getName());
    }

    @Override
    protected String getUuid(Environment c) {
        return c.getUUID();
    }
    
}
