/**
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.simulation.fromfile;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.LocationEvent;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.plugins.devices.simulation.Coordinate;
import com.freedomotic.plugins.devices.simulation.TrackingReadFile;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.GenericPerson;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Thread that does Location based events on each Coordinate mutlpy number of iterations .
 *
 * @author Enrico Nicoletti
 */
public class WorkerThread extends Thread {

    private ArrayList<Coordinate> coord;
    private TrackingReadFile master;
    private int iterations = 1;

    /**
     * @param master     plugin instance
     * @param coord      new position coordinates
     * @param iterations number of iterations
     */
    public WorkerThread(TrackingReadFile master, ArrayList<Coordinate> coord, int iterations) {
        this.master = master;
        this.coord = coord;
        this.iterations = iterations;
        setName("MoteTrackingFromFileWT");
    }

    /**
     * activate all events on Coordinates.
     */
    @Override
    public void run() {
        for (int i = 1; i <= iterations; i++) {
            doAllCoordinatesEvents();
        }
    }

    private void doAllCoordinatesEvents() {
        Iterator coordinateIterator = coord.iterator();

        while (coordinateIterator.hasNext()) {
            Coordinate coordinate = (Coordinate) coordinateIterator.next();

            if (coordinate != null) {
                for (EnvObjectLogic person : master.getApi().things().findByName(coordinate.getUserId())) {
                    if (person instanceof GenericPerson) {
                        addLocationBasedEvent(coordinate, (GenericPerson) person);
                    }
                }

                sleepCoordinateTime(coordinate);
            }
        }
    }

    private void addLocationBasedEvent(Coordinate coordinate, GenericPerson person) {
        if (person.getPojo().getName().equalsIgnoreCase(coordinate.getUserId())) {
            FreedomPoint location = new FreedomPoint(coordinate.getX(), coordinate.getY());
            LocationEvent event = new LocationEvent(this, person.getPojo().getUUID(), location);
            master.getLog().info("User \"{}\" moved to ({}) position", coordinate.getUserId(), location.toString());
            master.notifyEvent(event);
        }
    }

    private void sleepCoordinateTime(Coordinate coordinate) {
        try {
            WorkerThread.sleep(coordinate.getTime());
        } catch (InterruptedException interruptedException) {
            master.getLog().error(Freedomotic.getStackTraceInfo(interruptedException));
            Thread.currentThread().interrupt();
        }
    }
}
