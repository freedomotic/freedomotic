/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
 * http://freedomotic.com
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
package com.freedomotic.plugins.fromfile;

import com.freedomotic.events.LocationEvent;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.plugins.Coordinate;
import com.freedomotic.plugins.TrackingReadFile;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.GenericPerson;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Enrico
 */
public class WorkerThread
        extends Thread {

    ArrayList<Coordinate> coord;
    TrackingReadFile master;

    /**
     *
     * @param master
     * @param coord
     */
    public WorkerThread(TrackingReadFile master, ArrayList<Coordinate> coord) {
        this.master = master;
        this.coord = coord;
        setName("MoteTrackingFromFileWT");
    }

    /**
     *
     */
    @Override
    public void run() {
        Iterator it = coord.iterator();

        while (it.hasNext()) {
            Coordinate c = (Coordinate) it.next();

            if (c != null) {
                for (EnvObjectLogic object : master.getApi().things().findByName(String.valueOf(c.getId()))) {
                    if ((object instanceof GenericPerson) && (object.getPojo().getName().equalsIgnoreCase(String.valueOf(c.getId())))) {
                        GenericPerson person = (GenericPerson) object;
                        FreedomPoint location = new FreedomPoint(c.getX(), c.getY());
                        LocationEvent event = new LocationEvent(this, person.getPojo().getUUID(), location);
                        master.notifyEvent(event);
                    }
                    try {
                        WorkerThread.sleep(c.getTime());
                    } catch (InterruptedException interruptedException) {
                    }
                }
            }
        }
    }
}
