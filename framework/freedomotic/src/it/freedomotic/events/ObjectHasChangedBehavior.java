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
import it.freedomotic.model.object.Behavior;
import it.freedomotic.model.object.EnvObject;

/**
 * An object has changed its behavior (eg: a light change behavior from off to on)
 * @author enrico
 */
public final class ObjectHasChangedBehavior extends EventTemplate {

    private EnvObject obj;

    public ObjectHasChangedBehavior(Object source, EnvObject obj) {
        super(source);
        this.obj = obj;
        generateEventPayload();
    }


    @Override
    protected void generateEventPayload() {
        payload.addStatement("object.name", obj.getName());
        for (Behavior b : obj.getBehaviors()) {
            payload.addStatement(b.getName(), b.toString());
        }
        payload.addStatement("object.currentRepresentation", obj.getCurrentRepresentationIndex());
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.object.behavior.change";
    }
}
