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
 * All purpose event. Use only if no more specific events are present in this list.
 * You can generate your own events extending the {@link EventTemplate} class.
 * @author enrico
 */
public class GenericEvent extends EventTemplate {
    
	private static final long serialVersionUID = 6029054631809171990L;
	
	private String destination = "app.event.sensor";

    public GenericEvent(Object source) {
        this.setSender(source);
    }


    @Override
    protected void generateEventPayload() {

    }

    @Override
    public String getDefaultDestination() {
        return destination;
    }

    public void setDestination(String destination){
        this.destination = destination;
    }
}
