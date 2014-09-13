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

package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.objects.impl.Person;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Enrico
 */
public class TrackingRandomPosition
        extends Protocol {

    /**
     *
     */
    public TrackingRandomPosition() {
        //set plugin name and manufest path
        super("Tracking Simulator (Random)", "/simulation/tracking-simulator-random.xml");
        //set plugin description
        setDescription("It simulates a motes WSN that send information about "
                + "movable sensors position. Positions are randomly generated");
        //wait time between events generation
        //onRun() is called every 2000 milliseconds
        setPollingWait(2000);
    }

    private boolean canGo(int destX, int destY) {
        //can be reimplemented
        //always true
        return true;
    }

    private FreedomPoint randomLocation() {
        int x = 0;
        int y = 0;
        boolean validPos = false;

        while (!validPos) {
            Random rx = new Random();
            Random ry = new Random();
            x = rx.nextInt(1000/*EnvironmentPersistence.getEnvironments().get(0).getPojo().getWidth()*/);
            y = ry.nextInt(1000/*EnvironmentPersistence.getEnvironments().get(0).getPojo().getHeight()*/);

            if (canGo(x, y)) {
                validPos = true;
            }
        }

        return new FreedomPoint(x, y);
    }

    @Override
    protected void onRun() {
        for (EnvObjectLogic object : EnvObjectPersistence.getObjectList()) {
            if (object instanceof com.freedomotic.objects.impl.Person) {
                Person person = (Person) object;
                FreedomPoint location = randomLocation();
                LocationEvent event = new LocationEvent(this, person.getPojo().getUUID(), location);
                notifyEvent(event);
            }
        }
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        //do nothing, this plugin just sends events
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //do nothing, no external event is listened
    }
}
