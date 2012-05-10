/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.persistence.converters;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import it.freedomotic.model.ds.Tuples;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Enrico
 */
public class TupleConverter implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        Tuples t = (Tuples) o;
        for (int i = 0; i < t.size(); i++) {
            HashMap properties = t.getTuple(i);
            Set<Map.Entry> entrySet = properties.entrySet();
            writer.startNode("tuple");
            for (Map.Entry entry : entrySet) {
                writer.startNode("property");
                writer.addAttribute("name", entry.getKey().toString());
                writer.addAttribute("value", entry.getValue().toString());
                writer.endNode();
            }
            writer.endNode();
        }
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        Tuples t = new Tuples();
        //starts from root <tuples>
        while (reader.hasMoreChildren()) {
            reader.moveDown(); //goes down to <tuple>
            HashMap map = new HashMap();
            //reads properties on the same level
            while (reader.hasMoreChildren()) {
                reader.moveDown();
                map.put(reader.getAttribute("name"), reader.getAttribute("value"));
                reader.moveUp();
            }
            t.add(map);
            reader.moveUp(); //goes up to the next <tuple>
        }
        return t;
    }

    @Override
    public boolean canConvert(Class clazz) {
        return clazz.equals(Tuples.class);
    }
}
