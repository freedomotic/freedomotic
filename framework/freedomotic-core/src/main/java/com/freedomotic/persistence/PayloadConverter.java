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

import com.freedomotic.rules.Payload;
import com.freedomotic.rules.Statement;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class PayloadConverter
        implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(PayloadConverter.class.getName());

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Payload payload = (Payload) o;
        writer.startNode("payload");
        Iterator<Statement> it = payload.iterator();
        while (it.hasNext()) {
            Statement statement = it.next();
            writer.startNode("statement");
            writer.startNode("logical");
            writer.setValue(statement.getLogical());
            writer.endNode(); //</logical>
            writer.startNode("attribute");
            writer.setValue(statement.getAttribute());
            writer.endNode(); //</attribute>
            writer.startNode("operand");
            writer.setValue(statement.getOperand());
            writer.endNode(); //</operand>
            writer.startNode("value");
            writer.setValue(statement.getValue());
            writer.endNode(); //</value>
            writer.endNode(); //</com.freedomotic.reactions.Statement>
        }

        writer.endNode(); //</payload>
    }

    /**
     *
     * @param reader
     * @param uc
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Payload payload = new Payload();
        reader.moveDown(); //goes down to <payload>

        while (reader.hasMoreChildren()) { //<statements> are the childs of payload
            reader.moveDown();

            ArrayList<String> statementValues = new ArrayList<String>();

            while (reader.hasMoreChildren()) { //childs of statement (logical, attribute, ...)
                reader.moveDown();
                statementValues.add(reader.getValue());
                reader.moveUp();
            }

            payload.addStatement(statementValues.get(0),
                    statementValues.get(1),
                    statementValues.get(2),
                    statementValues.get(3));
            reader.moveUp(); //next <statement>
        } //no more <statements> (childs of payload)

        reader.moveUp(); //goes up to the next <payload>

        return payload;
    }

    /**
     *
     * @param clazz
     * @return
     */
    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(Payload.class);
    }
}
