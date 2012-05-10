/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.Tool;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.plugins.gui.ReactionList;
import it.freedomotic.reactions.Command;
import java.io.IOException;

/**
 *
 * @author enrico
 */
public class AutomationsEditor extends Tool {

    public AutomationsEditor() {
        super("Automations Editor", "/it.nicoletti.test/automations-editor.xml");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void onStart() {
    }

    public void onShowGui() {
        bindGuiToPlugin(new ReactionList(this));
    }

    @Override
    public void onStop() {
        //release resources
        gui.dispose();
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
