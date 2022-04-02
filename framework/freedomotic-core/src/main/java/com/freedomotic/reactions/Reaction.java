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

@SuppressWarnings("squid:S1948") //We are not planning to serialize UI components

@XmlRootElement
public final class Reaction
        implements Serializable {

    private static final long serialVersionUID = -5474545571527398625L;
    private Trigger trigger = new Trigger();
    //list of optional conditions
    private List<Condition> conditions = new ArrayList<>();
    private String uuid;
    private List<Command> commands = new ArrayList<>();
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
     * @param trigger the trigger of the new reaction
     * @param conditions the list of additional conditions of the new reaction
     * @param commands the list of commands of the new reaction
     */
    public Reaction(Trigger trigger, List<Condition> conditions, List<Command> commands) {
        this.uuid = UUID.randomUUID().toString();
        this.conditions = conditions;
        create(trigger, commands);
    }

    /**
     *
     * @param triggerName the trigger of the new reaction
     * @param conditions the list of additional conditions of the new reaction
     * @param commands the list of commands of the new reaction
     */
    public Reaction(String triggerName, List<Condition> conditions, List<Command> commands) {
        Trigger t = TriggerRepositoryImpl.getTrigger(triggerName);
        this.uuid = UUID.randomUUID().toString();
        this.conditions = conditions;
        create(t, commands);
    }

    /**
     *
     * @param trigger the trigger of the new reaction
     * @param commands the list of commands of the new reaction
     */
    public Reaction(String trigger, List<Command> commands) {
        Trigger t = TriggerRepositoryImpl.getTrigger(trigger);
        create(t, commands);
    }

    /**
     *
     * @param trigger the trigger of the new reaction
     * @param commands the list of commands of the new reaction
     */
    public Reaction(Trigger trigger, List<Command> commands) {
        create(trigger, commands);
    }

    /**
     * Creates a reaction without commands.
     *
     * @param trigger the trigger of the new reaction
     */
    public Reaction(Trigger trigger) {
        this.uuid = UUID.randomUUID().toString();
        this.trigger = trigger;
    }

    /**
     * Creates a single command reaction.
     *
     * @param trigger the trigger of the new reaction
     * @param command the command performed when the reaction is triggered
     */
    public Reaction(Trigger trigger, Command command) {
        List<Command> tmp = new ArrayList<>();
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
    /**
     * Creates a new reaction.
     *
     * @param trigger the trigger of the new reaction
     * @param commands the list of commands of the new reaction
     */
    private void create(Trigger trigger, List<Command> commands) {
        this.uuid = UUID.randomUUID().toString();
        if ((trigger != null) && (commands != null)) {
            this.trigger = trigger;
            this.setCommands(commands);
            setChanged();
        }
    }

    /**
     * Returns the trigger of the reaction.
     *
     * @return the trigger of the reaction
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * Returns the list of the reaction commands.
     *
     * @return the list of the reaction commands
     */
    public List<Command> getCommands() {
        if (commands == null) {
            setCommands(new ArrayList<>());
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

    /**
     * Creates a short description of the reaction.
     *
     * @return the short description of the reaction
     */
    private String buildShortDescription() {
        StringBuilder b = new StringBuilder();
        b.append("WHEN  [");
        b.append(trigger);
        b.append("] ");

        if ((conditions != null) && (!conditions.isEmpty())) {
            conditions.forEach((c) -> {
                b.append(c.getStatement().getLogical())
                        .append(" [")
                        .append(c.getTarget()).append(" ")
                        .append(c.getStatement().getAttribute()).append(" ")
                        .append(c.getStatement().getOperand()).append(" ")
                        .append(c.getStatement().getValue()).append("] ");
            });
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

    /**
     * Creates a reaction description.
     *
     * @return the reaction description
     */
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
     * Returns the reaction description.
     *
     * @return the reaction description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the list of additional conditions.
     *
     * @return the list of additional conditions
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
     * Sets the trigger of the reaction.
     *
     * @param trigger the trigger to set
     */
    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
        setChanged();
    }

    /**
     * Creates the short and long descriptions.
     *
     */
    public void setChanged() {
        setDescription(buildDescription());
        setShortDescription(buildShortDescription());
    }

    /**
     * Checks if the reaction has a trigger.
     *
     * @return true if the reaction has a trigger, false otherwise
     */
    public boolean hasTrigger() {
        return (trigger != null);
    }

    /**
     * Sets the additional conditions.
     *
     * @param conditions the additional conditions to set
     */
    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    /**
     * Returns the reaction uuid.
     *
     * @return the reaction uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the reaction uuid. 
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Sets the list of commands.
     *
     * @param commands the commands to set
     */
    public void setCommands(List<Command> commands) {
        this.commands = commands;
    }

    /**
     * Sets the reaction description.
     *
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the reaction short description.
     *
     * @return the reaction short description
     */
    public String getShortDescription() {
        shortDescription = buildShortDescription();
        return shortDescription;
    }

    /**
     * Sets the reaction short description.
     *
     * @param shortDescription the short description to set
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

    /**
     * Adds a command.
     *
     * @param c the command to add
     * @return
     */
    public boolean addCommand(Command c) {
        return commands.add(c);
    }

    /**
     * Removes a command.
     *
     * @param c the command to remove
     * @return
     */
    public boolean removeCommand(Command c) {
        return commands.remove(c);
    }

}
