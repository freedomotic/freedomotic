package it.freedomotic.persistence.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandSequence;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Trigger;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Enrico
 */
public class ReactionConverter implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Reaction rea = (Reaction) o;
        writer.startNode("trigger");
        writer.setValue(rea.getTrigger().getName());
        writer.endNode();
        writer.startNode("sequences");
        for (CommandSequence seq : rea.getCommandSequences()) {
            writer.startNode("sequence");
            for (Iterator it = seq.iterator(); it.hasNext();) {
                Command c = (Command)it.next();
                writer.startNode("command");
                writer.setValue(c.getName());
                writer.endNode(); //end command
            }
            writer.endNode(); //end sequence
        }
        writer.endNode(); //end sequences
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Trigger t = new Trigger();
        Command c = new Command();
        ArrayList<CommandSequence> list = new ArrayList<CommandSequence>();

        reader.moveDown(); //goes down to <trigger>
        String triggerName = reader.getValue();
        t = TriggerPersistence.getTrigger(triggerName.trim());
        reader.moveUp();
        reader.moveDown();
        while (reader.hasMoreChildren()) {
            reader.moveDown(); //goes down to <sequences>
            CommandSequence seq = new CommandSequence();
            list.add(seq);
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                c = CommandPersistence.getCommand(reader.getValue().trim());
                seq.append(c);
                reader.moveUp();
            }
            reader.moveUp(); //goes up to the next <tuple>
        }
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
