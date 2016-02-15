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

import com.freedomotic.restapi.model.PluginPojo;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author gpt
 */

public class PluginConverter implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        System.out.println("PluginConverter: " + o.toString());
        PluginPojo plug = (PluginPojo) o;
        writer.startNode("plugin");
        writer.setValue(plug.getName());
        writer.endNode();
        writer.startNode("running");
        writer.setValue(Boolean.toString(plug.isRunning()));
        writer.endNode(); //end sequences
    }

    @Override
    public boolean canConvert(Class type) {
        if (type == PluginPojo.class) {
            return true;
        }
        return false;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
