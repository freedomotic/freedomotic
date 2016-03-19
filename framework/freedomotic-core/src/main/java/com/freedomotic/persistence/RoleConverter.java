/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.persistence;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.slf4j.Logger;

/**
 *
 * @author Matteo Mazzoni
 */
class RoleConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(RoleConverter.class.getName());

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {

        SimpleRole r = (SimpleRole) o;

        writer.addAttribute("name", r.getName());
        writer.startNode("permissions");
        for (Permission p : r.getPermissions()) {
            WildcardPermission wp = (WildcardPermission) p;
            writer.startNode("permission");
            writer.setValue(wp.toString().replace("[", "").replace("]", "").replace(" ", ""));
            writer.endNode();
        }
        writer.endNode();

    }

    /**
     *
     * @param reader
     * @param uc
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {

        SimpleRole r = new SimpleRole();

        r.setName(reader.getAttribute("name"));
        reader.moveDown();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            r.add(new WildcardPermission(reader.getValue()));
            reader.moveUp();
        }
        reader.moveUp();

        return r;
    }

    /**
     *
     * @param type
     * @return
     */
    @Override
    public boolean canConvert(Class type) {
        return (type == SimpleRole.class);
    }
}
