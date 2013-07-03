/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.bus.EventChannel;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;

import it.freedomotic.security.Auth;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Uses a Template Method pattern which allows subclass to define how to perform
 * a command or how to act when a specific event is received.
 */
public abstract class Protocol extends Plugin implements BusConsumer {

    private static final String ACTUATORS_QUEUE_DOMAIN = "app.actuators.";
    private int POLLING_WAIT_TIME = -1;
    private CommandChannel commandsChannel; //one to one messaging pattern
    private EventChannel eventsChannel; //one to many messaging pattern
    private Protocol.SensorThread sensorThread;
    private volatile Destination lastDestination;

    protected abstract void onRun();

    protected abstract void onCommand(Command c) throws IOException, UnableToExecuteException;

    protected abstract boolean canExecute(Command c);

    protected abstract void onEvent(EventTemplate event);

    public Protocol(String pluginName, String manifest) {
        super(pluginName, manifest);
        POLLING_WAIT_TIME = -1; //just to be sure
        register();
        try {
            findAnnotations();
        } catch (Exception ex) {
            Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void register() {
        eventsChannel = new EventChannel();
        eventsChannel.setHandler(this);
        commandsChannel = new CommandChannel();
        commandsChannel.setHandler(this);
        commandsChannel.consumeFrom(getCommandsChannelToListen());
    }

    public void addEventListener(String listento) {
        eventsChannel.consumeFrom(listento);
    }

    private String getCommandsChannelToListen() {
        String defaultQueue = ACTUATORS_QUEUE_DOMAIN + category + "." + shortName;
        String customizedQueue = ACTUATORS_QUEUE_DOMAIN + listenOn;
        if (getReadQueue().equalsIgnoreCase("undefined")) {
            listenOn = defaultQueue + ".in";
            return listenOn;
        } else {
            return customizedQueue;
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
            eventsChannel.send(ev, destination);
        }
    }

    @Override
    public void start() {
        if (!isRunning) {
            isRunning = true;
            Runnable action = new Runnable() {
                @Override
                public synchronized void run() {
                    onStart();
                    sensorThread = new Protocol.SensorThread();
                    sensorThread.start();
                    PluginHasChanged event = new PluginHasChanged(this, getName(), PluginHasChanged.PluginActions.START);
                    Freedomotic.sendEvent(event);
                }
            };
            Auth.pluginExecutePrivileged(this, action);
        }
    }

    @Override
    public void stop() {
        Runnable action = new Runnable() {
            @Override
            public synchronized void run() {
                onStop();
                sensorThread = null;
                notify();
                PluginHasChanged event = new PluginHasChanged(this, getName(), PluginHasChanged.PluginActions.STOP);
                Freedomotic.sendEvent(event);
            }
        };
        if (isRunning) {
            isRunning = false;
            Auth.pluginExecutePrivileged(this, action);
        }
    }

    public final void setPollingWait(int wait) {
        if (wait > 0) {
            POLLING_WAIT_TIME = wait;
        }
    }

    public int getScheduleRate() {
        return POLLING_WAIT_TIME;
    }

    private boolean isPollingSensor() {
        if (POLLING_WAIT_TIME > 0) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void onMessage(final ObjectMessage message) {
        if (!isRunning) {
            Freedomotic.logger.config("Protocol '" + getName() + "' receives a command while is not running. Plugin tries to turn on itself...");
            start();
        }
        Object payload = null;
        try {
            payload = message.getObject();
            if (payload instanceof Command) {
                final Command command = (Command) payload;
                Freedomotic.logger.config(this.getName() + " receives command " + command.getName() + " with parametes {" + command.getProperties() + "}");
                Protocol.ActuatorPerforms task;
                lastDestination = message.getJMSReplyTo();
                task = new Protocol.ActuatorPerforms(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
                task.start();
            } else {
                if (payload instanceof EventTemplate) {
                    final EventTemplate event = (EventTemplate) payload;
                    onEvent(event);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(Actuator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private class ActuatorPerforms extends Thread {

        private Command command;
        private Destination reply;
        private String correlationID;

        ActuatorPerforms(Command c, Destination reply, String correlationID) {
            this.command = c;
            this.reply = reply;
            this.correlationID = correlationID;
            this.setName("freedom-protocol-executor");
        }

        @Override
        public void run() {
            try {
                command.setExecuted(true);
                onCommand(command);
            } catch (IOException ex) {
                Logger.getLogger(Actuator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnableToExecuteException ex) {
                command.setExecuted(false);
            }
            if ((getConfiguration().getBooleanProperty("automatic-reply-to-commands", true) == true) //default value is true
                    && (command.getReplyTimeout() > 0)) {
                commandsChannel.reply(command, reply, correlationID); //sends back the command marked as executed or not
            }
        }
    }

    protected Command send(Command command) {
        return commandsChannel.send(command);
    }

    public void reply(Command command) {
        commandsChannel.reply(command, lastDestination, "-1"); //sends back the command
    }

    private class SensorThread extends Thread {

        @Override
        public void run() {

            if (isPollingSensor()) {
                Thread thisThread = Thread.currentThread();
                while (sensorThread == thisThread) {
                    try {
                        sensorThread.sleep(POLLING_WAIT_TIME);
                        synchronized (this) {
                            while (!isRunning && sensorThread == thisThread) {
                                wait();
                            }
                        }
                    } catch (InterruptedException e) {
                    }

                    onRun();
                }
            } else {
                if (isRunning) {
                    onRun();
                }
            }
        }
    }

    private void findAnnotations() throws Exception {
        Class<? extends Protocol> currClass = getClass();
        Method[] methods = currClass.getDeclaredMethods();

        for (Method method : methods) {
            if (method.isAnnotationPresent(Schedule.class)) {
                Schedule schedule = method.getAnnotation(Schedule.class);
                int rate = schedule.rate();
                setPollingWait(rate);
            } else {
                if (method.isAnnotationPresent(ListenEventsOn.class)) {
                    ListenEventsOn listen = method.getAnnotation(ListenEventsOn.class);
                    String channel = listen.channel();
                    eventsChannel.consumeFrom(channel, method);
                }
                //TODO: serach for ListenCommandOn annotations
            }
        }
    }
}
