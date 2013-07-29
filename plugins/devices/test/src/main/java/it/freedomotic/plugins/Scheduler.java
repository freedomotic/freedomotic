/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.api.Sensor;

import it.freedomotic.events.ScheduledEvent;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.plugins.gui.ClockForm;

import it.freedomotic.reactions.Command;

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
    public Scheduler() {
        super("Scheduler", "/it.nicoletti.test/scheduler.xml");
        setDescription("Timer for scheduled events");
        TIMER_RESOLUTION = configuration.getIntProperty("timer-resolution", 1000);
    }

    protected void onRun() {
    }

    @Override
    protected void onStart() {
        super.onStart();
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
        super.onStop();
        awake.cancel();
        timer.cancel();
        awake = null;
        timer = null;
    }

    public void setResolution(int value) {
        TIMER_RESOLUTION = value;
    }

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
