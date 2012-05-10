/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class Successful extends Actuator {

    public Successful() {
        super("Successful Test", "/it.nicoletti.test/successful.xml");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
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
}
