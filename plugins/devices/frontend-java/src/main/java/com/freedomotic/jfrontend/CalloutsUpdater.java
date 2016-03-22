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
package com.freedomotic.jfrontend;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author Enrico Nicoletti
 */
public class CalloutsUpdater {

    private static final List<Callout> callouts = Collections.synchronizedList(new ArrayList<Callout>());
    private static Callout mousePointerCallout;
    private Timer timer;

    /**
     *
     * @param drawer
     * @param schedulingRate
     */
    public CalloutsUpdater(final Renderer drawer, int schedulingRate) {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                boolean removedSomething = false;
                synchronized (callouts) {
                    Iterator it = callouts.iterator();
                    while (it.hasNext() && !removedSomething) {
                        Callout callout = (Callout) it.next();
                        long elapsedTime = now - callout.getTimestamp();
                        if (elapsedTime > callout.getDuration()) {
                            removedSomething = true;
                            it.remove();
                        }
                    }
                }

                if (removedSomething && drawer != null) {
                    //request a repainting without background repainting (faster)
                    drawer.setNeedRepaint(false);
                }
            }
        }, 0, schedulingRate);
    }

    /**
     *
     * @param newCallout
     */
    public void addCallout(Callout newCallout) {
        if (!newCallout.getText().trim().isEmpty()) {
            newCallout.setTimestamp();
            if (newCallout.getDuration() > 5000) {
                newCallout.setDuration(5000);
            }
            //random color for info text
            if (newCallout.getGroup().equalsIgnoreCase("info")) {
                //newCallout.setColor(new Color(rand(0, 255), rand(0, 255), rand(0, 255), 180));
                newCallout.setColor(new Color(50, 50, 50, 180));
            }
            if (newCallout.getGroup().equalsIgnoreCase("mouse")) {
                mousePointerCallout = newCallout;
            } else {
                callouts.add(newCallout);
            }
        }
    }

    private int rand(int min, int max) {

        // NOTE: Usually this should be a field rather than a method
        // variable so that it is not re-seeded every call.
        Random rand = new Random();

        // nextInt is normally exclusive of the top value,
        // so add 1 to make it inclusive
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     *
     */
    public void clearAll() {
        callouts.clear();
    }

    /**
     *
     * @param group
     */
    public void clear(String group) {
        synchronized (callouts) {
            Iterator it = callouts.iterator();
            while (it.hasNext()) {
                Callout callout = (Callout) it.next();

                if (callout.getGroup().equals(group)) {
                    it.remove();
                }
            }
        }
    }

    Collection<Callout> getPrintableCallouts() {
        return callouts;
    }

    Callout getMousePointerCallout() {
        return mousePointerCallout;
    }
}
