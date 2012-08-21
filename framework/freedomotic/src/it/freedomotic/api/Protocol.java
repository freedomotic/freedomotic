package it.freedomotic.api;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.bus.BusConsumer;
import it.freedomotic.bus.CommandChannel;
import it.freedomotic.bus.EventChannel;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author Enrico
 */
public abstract class Protocol extends Plugin implements BusConsumer {

    private static final String ACTUATORS_QUEUE_DOMAIN = "app.actuators.";
    private int POLLING_WAIT_TIME = -1;
    private CommandChannel commandsChannel; //one to one messaging pattern
    private EventChannel eventsChannel; //one to many messaging pattern
    private SensorThread sensorThread;
    private volatile Destination lastDestination;

    protected abstract void onRun();

    protected abstract void onCommand(Command c) throws IOException, UnableToExecuteException;

    protected abstract boolean canExecute(Command c);

    protected abstract void onEvent(EventTemplate event);

    public Protocol(String pluginName, String manifest) {
        super(pluginName, manifest);
        POLLING_WAIT_TIME = -1; //just to be sure
        register();
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
        if (listenOn.equalsIgnoreCase("undefined")) {
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
            Freedomotic.logger.config("Sensor " + this.getName() + " notify event " + ev.getEventName() + ":" + ev.getPayload().toString());
            eventsChannel.send(ev, destination);
        }
    }

    @Override
    public void start() {
        if (!isRunning) {
            isRunning = true;
            onStart();
            sensorThread = new SensorThread();
            sensorThread.start();
        }
    }

    @Override
    public void stop() {
        if (isRunning) {
            isRunning = false;
            onStop();
            if (sensorThread != null) {
                sensorThread.interrupt();
            }
        }
    }

    public final void setPollingWait(int wait) {
        if (wait > 0) {
            POLLING_WAIT_TIME = wait;
        }
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
                ActuatorPerforms task;
                lastDestination = message.getJMSReplyTo();
                task = new ActuatorPerforms(command, message.getJMSReplyTo(), message.getJMSCorrelationID());
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

        public ActuatorPerforms(Command c, Destination reply, String correlationID) {
            this.command = c;
            this.reply = reply;
            this.correlationID = correlationID;
            this.setName("freedom-protocol-executor");
        }

        @Override
        public void run() {
            try {
                onCommand(command);
            } catch (IOException ex) {
                Logger.getLogger(Actuator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (UnableToExecuteException ex) {
                command.setExecuted(false);
            }
            if ((configuration.getBooleanProperty("automatic-reply-to-commands", true) == true) //default value is true
                    && (command.getReplyTimeout() > 0)) {
                commandsChannel.reply(command, reply, correlationID); //sends back the command marked as executed or not
            }
        }
    }

    public void reply(Command command) {
        commandsChannel.reply(command, lastDestination, "-1"); //sends back the command
    }

    private class SensorThread extends Thread {

        @Override
        public void run() {
            if (isPollingSensor()) {
                while (isRunning && isPollingSensor()) {
                    onRun();
                    try {
                        Thread.sleep(POLLING_WAIT_TIME);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Protocol.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            } else {
                if (isRunning) {
                    onRun();
                }
            }
        }
    }
}
