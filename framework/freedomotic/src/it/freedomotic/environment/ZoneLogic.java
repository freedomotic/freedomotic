//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.co m
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package it.freedomotic.environment;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ZoneHasChanged;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.objects.impl.Person;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author enrico
 */
public class ZoneLogic {

    private Zone pojo;
    private Ownership owner = new LastOutStrategy();
    private List<Person> occupiers = new ArrayList<Person>();
    private EnvironmentLogic FatherEnv = null;
   
    public EnvironmentLogic getEnv(){
        return this.FatherEnv;
    }
    
    public ZoneLogic(final Zone pojo) {
        this.pojo=pojo;
    }

    public Zone getPojo() {
        return pojo;
    }

    public boolean alreadyTakenBy(Person g) {
        try {
            if (occupiers.contains(g)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Freedomotic.logger.info("This zone have no occupiers or null reference in occupiers of Zone class");
            return false;
        }
    }

    public Ownership getOwnershipStrategy() {
        Ownership os = new LastOutStrategy();
        return os;
    }

    public int howManyInside() {
        return occupiers.size();
    }

    public boolean enter(Person g) {
        boolean success = false;
        owner = getOwnershipStrategy();

        if (owner.canTriggerReactionsOnEnter(this)) {
            //TODO: REIMPLEMENT
//            PersonEnterZone ev = new PersonEnterZone(this, g, getPojo());
//            Freedomotic.sendEvent(ev);
//            success = true;
        }
        occupiers.add(g); //this must be AFTER the count
        return success;
    }

    public boolean exit(Person g) {
        boolean success = false;
        owner = getOwnershipStrategy();
        if (owner.canTriggerReactionsOnExit(this)) {
            //REIMPLEMENT
//            PersonExitZone ev = new PersonExitZone(this, g, getPojo());
//            Freedomotic.sendEvent(ev);
//            success = true;
        }
        occupiers.remove(g); //this must be AFTER the count
        return success;
    }

    @Override
    public String toString() {
        return getPojo().getName();
    }

    public void setChanged() {
        ZoneHasChanged event = new ZoneHasChanged(this, getPojo());
        Freedomotic.sendEvent(event);
    }

    protected void init(EnvironmentLogic env) {
        this.FatherEnv = env;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ZoneLogic other = (ZoneLogic) obj;
        if (this.pojo != other.pojo && (this.pojo == null || !this.pojo.equals(other.pojo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.pojo != null ? this.pojo.hashCode() : 0);
        return hash;
    }
}
