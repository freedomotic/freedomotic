/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
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
import com.freedomotic.events.PersonEntersZone;
import com.freedomotic.events.PersonExitsZone;
import com.freedomotic.events.ZoneHasChanged;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.things.GenericPerson;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.LoggerFactory;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
public class ZoneLogic {

    //private BusService busService;
    private Zone pojo;
    private Ownership owner = new LastOutStrategy();
    private final List<GenericPerson> occupiers = new ArrayList<GenericPerson>();
    private EnvironmentLogic FatherEnv = null;
    private Ownership ownershipStrategy;
    private static final Logger LOG = LoggerFactory.getLogger(ZoneLogic.class.getName());

    /**
     *
     * @param pojo
     */
    public ZoneLogic(final Zone pojo) {
        this.pojo = pojo;
    }

    @RequiresPermissions("zones:read")
    public EnvironmentLogic getEnv() {
        return this.FatherEnv;
    }

    /**
     *
     *
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
    public boolean isInside(GenericPerson g) {
        try {
            return occupiers.contains(g);
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
        if (ownershipStrategy == null) {
            //create default ownership strategy
            ownershipStrategy = new LastOutStrategy();
        }

        return ownershipStrategy;
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
     * @param p
     * @return
     */
    @RequiresPermissions("zones:update")
    public synchronized boolean enter(GenericPerson p) {
        boolean success = false;
        owner = getOwnershipStrategy();

        if (owner.canTriggerReactionsOnEnter(this)) {
            PersonEntersZone ev = new PersonEntersZone(this, p, getPojo());
            Freedomotic.sendEvent(ev);
            success = true;
        }

        occupiers.add(p); //this must be AFTER the howManyInside() count

        return success;
    }

    /**
     *
     * @param p
     * @return
     */
    @RequiresPermissions("zones:update")
    public synchronized boolean exit(GenericPerson p) {
        boolean success = false;
        owner = getOwnershipStrategy();

        if (owner.canTriggerReactionsOnExit(this)) {
            PersonExitsZone ev = new PersonExitsZone(this, p, getPojo());
            Freedomotic.sendEvent(ev);
            success = true;
        }

        occupiers.remove(p); //this must be AFTER the howManyInside() count

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
        Freedomotic.sendEvent(event);
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
}
