/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.persistence;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import it.freedomotic.model.object.Properties;
import java.util.HashMap;

/**
 *
 * @author gpt
 */
public class PropertiesConverter implements Converter{

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {        
        Properties rea = (Properties) o;               
        for(String name:rea.stringPropertyNames())
        {
            writer.startNode("property");
            writer.addAttribute("name", name);
            writer.addAttribute("value", rea.getProperty(name));
            writer.endNode();
        }                                     
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        HashMap<String,String> propertiesHashMap = new HashMap<String,String>();                 
        while (reader.hasMoreChildren()) {
                reader.moveDown();
                propertiesHashMap.put(reader.getAttribute("name"),reader.getAttribute("value"));               
                reader.moveUp();
        }
        return new Properties(propertiesHashMap);               
    }
        
    @Override
    public boolean canConvert(Class type) {
          if (type == Properties.class) {
            return true;
        }
        return false;
    }
    
}
