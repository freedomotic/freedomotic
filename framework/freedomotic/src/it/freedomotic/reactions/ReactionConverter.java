package it.freedomotic.reactions;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;
import java.util.ArrayList;

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
        writer.startNode("sequence");
        for (Command c : rea.getCommands()) {
            writer.startNode("command");
            writer.setValue(c.getName());
            writer.endNode(); //end command
        }
        writer.endNode(); //end sequence
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Trigger t = new Trigger();
        ArrayList<Command> list = new ArrayList<Command>();

        reader.moveDown(); //goes down to <trigger>
        String triggerName = reader.getValue();
        t = TriggerPersistence.getTrigger(triggerName.trim());
        reader.moveUp();
        reader.moveDown();
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                Command c = CommandPersistence.getCommand(reader.getValue().trim());
                list.add(c);
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
