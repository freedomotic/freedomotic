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
 * A person is detected in a position with coordinates x,y of the environment.
 * This event is throwed on the first relevation of the person, after a detection,
 * if the person moves, a {@link PersonMoving} event is thwowed.
 * @author Enrico
 */
public class PersonDetected extends EventTemplate {

    private static final long serialVersionUID = 180422544433345304L;
	
	int id;
    int x;
    int y;

    public PersonDetected(Object source, int id, Point startLocation) {
        
        this.id = id;
        x = (int) startLocation.getX();
        y=(int) startLocation.getY();
        generateEventPayload();
    }

//    public void applyChangesTo(PersonLogic p) {
//        p.setCurrentLocation(startLocation);
//        p.addDestination(startLocation);
//        generateEventPayload();
//    }

//    public int getPersonId() {
//        return id;
//    }

    @Override
    protected void generateEventPayload() {
        payload.addStatement("id", id);
        payload.addStatement("xCord", x);
        payload.addStatement("yCord",y);
    }

    public int getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

   
    @Override
    public String toString(){
       return ("Person " + id + " has been detected in the environment at location " + x+","+y);
    }

        @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.movement.detected";
    }
}
