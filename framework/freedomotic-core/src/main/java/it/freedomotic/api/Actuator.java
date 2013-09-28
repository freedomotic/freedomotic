/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.BusService;
import it.freedomotic.bus.BusMessagesListener;
import it.freedomotic.events.PluginHasChanged;
import it.freedomotic.events.PluginHasChanged.PluginActions;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import com.google.inject.Inject;

/**
 *
 * @author Enrico Nicoletti
 */
@Deprecated
public abstract class Actuator extends Plugin implements BusConsumer {
	
	private static final Logger LOG = Logger.getLogger(Actuator.class.getName());

    private static final String ACTUATORS_QUEUE_DOMAIN = "app.actuators.";

    private volatile Destination lastDestination;
    
    private BusMessagesListener listener;
    
    private ExecutorService executor;
    
    private BusService busService;
    
    public Actuator(String pluginName, String manifest) {
        super(pluginName, manifest);
        register();
//        if (configuration.getBooleanProperty("threaded-commands-execution", true)) {
//            executor = Executors.newCachedThreadPool();
//        } else {
        executor = Executors.newSingleThreadExecutor();
//        }
		this.busService = Freedomotic.INJECTOR.getInstance(BusService.class);
    }

	private void register() {
		listener = new BusMessagesListener(this);
		listener.consumeCommandFrom(listenMessagesOn());
	}

    public void addEventListener(String listento) {
    	listener.consumeEventFrom(listento);
    }

    public void addCommandListener(String listento) {
    	listener.consumeCommandFrom(listento);
    }

    public String listenMessagesOn() {
        String defaultQueue = ACTUATORS_QUEUE_DOMAIN + category + "." + shortName;
        String customizedQueue = ACTUATORS_QUEUE_DOMAIN + listenOn;

        if (listenOn.equalsIgnoreCase("undefined")) {
            listenOn = defaultQueue + ".in";

            return listenOn;
        } else {
            return customizedQueue;
        }
    }

    protected abstract void onCommand(Command c)
            throws IOException, UnableToExecuteException;

    protected abstract boolean canExecute(Command c);

    protected void onEvent(EventTemplate event) {
        //do nothing. It can be overridden by subclasses
    }

    @Override
    public void start() {
         if (configuration.getBooleanProperty("enable-i18n", false)){
            getApi().getI18n().registerPluginBundleDir(this);
        }
        if (!isRunning) {
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    loadPermissionsFromManifest();
                    onStart();
                    isRunning = true;
                    LOG.info("Actuator " + getName() + " started.");
                    PluginHasChanged change = new PluginHasChanged(this, getName(), PluginActions.START);
                    busService.send(change);
                }
            };
            getApi().getAuth().pluginExecutePrivileged(this, action);
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            Runnable action = new Runnable() {
                @Override
                public void run() {
                    isRunning = false;
                    onStop();
                    LOG.info("Actuator " + getName() + " stopped.");
                    PluginHasChanged change = new PluginHasChanged(this, getName(), PluginActions.STOP);
                    busService.send(change);
                }
            };
            getApi().getAuth().pluginExecutePrivileged(this, action);
        }
    }

    @Override
    public void onMessage(final ObjectMessage message) {
        if (!isRunning) {
            LOG.config("Actuator '" + getName()
                    + "' receives a Command while is not running. Plugin try to turn on itself...");
            start();
        }

        try {
            Object payload = message.getObject();

            if (payload instanceof Command) {
                final Command command = (Command) payload;
                LOG.info(this.getName() + " receives command " + command.getName() + " with parametes {" + command.getProperties() + "}");
                Runnable executorThread = new Runnable() {
                    @Override
                    public void run() {
                        try {
                            lastDestination = message.getJMSReplyTo();
                            onCommand(command);
                        } catch (JMSException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        } catch (IOException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        } catch (UnableToExecuteException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    }
                };
                executor.submit(executorThread);
            } else {
                if (payload instanceof EventTemplate) {
                    final EventTemplate event = (EventTemplate) payload;
                    onEvent(event);
                }
            }
        } catch (JMSException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Used by childs of this class to send back a reply after a command is
     * executed.
     *
     * @param command
     */
    public void sendBack(Command command) {
        if (command.getReplyTimeout() > 0) { //a sendBack is expected
        	busService.reply(command, lastDestination, "-1"); //sends back the command
        }
    }
}
