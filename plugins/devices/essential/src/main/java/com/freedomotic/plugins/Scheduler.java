/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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
import com.freedomotic.events.ScheduledEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.plugins.gui.ClockForm;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author enrico
 */
public class Scheduler
        extends Protocol {

    private int TIMER_RESOLUTION = 1000;
    private int lastSentMinute = -1;
    private Timer timer;
    private Awake awake;

    /*
     * sends a scheduled event with the current time every minute retry to send
     * every TIMER_DELAY seconds
     */

    /**
     *
     */
    
    public Scheduler() {
        super("Scheduler", "/essential/scheduler.xml");
    }

    @Override
    protected void onRun() {
    }

    @Override
    protected void onStart() {
        TIMER_RESOLUTION = configuration.getIntProperty("timer-resolution", 1000);
        timer = new Timer("FreedomClock", true);
        awake = new Awake();
        timer.scheduleAtFixedRate(awake, TIMER_RESOLUTION, TIMER_RESOLUTION);
    }

    @Override
    public void onShowGui() {
        ClockForm form = new ClockForm(this);
        bindGuiToPlugin(form);
    }

    @Override
    protected void onStop() {
        awake.cancel();
        timer.cancel();
        awake = null;
        timer = null;
    }

    /**
     *
     * @param value
     */
    public void setResolution(int value) {
        TIMER_RESOLUTION = value;
    }

    /**
     *
     * @return
     */
    public int getResolution() {
        return TIMER_RESOLUTION;
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private class Awake
            extends TimerTask {

        @Override
        public void run() {
            notifyEvent(new ScheduledEvent(this));
        }
    }
}
