/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
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
package com.freedomotic.environment;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusService;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.objects.impl.Person;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author enrico
 */
public class ZoneLogic {

    private BusService busService;
  
	private Zone pojo;
    private Ownership owner = new LastOutStrategy();
    private List<Person> occupiers = new ArrayList<Person>();
    private EnvironmentLogic FatherEnv = null;

    /**
     *
     * @return
     */
    @RequiresPermissions("zones:read")
    public EnvironmentLogic getEnv() {
        return this.FatherEnv;
    }

    /**
     *
     * @param pojo
     */
    public ZoneLogic(final Zone pojo) {
        this.pojo = pojo;
	this.busService = Freedomotic.INJECTOR.getInstance(BusService.class);
   }

    /**
     *
     * @return
     */
    @RequiresPermissions("zones:read")
    public Zone getPojo() {
        return pojo;
    }

    /**
     *
     * @param g
     * @return
     */
    @RequiresPermissions("zones:read")
    public boolean alreadyTakenBy(Person g) {
        try {
            if (occupiers.contains(g)) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.info("This zone have no occupiers or null reference in occupiers of Zone class");

            return false;
        }
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("zones:read")
    public Ownership getOwnershipStrategy() {
        Ownership os = new LastOutStrategy();

        return os;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("zones:read")
    public int howManyInside() {
        return occupiers.size();
    }

    /**
     *
     * @param g
     * @return
     */
    @RequiresPermissions("zones:update")
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

    /**
     *
     * @param g
     * @return
     */
    @RequiresPermissions("zones:update")
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

    /**
     *
     * @return
     */
    @Override
    @RequiresPermissions("zones:read")
    public String toString() {
        return getPojo().getName();
    }

    /**
     *
     */
    @RequiresPermissions("zones:update")
    public void setChanged() {
        ZoneHasChanged event = new ZoneHasChanged(this,
                getPojo());
        busService.send(event);
    }

    /**
     *
     * @param env
     */
    @RequiresPermissions("zones:read")
    protected void init(EnvironmentLogic env) {
        this.FatherEnv = env;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    @RequiresPermissions("zones:read")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final ZoneLogic other = (ZoneLogic) obj;

        if ((this.pojo != other.pojo) && ((this.pojo == null) || !this.pojo.equals(other.pojo))) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    @RequiresPermissions("zones:read")
    public int hashCode() {
        int hash = 5;
        hash = (89 * hash) + ((this.pojo != null) ? this.pojo.hashCode() : 0);

        return hash;
    }
    private static final Logger LOG = Logger.getLogger(ZoneLogic.class.getName());
}
