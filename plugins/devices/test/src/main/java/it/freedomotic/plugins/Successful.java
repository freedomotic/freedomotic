/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;

import it.freedomotic.events.ProtocolRead;

import it.freedomotic.exceptions.UnableToExecuteException;

import it.freedomotic.reactions.Command;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Successful
        extends Protocol {

    private Boolean powered = true;

    public Successful() {
        super("Successful Test", "/it.nicoletti.test/successful.xml");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            Logger.getLogger(Successful.class.getName()).log(Level.SEVERE, null, ex);
        }

        c.setExecuted(true);
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        //sends a fake sensor read event. Used for testing
        ProtocolRead event = new ProtocolRead(this, "test", "test");
        event.getPayload().addStatement("object.class", "Light");
        event.getPayload().addStatement("object.name", "myLight");
        event.getPayload().addStatement("value",
                powered.toString());
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
            Logger.getLogger(VariousSensors.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
