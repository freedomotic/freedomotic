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

import com.freedomotic.model.ds.Tuples;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class TupleConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(TupleConverter.class.getName());

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Tuples t = (Tuples) o;

        for (int i = 0; i < t.size(); i++) {
            HashMap<String, String> properties = t.getTuple(i);
            Set<Map.Entry<String, String>> entrySet = properties.entrySet();
            writer.startNode("tuple");
            for (Map.Entry<String, String> entry : entrySet) {
                writer.startNode("property");
                // TODO unnecessary explicit .toString() invocation
                writer.addAttribute("name", entry.getKey().toString());
                writer.addAttribute("value", entry.getValue().toString());
                writer.endNode();
            }

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
        Tuples t = new Tuples();

        //starts from root <tuples>
        while (reader.hasMoreChildren()) {
            reader.moveDown(); //goes down to <tuple>
            HashMap<String, String> map = new HashMap<String, String>();
            //reads properties on the same level
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                map.put(reader.getAttribute("name"),
                        reader.getAttribute("value"));
                reader.moveUp();
            }

            t.add(map);
            reader.moveUp(); //goes up to the next <tuple>
        }

        return t;
    }

    /**
     *
     * @param clazz
     * @return
     */
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(Tuples.class);
    }
}
