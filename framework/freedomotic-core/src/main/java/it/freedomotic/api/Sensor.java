/*Copyright 2009 Enrico Nicoletti
eMail: enrico.nicoletti84@gmail.com

This file is part of Freedomotic.

Freedomotic is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
any later version.

Freedomotic is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with EventEngine; if not, write to the Free Software
Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.EventChannel;
import it.freedomotic.exceptions.UnableToExecuteException;

import java.io.IOException;

import javax.jms.ObjectMessage;

/**
 *
 * @author Enrico Nicoletti
 */
@Deprecated
public abstract class Sensor extends Plugin implements Runnable, BusConsumer {

    private static final String DEFAULT_QUEUE_PREFIX = "app.sensor.";
    private static final String SENSORS_QUEUE_DOMAIN = "app.sensor.";
    private boolean isPollingSensor = true;
    private EventChannel channel;

    protected abstract void onInformationRequest(/*TODO: define parameters*/) throws IOException, UnableToExecuteException;

    protected abstract void onRun(); //you can override public void run() anyway

    public Sensor(String pluginName, String manifest) {
        super(pluginName, manifest);
        register();
        setAsNotPollingSensor();
    }

    private void register() {
        channel = new EventChannel();
        channel.setHandler(this);
    }

    public String listenMessagesOn() {
        String defaultQueue = DEFAULT_QUEUE_PREFIX + category + "." + shortName;
        String fromFile = SENSORS_QUEUE_DOMAIN + listenOn;
        if (listenOn.equalsIgnoreCase("undefined")) {
            listenOn = defaultQueue;
            return listenOn;
        } else {
            return fromFile;
        }
    }

    public void notifyEvent(EventTemplate ev) {
        if (isRunning) {
            notifyEvent(ev, ev.getDefaultDestination());
        }
    }

    public void notifyEvent(EventTemplate ev, String destination) {
        if (isRunning) {
            Freedomotic.logger.fine("Sensor " + this.getName() + " notify event " + ev.getEventName() + ":" + ev.getPayload().toString());
            channel.send(ev, destination);
        }
    }

    @Override
    public void start() {
        if (!isRunning) {
            isRunning = true;
            Thread thread = new Thread(this);
            thread.setName("Thread-" + getClass().getSimpleName());
            onStart();
            if (isPollingSensor()) {
                //enters in run only if is required
                thread.start();
            }
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            isRunning = false;
            onStop();
        }
    }

    @Override
    public void run() {
        //onStart();
        while (isRunning && isPollingSensor) {
            onRun();
        }
        //onStop();
    }

    public final boolean isPollingSensor() {
        return isPollingSensor;
    }

    public final void setAsNotPollingSensor() {
        this.isPollingSensor = false;
    }

    public final void setAsPollingSensor() {
        this.isPollingSensor = true;
    }

    @Override
    public void onMessage(ObjectMessage ev) {
        Freedomotic.logger.severe("Sensor class have received a message");
//        if (isRunning) {
//            if (ev instanceof QueryResult) {
//                try {
//                    QueryResult ir = (QueryResult) ev;
//                    onInformationRequest();
//                    ev.setExecuted(true);
//                    queryBus.replyTo(ev);
//                } catch (IOException ex) {
//                    Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
//                } catch (UnableToExecuteException ex) {
//                    Logger.getLogger(Sensor.class.getName()).log(Level.SEVERE, null, ex);
//                    ev.setExecuted(false);
//                }
//            }
//        }
    }
}
