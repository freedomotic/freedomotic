/*Copyright 2009 Enrico Nicoletti
 eMail: enrico.nicoletti84@gmail.com

 This file is part of Freedomotic.

 Freedomotic is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 Freedomotic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with EventEngine; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.events;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;

import java.util.Iterator;
import java.util.Map.Entry;

/**
 * Channel <b>app.event.sensor.object.behavior.change</b> informs that an object 
 * has changed its behavior (eg: a light change behavior from off to on).
 *
 * Available tokens for triggers:
 *
 * @see it.freedomotic.api.EventTemplate for properties like date, time, sender
 * which are common to all events
 * @see it.freedomotic.object.EnvObjectLogic#getExposedProperties() for object
 * data
 *
 * @author enrico
 */
public class ObjectHasChangedBehavior extends EventTemplate {

    private static final long serialVersionUID = 6892968576173017195L;

	//private EnvObject obj;
    public ObjectHasChangedBehavior(Object source, EnvObjectLogic obj) {
        super(source);
        //add default object properties
        Iterator<Entry<String,String>> it = obj.getExposedProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<String,String> entry = it.next();
            payload.addStatement(entry.getKey().toString(), entry.getValue().toString());
        }
        //add the list of changed behaviors
        payload.addStatement("object.currentRepresentation", obj.getPojo().getCurrentRepresentationIndex());
        for (BehaviorLogic behavior : obj.getBehaviors()) {
            if (behavior.isChanged()) {
                payload.addStatement("object.behavior." + behavior.getName(), behavior.getValueAsString());
                behavior.setChanged(false);
            }
        }
    }

    @Override
    protected void generateEventPayload() {
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.object.behavior.change";
    }
}
