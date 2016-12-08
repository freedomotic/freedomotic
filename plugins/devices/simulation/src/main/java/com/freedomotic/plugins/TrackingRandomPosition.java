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
package com.freedomotic.plugins;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.reactions.Command;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.GenericPerson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

/**
 *
 * @author Enrico Nicoletti
 */
public class TrackingRandomPosition extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(TrackingRandomPosition.class);

    public TrackingRandomPosition() {
        super("Tracking Simulator (Random)", "/simulation/tracking-simulator-random.xml");
        setPollingWait(2000);
    }

    private FreedomPoint randomLocation() {
        int x;
        int y;

        Random rx = new Random();
        Random ry = new Random();
        x = rx.nextInt(getApi().environments().findAll().get(0).getPojo().getWidth());
        y = ry.nextInt(getApi().environments().findAll().get(0).getPojo().getHeight());

        return new FreedomPoint(x, y);
    }

    @Override
    protected void onRun() {
        for (EnvObjectLogic object : getApi().things().findAll()) {
            if (object instanceof GenericPerson) {
                GenericPerson person = (GenericPerson) object;
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
