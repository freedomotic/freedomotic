/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.jfrontend;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Enrico
 */
public class CalloutsUpdater {

    private static HashMap<Object, Callout> callouts = new HashMap<Object, Callout>();
    private Timer timer;

    public CalloutsUpdater(final Renderer drawer, int milliseconds) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Iterator it = callouts.values().iterator();

                while (it.hasNext()) {
                    Callout callout = (Callout) it.next();
                    long elapsed = now - callout.getTimestamp();

                    if ((callout.getDuration() - elapsed) <= 0) {
                        callout.setDuration(-1);
                        it.remove();
                    }
                }

                if (drawer != null) {
                    drawer.setNeedRepaint(false);
                }
            }
        },
                0,
                milliseconds);
    }

    public void addCallout(Callout newCallout) {
        Iterator it = callouts.values().iterator();
        boolean found = false;
        int line = 0;

        //is an info callout
        if (newCallout.getGroup().equalsIgnoreCase("info")) {
            while (it.hasNext()) {
                Callout callout = (Callout) it.next();

                if (callout.getGroup().equalsIgnoreCase("info")) {
                    //shift old info callout to the next line
                    callout.setPosition(new Point((int) callout.getPosition().getX() + 100,
                            (int) callout.getPosition().getY() + 300));
                    callout.setDuration(callout.getDuration() + 1000);
                }
            }

            //add the new info callout on top
            callouts.put(newCallout.getRelated(),
                    newCallout);
        } else {
            //is an object callout
            if (callouts.containsKey(newCallout.getRelated())) {
                Callout callout = (Callout) callouts.get(newCallout.getRelated());
                callout.setText(newCallout.getText());
                callout.setPosition(newCallout.getPosition());
                callout.setRotation(newCallout.getRotation());
                //extend old callout duration
                callout.setDuration(callout.getDuration() + newCallout.getDuration());
                found = true;
            } else {
                callouts.put(newCallout.getRelated(),
                        newCallout);
            }
        }
    }

    public static void clearAll() {
        callouts.clear();
    }

    public static void clear(String group) {
        Iterator it = callouts.values().iterator();

        while (it.hasNext()) {
            Callout callout = (Callout) it.next();

            if (callout.getGroup().equals(group)) {
                it.remove();
            }
        }
    }

    Iterator iterator() {
        return callouts.values().iterator();
    }
}
