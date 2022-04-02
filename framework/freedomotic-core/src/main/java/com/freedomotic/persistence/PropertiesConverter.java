/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

/**
 * Manages the serialization of Properties objects.
 * 
 * @author Enrico Nicoletti
 */
class PropertiesConverter implements Converter {

    /**
     *{@inheritDoc}}
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
     *{@inheritDoc}}
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        HashMap<String, String> propertiesHashMap = new HashMap<>();

        while (reader.hasMoreChildren()) {
            reader.moveDown();
            propertiesHashMap.put(reader.getAttribute("name"),
                    reader.getAttribute("value"));
            reader.moveUp();
        }

        return new Properties(propertiesHashMap);
    }

    /**
     *{@inheritDoc}}
     */
    @Override
    public boolean canConvert(Class type) {
        return (type == Properties.class);
    }
}
