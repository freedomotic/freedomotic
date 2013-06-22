/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.core.TriggerCheck;
import it.freedomotic.reactions.Trigger;

/**
 * Channel <b>app.event.sensor.protocol.read.PROTOCOL_NAME</b> informs about
 * state changes of objects identified by protocol PROTOCOL_NAME
 *
 * @author Enrico
 */
public class ProtocolRead extends EventTemplate {

    private static final long serialVersionUID = -5568720131236449299L;
	
	String protocol;

    public ProtocolRead(Object source, String protocol, String address) {
        this.setSender(source);
        this.protocol = protocol;
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
        /*
         * If the event contains behavior.name and behavior.value we can bypass
         * the entire trigger system so the plugin developer doesen't need
         * to define an xml trigger file if he already knows on which behavior
         * he would act. If this two properties are not defined the event 
         * is sent as usual.
         * 
         */
        String behaviorName = getProperty("behavior.name");
        //TODO: change behaviorValue in behavior.value (must be changed in all triggers)
        String behaviorValue = getProperty("behaviorValue");
        if (!behaviorName.isEmpty() && !behaviorValue.isEmpty()) {
            Trigger trigger = new Trigger();
            trigger.setName("Object behavior change request from " + this.sender);
            trigger.setPersistence(false);
            trigger.setIsHardwareLevel(true);
            trigger.setPayload(payload);
            TriggerCheck.check(this, trigger);
            return ""; //this event is not sent on the bus
        } else {
            //return the normal event destination
            return "app.event.sensor.protocol.read." + protocol.trim().toLowerCase();
        }
    }
}
