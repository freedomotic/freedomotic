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

import com.freedomotic.model.object.Properties;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriel Pulido de Torres
 */
class PropertiesConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(PropertiesConverter.class.getName());

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Properties rea = (Properties) o;

        for (String name : rea.stringPropertyNames()) {
            writer.startNode("property");
            writer.addAttribute("name", name);
            writer.addAttribute("value",
                    rea.getProperty(name));
            writer.endNode();
        }
    }

    /**
     *
     * @param reader
     * @param uc
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        HashMap<String, String> propertiesHashMap = new HashMap<String, String>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            propertiesHashMap.put(reader.getAttribute("name"),
                    reader.getAttribute("value"));
            reader.moveUp();
        }

        return new Properties(propertiesHashMap);
    }

    /**
     *
     * @param type
     * @return
     */
    @Override
    public boolean canConvert(Class type) {
        if (type == Properties.class) {
            return true;
        }

        return false;
    }
}
