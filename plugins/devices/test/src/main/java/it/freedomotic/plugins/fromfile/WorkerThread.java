/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.fromfile;

import it.freedomotic.plugins.Coordinate;
import it.freedomotic.plugins.TrackingReadFile;

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

    public WorkerThread(TrackingReadFile master, ArrayList<Coordinate> coord) {
        this.master = master;
        this.coord = coord;
        setName("MoteTrackingFromFileWT");
    }

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
