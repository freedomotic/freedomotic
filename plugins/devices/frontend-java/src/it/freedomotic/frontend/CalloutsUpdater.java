/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.frontend;

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
    private static Timer timer;
    private static Renderer drawer;

    public CalloutsUpdater(final Renderer drawer, int milliseconds) {
        timer = new Timer();
        CalloutsUpdater.drawer = drawer;
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
                drawer.setNeedRepaint(false);
            }
        }, 0, milliseconds);
    }

    public void addCallout(Callout newCallout) {
        Iterator it = callouts.values().iterator();
        boolean found = false;
        while (it.hasNext()) {
            Callout callout = (Callout) it.next();
            if (callout.getRelated() != null) {
                if (callout.getRelated().equals(newCallout.getRelated())) {
                    //this new callout overrides an old one. Remove old callout
                    callout.setText(newCallout.getText());
                    callout.setPosition(newCallout.getPosition());
                    callout.setRotation(newCallout.getRotation());
                    //extend callout duration
                    callout.setDuration(callout.getDuration() + newCallout.getDuration());
                    found = true;
                }
            }
        }
        if (!found) {
            callouts.put(newCallout.getRelated(), newCallout);
        }
    }

    public static void clearAll() {
        callouts.clear();
    }

    public static void clear(String group) {
        Iterator it = callouts.values().iterator();
        while (it.hasNext()) {
            Callout callout = (Callout) it.next();
            if (callout.getGroup().matches(group)){
                it.remove();
            }
        }
    }

    Iterator iterator() {
        return callouts.values().iterator();
    }
}
