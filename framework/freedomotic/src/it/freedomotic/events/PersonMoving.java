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
import java.awt.Point;

/**
 * A person is moving from its actual coordinates to a now coordinate in the
 * environment. This events implicates that the person is already known, see {@link PersonDetected}
 *
 * @author Enrico
 */
public final class PersonMoving extends EventTemplate {

    String id;
    int x;
    int y;

    public PersonMoving(Object source, String name, Point start, Point destination) {

        x = (int) destination.getX();
        y = (int) destination.getY();
        id = name;
        generateEventPayload();
//        EnvObjectLogic object = EnvObjectPersistence.getObject(name);
//        if (object != null) {
//            object.getPojo().getCurrentRepresentation().setOffset(x, y);
//            for (ZoneLogic zone : Freedomotic.environment.getZones()) {
//                if (AWTConverter.contains(zone.getPojo().getShape(), new FreedomPoint(x, y))) {
//                    ZoneEvent event = new ZoneEvent(this, zone, object);
//                }
//            }
//        }
    }

    @Override
    public String toString() {
        return "person " + id + " moves in coords " + x + "," + y;
    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("id", id);
        payload.addStatement("xCord", x);
        payload.addStatement("yCord", y);
    }

    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.movement.moving";
    }
}
