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
 * A person is moving from its actual coordinates to a now coordinate in the environment.
 * This events implicates that the person is already known, see {@link PersonDetected}
 * @author Enrico
 */
public final class PersonMoving extends EventTemplate {

    int id;
    int x;
    int y;

    public PersonMoving(Object source, int id, Point destination) {
        
        x=(int) destination.getX();
        y = (int) destination.getY();
        this.id=id;
        generateEventPayload();
    }

//    public void applyChangesTo(PersonLogic p) {
//        p.addDestination(destination);
//        generateEventPayload();
//    }


//    public int getPersonId() {
//        return id;
//    }
//
//    public Point getDestination() {
//        return destination;
//    }

    @Override
    public String toString() {
        return "person "+ id + " moves in coords " + x+","+ y ;
    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("id",  id);
        payload.addStatement("xCord", x);
        payload.addStatement("yCord",  y);
    }

        @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.movement.moving";
    }
}
