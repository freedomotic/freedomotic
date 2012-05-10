/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Sensor;
import it.freedomotic.events.ScheduledEvent;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.gui.ClockForm;
import it.freedomotic.plugins.gui.ReactionList;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author enrico
 */
public class Clock extends Sensor {

    private int TIMER_RESOLUTION = 1000;
    private int lastSentMinute = -1;
    private Timer clock;
    private Awake awake;

    /*
     * sends a scheduled event with the current time every minute retry to send
     * every TIMER_DELAY seconds
     */
    public Clock() {
        super("Clock", "/it.nicoletti.test/clock.xml");
        setDescription("Timer for scheduled events");
        TIMER_RESOLUTION = configuration.getIntProperty("timer-resolution", 1000);
    }

    protected void onRun() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onStart() {
        super.onStart();
        clock = new Timer("FreedomClock", true);
        awake = new Awake();
        clock.scheduleAtFixedRate(awake, TIMER_RESOLUTION, TIMER_RESOLUTION);

    }

    public void onShowGui() {
        ClockForm form = new ClockForm(this);
        bindGuiToPlugin(form);
    }

    @Override
    protected void onStop() {
        super.onStop();
        awake.cancel();
        clock.cancel();
        awake = null;
        clock = null;
    }

    @Override
    protected void onInformationRequest() throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setResolution(int value) {
        TIMER_RESOLUTION = value;
    }

    public int getResolution() {
        return TIMER_RESOLUTION;
    }

    private class Awake extends TimerTask {

        @Override
        public void run() {
            notifyEvent(new ScheduledEvent(this));
        }
    }
}
