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

import it.freedomotic.core.SchedulingData;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import java.util.ArrayList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 *
 * @author enrico
 */
public class Reaction implements Serializable {

    private Trigger trigger;
    private String uuid;
    private ArrayList<CommandSequence> sequences = new ArrayList<CommandSequence>();
    private String description;
    private String shortDescription;
    private SchedulingData scedulingData;

    public Reaction(String trigger, ArrayList<CommandSequence> sequences) {
        Trigger t = TriggerPersistence.getTrigger(trigger);
        create(t, sequences);
    }

    public Reaction(Trigger trigger, ArrayList<CommandSequence> sequences) {
        create(trigger, sequences);
    }

    /**
     * creates a single command reaction
     * @param trigger
     * @param command
     */
    public Reaction(Trigger trigger, Command command) {
        ArrayList<CommandSequence> tmpSequences = new ArrayList<CommandSequence>();
        CommandSequence seq = new CommandSequence();
        tmpSequences.add(seq);
        seq.append(command);
        create(trigger, tmpSequences);
    }

    public Reaction(Trigger trigger, String commandsList) {
        String[] lines = commandsList.split("\n");
        ArrayList<CommandSequence> tmpSequences = new ArrayList<CommandSequence>();
        for (String line : lines) {
            CommandSequence seq = new CommandSequence();
            for (String string : Arrays.asList(line.split(","))) {
                seq.append(CommandPersistence.getCommand(string));
            }
            tmpSequences.add(seq);
        }
        create(trigger, tmpSequences);
    }

    private void create(Trigger trigger, ArrayList<CommandSequence> sequences) {
        if ((trigger != null) && (sequences != null)) {
            //TODO: addAndRegister other controls. validate reaction
            this.trigger = trigger;
            this.sequences = sequences;
            setChanged();
        }
    }

    public int getSequences() {
        return this.sequences.size();
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public ArrayList<CommandSequence> getCommandSequences() {
        return this.sequences;
    }

    public ArrayList<Command> getCommands() {
        ArrayList<Command> commands = new ArrayList<Command>();
        for (CommandSequence sequence : getCommandSequences()) {
            for (Iterator it = sequence.iterator(); it.hasNext();) {
                Command command = (Command) it.next();
                commands.add(command);
            }
        }
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
        Iterator sequenceIterator = sequences.iterator();

        while (sequenceIterator.hasNext()) {
            CommandSequence commandSequence = (CommandSequence) sequenceIterator.next();
            b.append("[");
            Iterator commandIterator = commandSequence.iterator();
            while (commandIterator.hasNext()) {
                Command c = (Command) commandIterator.next();
                b.append("(").append(c.getName()).append(")");
                if (commandIterator.hasNext()) {
                    b.append(" AFTER THAT ");
                }
            }
            b.append("]");
            if (sequenceIterator.hasNext()) {
                b.append(" AND ");
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

    public void clearSequence(int index) {
        getCommandSequences().get(index).getCommands().clear();
        setChanged();
    }

    public void appendCommand(Command command, int index) {
        getCommandSequences().get(index).append(command);
        setChanged();
    }

    public void setScheduling(SchedulingData data) {
        scedulingData = data;
    }

    public SchedulingData getScedulingData() {
        return scedulingData;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid=uuid;
    }
}
