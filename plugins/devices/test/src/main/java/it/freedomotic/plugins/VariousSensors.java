/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.plugins.gui.VariousSensorsGui;
import it.freedomotic.api.Sensor;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class VariousSensors extends Protocol {

    int hour = 0, month = 1;
    Boolean powered = false;

    public VariousSensors() {
        super("Sensors Simulator", "/it.nicoletti.test/sensors-simulator.xml");
        setPollingWait(2000);
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(new VariousSensorsGui(this));
    }

    public void askSomething() {
        final Command c = new Command();
        c.setName("Ask something silly to user");
        c.setDelay(0);
        c.setExecuted(true);
        c.setEditable(false);
        c.setReceiver("app.actuators.frontend.javadesktop.in");
        c.setProperty("question", "<html><h1>Do you like Freedomotic?</h1></html>");
        c.setProperty("options", "Yes, it's good; No, it sucks; I don't know");
        c.setReplyTimeout(10000); //10 seconds

        new Thread(new Runnable() {

            public void run() {
                VariousSensorsGui guiHook = (VariousSensorsGui) gui;
                Command reply = send(c);
                if (reply != null) {
                    String userInput = reply.getProperty("result");
                    if (userInput != null) {
                        guiHook.updateDescription("The reply to the test question is " + userInput);
                    } else {
                        guiHook.updateDescription("The user has not responded to the question within the given time");
                    }
                } else {
                    guiHook.updateDescription("Unreceived reply within given time (10 seconds)");
                }
            }
        }).start();
    }

    @Override
    protected void onRun() {
        //sends a fake sensor read event
        ProtocolRead event = new ProtocolRead(this, "test", "test");
        event.getPayload().addStatement("value", powered.toString());
        event.getPayload().addStatement("object.class", "Light");
        event.getPayload().addStatement("object.name", "Created by VariousSensors");
        //invert the value for the next round
        notifyEvent(event);
        if (powered) {
            powered = false;
        } else {
            powered = true;
        }
        //wait two seconds before sending another event
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
             // Restore the interrupted status
             Thread.currentThread().interrupt();
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
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
}
