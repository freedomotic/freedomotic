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
package com.freedomotic.api;

import com.freedomotic.exceptions.PluginRuntimeException;
import com.freedomotic.events.PluginHasChanged;
import com.freedomotic.exceptions.PluginShutdownException;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 * Uses a Template Method pattern which allows subclass to define how to perform
 * a command or how to act when a specific event is received.
 */
public abstract class Protocol extends Plugin {

    private static final Logger LOG = LoggerFactory.getLogger(Protocol.class.getName());
    private int pollingWaitTime = -1;
    private Protocol.SensorThread sensorThread;
    private volatile Destination lastDestination;

    /**
     *
     * @param pluginName
     * @param manifest
     */
    public Protocol(String pluginName, String manifest) {
        super(pluginName, manifest);
        setStatus(PluginStatus.STOPPED);
    }

    /**
     *
     * @throws com.freedomotic.exceptions.PluginRuntimeException
     */
    protected abstract void onRun() throws PluginRuntimeException;

    /**
     *
     * @param c
     * @throws IOException
     * @throws UnableToExecuteException
     */
    protected abstract void onCommand(Command c) throws IOException, UnableToExecuteException;

    /**
     *
     * @param c
     * @return
     */
    protected abstract boolean canExecute(Command c);

    /**
     *
     * @param event
     */
    protected abstract void onEvent(EventTemplate event);

    /**
     *
     * @param listento
     */
    public void addEventListener(String listento) {
        listener.consumeEventFrom(listento);
    }

    public void removeEventListeners() {
        listener.destroy();
    }

    /**
     *
     * @param ev
     */
    public void notifyEvent(EventTemplate ev) {
        if (isAllowedToSend()) {
            notifyEvent(ev, ev.getDefaultDestination());
        }
    }

    public Command notifyCommand(Command command) {
        return getBusService().send(command);
    }

    /**
     *
     * @param ev
     * @param destination
     */
    public void notifyEvent(EventTemplate ev, String destination) {
        if (isAllowedToSend()) {
            LOG.debug("Sensor " + this.getName() + " notify event " + ev.getEventName() + ":" + ev.getPayload().toString());
            getBusService().send(ev, destination);
        }
    }

    /**
     *
     */
    @Override
    public void start() {
        super.start();
        if (isAllowedToStart()) {
            LOG.info("Starting plugin {}", getName());
            Runnable action = new Runnable() {
                @Override
                public synchronized void run() {
                    try {
                        setStatus(PluginStatus.STARTING);
                        //onStart() is called before the thread because it may have some initialization for the sensor thread
                        try {
                            onStart();
                        } catch (PluginStartupException startupEx) {
                            notifyCriticalError(startupEx.getMessage(), startupEx);
                            return; //stop the plugin startup
                        }
                        sensorThread = new Protocol.SensorThread();
                        sensorThread.start();
                        setStatus(PluginStatus.RUNNING);
                        PluginHasChanged event = new PluginHasChanged(this, getName(), PluginHasChanged.PluginActions.START);
                        getBusService().send(event);
                    } catch (Exception e) {
                        setStatus(PluginStatus.FAILED);
                        setDescription("Plugin start FAILED. see logs for details.");
                        LOG.error("Plugin " + getName() + " start FAILED: " + e.getLocalizedMessage(), e);
                    }

                }
            };
            getApi().getAuth().pluginBindRunnablePrivileges(this, action).run();
        }
    }

    /**
     *
     */
    @Override
    public void stop() {
        super.stop();
        if (isRunning()) {
            LOG.info("Stopping plugin {}", getName());
            Runnable action = new Runnable() {
                @Override
                public synchronized void run() {
                    try {
                        setStatus(PluginStatus.STOPPING);
                        try {
                            onStop();
                        } catch (PluginShutdownException shutdownEx) {
                            notifyError(shutdownEx.getMessage());
                        }
                        sensorThread = null;
                        PluginHasChanged event = new PluginHasChanged(this, getName(), PluginHasChanged.PluginActions.STOP);
                        getBusService().send(event);
                        setStatus(PluginStatus.STOPPED);
                    } catch (Exception e) {
                        setStatus(PluginStatus.FAILED);
                        setDescription("Plugin stop FAILED. see logs for details.");
                        LOG.error("Error stopping " + getName() + ": " + e.getLocalizedMessage(), e);
                    }
                }
            };
            getApi().getAuth().pluginBindRunnablePrivileges(this, action).run();
        }
    }

