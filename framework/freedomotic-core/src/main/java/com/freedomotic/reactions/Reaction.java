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
//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.com
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package com.freedomotic.reactions;

import com.freedomotic.core.Condition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public final class Reaction
        implements Serializable {

    private static final long serialVersionUID = -5474545571527398625L;
    private Trigger trigger = new Trigger();
    //list of optional conditions
    private List<Condition> conditions = new ArrayList<Condition>();
    private String uuid;
    private List<Command> commands = new ArrayList<Command>();
    private String description;
    private String shortDescription;

    /**
     *
     */
    public Reaction() {
        this.uuid = UUID.randomUUID().toString();
    }

    /**
     *
     * @param trigger
     * @param conditions
     * @param commands
     */
    public Reaction(Trigger trigger, List<Condition> conditions, List<Command> commands) {
        this.uuid = UUID.randomUUID().toString();
        this.conditions = conditions;
        create(trigger, commands);
    }

    public Reaction(String triggerName, List<Condition> conditions, List<Command> commands) {
        Trigger t = TriggerRepositoryImpl.getTrigger(triggerName);
        this.uuid = UUID.randomUUID().toString();
        this.conditions = conditions;
        create(t, commands);
    }

    /**
     *
     * @param trigger
     * @param commands
     */
    public Reaction(String trigger, List<Command> commands) {
        Trigger t = TriggerRepositoryImpl.getTrigger(trigger);
        create(t, commands);
    }

    /**
     *
     * @param trigger
     * @param commands
     */
    public Reaction(Trigger trigger, List<Command> commands) {
        create(trigger, commands);
    }

    /**
     *
     * @param trigger
     */
    public Reaction(Trigger trigger) {
        this.trigger = trigger;
    }

    /**
     * creates a single command reaction
     *
     * @param trigger the trigger of the new reaction
     * @param command the command performed when the reaction is triggered
     */
    public Reaction(Trigger trigger, Command command) {
        ArrayList<Command> tmp = new ArrayList<Command>();
        tmp.add(command);
        create(trigger, tmp);
    }

//    public Reaction(Trigger trigger, String commandsList) {
//        String[] lines = commandsList.split("\n");
//        ArrayList<CommandSequence> tmpSequences = new ArrayList<CommandSequence>();
//        for (String line : lines) {
//            CommandSequence seq = new CommandSequence();
//            for (String string : Arrays.asList(line.split(","))) {
//                seq.append(CommandPersistence.getCommand(string));
//            }
//            tmpSequences.add(seq);
//        }
//        create(trigger, tmpSequences);
//    }
    private void create(Trigger trigger, List<Command> commands) {
        this.uuid = UUID.randomUUID().toString();
        if ((trigger != null) && (commands != null)) {
            this.trigger = trigger;
            this.setCommands(commands);
            setChanged();
        }
    }

    /**
     *
     * @return
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     *
     * @return
     */
    public List<Command> getCommands() {
        if (commands == null) {
            setCommands(new ArrayList<Command>());
        }

        return commands;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getShortDescription();
    }

    private String buildShortDescription() {
        StringBuilder b = new StringBuilder();
        b.append("WHEN  [");
        b.append(trigger);
        b.append("] ");

        if ((conditions != null) && (!conditions.isEmpty())) {
            for (Condition c : conditions) {
                b.append(c.getStatement().getLogical())
                        .append(" [")
                        .append(c.getTarget()).append(" ")
                        .append(c.getStatement().getAttribute()).append(" ")
                        .append(c.getStatement().getOperand()).append(" ")
                        .append(c.getStatement().getValue()).append("] ");
            }
        }

        b.append(" THEN ");

        Iterator<Command> commandIterator = getCommands().iterator();
        while (commandIterator.hasNext()) {
            Command c = commandIterator.next();
            if (c != null) {
                b.append("(");
                //TODO: disabled because it always reports 'failed'
//                if (!c.isExecuted()) {
//                    b.append("FAILED: ");
//                }
                b.append(c.getName()).append(")");
            }

            if (commandIterator.hasNext()) {
                b.append(" AFTER THAT ");
            }
        }

        return b.toString();
    }

    private String buildDescription() {
        StringBuilder b = new StringBuilder();

        if ((trigger != null) && (trigger.getDescription() != null)) {
            b.append(TriggerRepositoryImpl.getTrigger(trigger).getDescription());
        }

        b.append(" then ");
        Iterator<Command> it = getCommands().iterator();
        while (it.hasNext()) {
            Command c = it.next();
            if (c != null) {
                b.append(c.getDescription());
            }

            if (it.hasNext()) {
                b.append(" and ");
            }
        }

        return b.toString();
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @return
     */
    public List<Condition> getConditions() {
        return conditions;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Reaction other = (Reaction) obj;

        if ((this.getShortDescription() == null) ? (other.getShortDescription() != null)
                : (!this.shortDescription.equals(other.shortDescription))) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = (73 * hash) + ((this.getShortDescription() != null) ? this.getShortDescription().hashCode() : 0);

        return hash;
    }

    /**
     *
     * @param trigger
     */
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
        setChanged();
    }

    /**
     *
     */
    public void setChanged() {
        setDescription(buildDescription());
        setShortDescription(buildShortDescription());
    }

    /**
     *
     * @return
     */
    public boolean hasTrigger() {
        if (trigger != null) {
            return true;
        }

        return false;
    }

    /**
     * @param conditions the conditions to set
     */
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @param commands the commands to set
     */
    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the shortDescription
     */
    public String getShortDescription() {
        shortDescription = buildShortDescription();
        return shortDescription;
    }

    /**
     * @param shortDescription the shortDescription to set
     */
    public void setShortDescription(String shortDescription) {
        this.shortDescription = shortDescription;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        Reaction r = new Reaction();
        r.setCommands(this.getCommands());
        //r.setConditions(this.getConditions());
        r.setTrigger(this.getTrigger());

        return r;
    }

    public boolean addCommand(Command c) {
        return commands.add(c);
    }

    public boolean removeCommand(Command c) {
        return commands.remove(c);
    }

}
