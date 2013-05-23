/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.restapi.server.resources;

import it.freedomotic.restapi.model.PluginPojo;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 *
 * @author gpt
 */
public class PluginConverter implements Converter {

    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {
        System.out.println("PluginConverter: " + o.toString());
        PluginPojo plug = (PluginPojo) o;
        writer.startNode("plugin");
        writer.setValue(plug.getName());
        writer.endNode();
        writer.startNode("running");
        writer.setValue(Boolean.toString(plug.isRunning()));
        writer.endNode(); //end sequences
    }

    @Override
    public boolean canConvert(Class type) {
        if (type == PluginPojo.class) {
            return true;
        }
        return false;
    }

    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
