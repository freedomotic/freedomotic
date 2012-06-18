/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.api.Tool;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.plugins.gui.CustomizeCommand;
import it.freedomotic.plugins.gui.ReactionList;
import it.freedomotic.reactions.Command;
import java.io.IOException;

/**
 *
 * @author enrico
 */
public class AutomationsEditor extends Protocol {

    public AutomationsEditor() {
        super("Automations Editor", "/it.nicoletti.test/automations-editor.xml");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (c.getProperty("editor").equalsIgnoreCase("command")){
            Command command = CommandPersistence.getCommand(c.getProperty("editable"));
            ReactionList reactionList = new ReactionList(this);
            CustomizeCommand cc = new CustomizeCommand(reactionList, command);
            cc.setVisible(true);
        }
    }

    @Override
    public void onStart() {
    }

    @Override
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

    @Override
    protected void onRun() {
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
