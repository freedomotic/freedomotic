/**
 *
 * Copyright (c) 2009-2015 Freedomotic team
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

package com.freedomotic.plugins.fromfile;

import com.freedomotic.plugins.Coordinate;
import com.freedomotic.plugins.TrackingReadFile;
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
                //MUST BE REIMPLEMENTED
//                PersonLogic p = Freedom.people.get(c.getId());
//                if (p == null) {
//                    p = Freedom.people.create();
//                }
//                if (!p.getPojo().isDetected()) {
//                    master.notifyEvent(new PersonDetected(master, c.getId(), new Point(c.getX(), c.getY())));
//                } else {
//                    master.notifyEvent(new PersonMoving(master, c.getId(), new Point(c.getX(), c.getY())));
//                }
//                try {
//                    WorkerThread.sleep(c.getTime());
//                } catch (InterruptedException interruptedException) {
//                }
            }
        }
    }
}
