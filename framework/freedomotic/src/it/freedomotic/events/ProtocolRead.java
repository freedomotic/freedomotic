/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package it.freedomotic.events;


import com.thoughtworks.xstream.XStream;
import it.freedomotic.api.EventTemplate;

/**
 * Notify a state change for an object identified by the protocol and address values.
 * @author Enrico
 */
public class ProtocolRead extends EventTemplate {
    String protocol;

    public ProtocolRead(Object source, String protocol, String address) {
        this.setSender(source);
        this.protocol=protocol;
        addProperty("protocol", protocol);
        addProperty("address", address);
        generateEventPayload();
//        XStream x = new XStream();
//        System.out.println(x.toXML(this));
    }

    @Override
    protected void generateEventPayload() {
       //do nothing
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.protocol.read."+ protocol.trim().toLowerCase();
    }
}
