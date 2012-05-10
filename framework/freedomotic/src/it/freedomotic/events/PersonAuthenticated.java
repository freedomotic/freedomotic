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

/**
 * A person has just been authenticated to enter a zone of the environment.
 * This event must be renamed to PersonAuthentication and enhanced to notify
 * also the de-authentication of a person.
 * @author enrico
 */
public class PersonAuthenticated extends EventTemplate{


    public PersonAuthenticated(){
        
    }

    @Override
    protected void generateEventPayload() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    @Override
    public String getDefaultDestination() {
        return "app.event.sensor.person.access.authenticated";
    }
}