    /**
     *
     * @param wait
     */
    public final void setPollingWait(int wait) {
        pollingWaitTime = wait;
    }

    /**
     *
     * @return
     */
    public int getScheduleRate() {
        return pollingWaitTime;
    }

    private boolean isPollingSensor() {
        return pollingWaitTime > 0;
    }

    @Override
    public final void onMessage(final ObjectMessage message) {
        if (!isRunning()) {
            notifyError("Plugin '" + getName() + "' receives a command while is not running. Turn on the plugin first ");
            return;
        }

        Object payload;

        try {
            payload = message.getObject();

            if (payload instanceof Command) {
                final Command command = (Command) payload;
                LOG.info("{} receives command {} with parametes '{''{'{}'}''}'", new Object[]{this.getName(), command.getName(), command.getProperties()});

                Protocol.ActuatorOnCommandRunnable action;
                lastDestination = message.getJMSReplyTo();
                action = new Protocol.ActuatorOnCommandRunnable(command,
                        message.getJMSReplyTo(),
                        message.getJMSCorrelationID());
                Protocol.ActuatorPerforms task = new Protocol.ActuatorPerforms(getApi().getAuth().pluginBindRunnablePrivileges(this, action));
                task.start();
            } else {
                if (payload instanceof EventTemplate) {
                    final EventTemplate event = (EventTemplate) payload;
                    Protocol.ActuatorOnEventRunnable r = new Protocol.ActuatorOnEventRunnable(event);
                    Protocol.ActuatorPerforms task = new Protocol.ActuatorPerforms(getApi().getAuth().pluginBindRunnablePrivileges(this, r));
                    task.start();
                }
            }
        } catch (JMSException ex) {
            LOG.error(ex.getLocalizedMessage());

        }
    }

    /**
     *
     * @param command
     * @return
     */
    protected Command send(Command command) {
        return getBusService().send(command);
    }

    /**
     *
     * @param command
     */
    public void reply(Command command) {
        // sends back the command
        final String defaultCorrelationID = "-1";
        getBusService().reply(command, lastDestination, defaultCorrelationID);

    }

    private class ActuatorPerforms extends Thread {

        public ActuatorPerforms(Runnable target) {
            super(target, "freedomotic-protocol-executor");
        }

    }

    public class ActuatorOnEventRunnable implements Runnable {

        private final EventTemplate event;

        ActuatorOnEventRunnable(EventTemplate e) {
            this.event = e;
        }

        @Override
        public void run() {
            try {
                // a command is supposed executed if the plugin doesen't say the contrary
                onEvent(event);
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage());
            }
        }
    }

    public class ActuatorOnCommandRunnable implements Runnable {

        private final Command command;
        private final Destination reply;
        private final String correlationID;

        ActuatorOnCommandRunnable(Command c, Destination reply, String correlationID) {
            this.command = c;
            this.reply = reply;
            this.correlationID = correlationID;
        }

        @Override
        public void run() {
            try {
                // a command is supposed executed if the plugin doesen't say the contrary
                command.setExecuted(true);
                onCommand(command);
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage());
                command.setExecuted(false);
            } catch (UnableToExecuteException ex) {
                command.setExecuted(false);
                LOG.info(getName() + " failed to execute command " + command.getName() + ": " + ex.getMessage());
            }

            // automatic-reply-to-command is used when the plugin executes the command in a
            // separate thread. In this cases the onCommand() returns immediately (as execution is forked in a thread)
            // and sometimes this is not the intended behavior. Take a look at the Delayer plugin configuration
            // it has to call reply(...) explicitely
            if ((getConfiguration().getBooleanProperty("automatic-reply-to-commands", true) == true) //default value is true
                    && (command.getReplyTimeout() > 0)) {
                getBusService().reply(command, reply, correlationID); //sends back the command marked as executed or not
            }
        }
    }

    private class SensorThread
            extends Thread {

        @Override
        public void run() {
            try {
                if (isPollingSensor()) {
                    Thread thisThread = Thread.currentThread();
                    while (sensorThread == thisThread) {
                        try {
                            Thread.sleep(pollingWaitTime);
                            synchronized (this) {
                                while (!isRunning() && (sensorThread == thisThread)) {
                                    wait();
                                }
                            }
                        } catch (InterruptedException e) {
                            // TODO do Log?
                        }
                        onRun();
                    }
                } else {
                    if (isRunning()) {
                        onRun();
                    }
                }
            } catch (Exception e) {
                notifyCriticalError(e.getMessage(), e);
            }
        }
    }
}
