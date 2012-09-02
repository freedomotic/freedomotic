/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Actuator;
import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Profiler;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.gui.ClockForm;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 *
 * @author Enrico
 */
public class Performances extends Protocol {

    public Performances() {
        super("Performances Report", "/it.nicoletti.test/performances.xml");
    }

    @Override
    public void onStart() {
    }

    public void onShowGui() {
        JOptionPane.showMessageDialog(null,
                Profiler.print(),
                "Performances Report",
                JOptionPane.PLAIN_MESSAGE);
        stop();
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
    protected void onRun() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
