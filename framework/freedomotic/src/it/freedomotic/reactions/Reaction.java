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
package it.freedomotic.reactions;

import it.freedomotic.persistence.TriggerPersistence;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author enrico
 */
public class Reaction implements Serializable {

    private Trigger trigger=new Trigger();
    private String uuid;
    private ArrayList<Command> commands = new ArrayList<Command>();
    private String description;
    private String shortDescription;

    public Reaction() {
    }

    public Reaction(String trigger, ArrayList<Command> commands) {
        Trigger t = TriggerPersistence.getTrigger(trigger);
        create(t, commands);
    }

    public Reaction(Trigger trigger, ArrayList<Command> commands) {
        create(trigger, commands);
    }

    public Reaction(Trigger trigger) {
        this.trigger = trigger;
    }

    /**
     * creates a single command reaction
     *
     * @param trigger
     * @param command
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
    private void create(Trigger trigger, ArrayList<Command> commands) {
        if ((trigger != null) && (commands != null)) {
            this.trigger = trigger;
            this.commands = commands;
            setChanged();
        }
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public ArrayList<Command> getCommands() {
        return commands;
    }

    @Override
    public String toString() {
        return shortDescription;
    }

    private String buildShortDescription() {
        StringBuilder b = new StringBuilder();
        b.append("IF  [");
        b.append(trigger);
        b.append("] THEN ");

        Iterator commandIterator = commands.iterator();
        while (commandIterator.hasNext()) {
            Command c = (Command) commandIterator.next();
            b.append("(").append(c.getName()).append(")");
            if (commandIterator.hasNext()) {
                b.append(" AFTER THAT ");
            }
        }
        return b.toString();
    }

    private String buildDescription() {
        StringBuilder b = new StringBuilder();
        b.append(TriggerPersistence.getTrigger(trigger).getDescription());
        b.append(" then ");
        Iterator it = getCommands().iterator();
        while (it.hasNext()) {
            Command c = (Command) it.next();
            b.append(c.getDescription());
            if (it.hasNext()) {
                b.append(" and ");
            }
        }
        return b.toString();
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Reaction other = (Reaction) obj;
        if ((this.shortDescription == null) ? (other.shortDescription != null) : !this.shortDescription.equals(other.shortDescription)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + (this.shortDescription != null ? this.shortDescription.hashCode() : 0);
        return hash;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
        setChanged();
    }

    public void setChanged() {
        description = buildDescription();
        shortDescription = buildShortDescription();
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public boolean hasTrigger() {
        if (trigger != null) {
            return true;
        }
        return false;
    }
}
