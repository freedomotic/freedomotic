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

package com.freedomotic.restapi.server.resources;

import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.restapi.server.interfaces.TriggersResource;
import java.util.ArrayList;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

/**
 *
 * @author gpt
 */

public class TriggersServerResource extends ServerResource implements TriggersResource {

    private static volatile ArrayList<Trigger> triggers;

    @Override
    protected void doInit() throws ResourceException {
        triggers = TriggerPersistence.getTriggers();
    }

    @Override
    public String retrieveXml() {
        String ret = "";
        XStream xstream = FreedomXStream.getXstream();
        ret = xstream.toXML(triggers);
        return ret;
    }

    @Override
    public String retrieveJson() {
        String ret = "";
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
        xstream.setMode(XStream.NO_REFERENCES);
        ret = xstream.toXML(triggers);
        return ret;
    }
}
