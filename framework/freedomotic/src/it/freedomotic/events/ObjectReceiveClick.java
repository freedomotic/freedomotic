/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.objects.EnvObjectLogic;
import java.util.Iterator;
import java.util.Map.Entry;

/**
 * An object receives a click on its representation on the GUI.
 * Different types of click are supported (single_click, double_click, right_click)
 * @author Enrico
 */
public final class ObjectReceiveClick extends EventTemplate {

    public enum Click {

        SINGLE_CLICK, DOUBLE_CLICK, RIGHT_CLICK
    };


    public ObjectReceiveClick(Object source, EnvObjectLogic obj, Click click) {
        this.setSender(source);
        payload.addStatement("click", click.toString());
        Iterator it = obj.getExposedProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry entry = (Entry) it.next();
            payload.addStatement(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    @Override
    protected void generateEventPayload() {
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.object.behavior.clicked";
    }
}
