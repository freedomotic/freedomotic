/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.reactions;

import java.util.ArrayList;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author Enrico
 */
public class ReactionConverter
        implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Reaction rea = (Reaction) o;
        writer.startNode("trigger");
        writer.setValue(rea.getTrigger().getName());
        writer.endNode();
        writer.startNode("sequence");

        for (Command c : rea.getCommands()) {
            if (c != null) {
                writer.startNode("command");
                writer.setValue(c.getName());
                writer.endNode(); //end command
            }
        }

        writer.endNode(); //end sequence
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Trigger t;
        ArrayList<Command> list = new ArrayList<Command>();

        reader.moveDown(); //goes down to <trigger>

        String triggerName = reader.getValue();
        t = TriggerPersistence.getTrigger(triggerName.trim());
        reader.moveUp();
        reader.moveDown();

        while (reader.hasMoreChildren()) {
            reader.moveDown();

            Command c = CommandPersistence.getCommand(reader.getValue().trim());

            if (c != null) {
                list.add(c);
            }

            reader.moveUp();
        }

        reader.moveUp(); //goes up to the next <tuple>

        return new Reaction(t, list);
    }

    @Override
    public boolean canConvert(Class type) {
        if (type == Reaction.class) {
            return true;
        }

        return false;
    }
}
