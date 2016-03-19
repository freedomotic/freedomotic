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

import com.freedomotic.rules.Statement;
import com.freedomotic.core.Condition;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.Reaction;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

/**
 *
 * @author Enrico Nicoletti
 */
class ReactionConverter implements Converter {

    @Inject
    private CommandRepository commandRepository;

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Reaction rea = (Reaction) o;
        writer.startNode("trigger");
        writer.setValue(rea.getTrigger().getName());
        writer.endNode();

        //start conditions
        if (rea.getConditions() != null && !rea.getConditions().isEmpty()) {
            writer.startNode("conditions");

            for (Condition c : rea.getConditions()) {
                if (c != null) {
                    //start condition
                    writer.startNode("condition");
                    //start target
                    writer.startNode("target");
                    writer.setValue(c.getTarget());
                    writer.endNode();
                    //end target
                    //start statement
                    Statement statement = c.getStatement();
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
                    writer.endNode();
                    //end statement
                    writer.endNode();
                    //end condition
                }
            }
            writer.endNode();
            //end conditions
        }

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

    /**
     *
     * @param reader
     * @param uc
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        //Trigger t;
        List<Condition> conditions = new ArrayList<Condition>();
        ArrayList<Command> list = new ArrayList<Command>();

        reader.moveDown(); //goes down to <trigger>
        String triggerName = reader.getValue();
        //t = TriggerPersistence.getTrigger(triggerName.trim());
        reader.moveUp(); //up to root

        //go down to conditions or sequence
        reader.moveDown();
        if (reader.getNodeName().equalsIgnoreCase("conditions")) {
            while (reader.hasMoreChildren()) {
                //read a single condition
                reader.moveDown(); //move down to condition
                reader.moveDown(); //move down to target
                Condition condition = new Condition();
                condition.setTarget(reader.getValue().trim());
                reader.moveUp(); //move up to condition
                //parse statement
                ArrayList<String> statementValues = new ArrayList<String>();
                reader.moveDown(); //move down to statement
                while (reader.hasMoreChildren()) { //childs of statement (logical, attribute, ...)
                    reader.moveDown(); //move down to statement property
                    System.out.println("    " + reader.getValue().trim());
                    statementValues.add(reader.getValue());
                    reader.moveUp(); //move up to statement
                }
                reader.moveUp();//move up to condition
                Statement stm = new Statement();
                stm.create(statementValues.get(0),
                        statementValues.get(1),
                        statementValues.get(2),
                        statementValues.get(3));
                condition.setStatement(stm);
                conditions.add(condition);
                reader.moveUp(); //move up to conditions
            }
            reader.moveUp(); //move up to root
            reader.moveDown();//move down to sequence
        }
        while (reader.hasMoreChildren()) {
            reader.moveDown(); //move down to command
            List<Command> commands = commandRepository.findByName(reader.getValue().trim());
            if (!commands.isEmpty()) {
                list.add(commands.get(0));
            } else {
                throw new RuntimeException("Cannot find command named " + reader.getValue().trim());
            }
            reader.moveUp(); //move up to sequence
        }
        reader.moveUp(); //move uo to root
        return new Reaction(triggerName, conditions, list);
    }

    /**
     *
     * @param type
     * @return
     */
    @Override
    public boolean canConvert(Class type) {
        return (type == Reaction.class);
    }
}
