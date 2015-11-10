/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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

package com.freedomotic.restapi.server.resources;

import com.freedomotic.model.object.EnvObject;
import com.freedomotic.things.EnvObjectPersistence;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.restapi.server.interfaces.ObjectResource;
import org.restlet.data.Reference;
import org.restlet.resource.ServerResource;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */

public class ObjectServerResource extends ServerResource implements ObjectResource {

    private volatile EnvObject envObject;
    String name;

    @Override
    public void doInit() {
        name = Reference.decode((String) getRequest().getAttributes().get("name"));
        envObject = EnvObjectPersistence.getObjectByName(name).getPojo();
    }

    @Override
    public String retrieveXml() {
        String ret = "";
        XStream xstream = FreedomXStream.getXstream();
        ret = xstream.toXML(envObject);
        return ret;
    }

    @Override
    public String retrieveJson() {
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        ret = xstream.toXML(envObject);
        System.out.println("json: " + ret);
        return ret;
    }

    @Override
    public EnvObject retrieveObject() {
        return envObject;
    }
}
