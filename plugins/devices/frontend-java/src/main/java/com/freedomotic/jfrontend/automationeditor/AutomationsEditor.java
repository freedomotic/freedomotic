/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.jfrontend.automationeditor;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 *
 * @author enrico
 */
public class AutomationsEditor
        extends Protocol {

    /**
     *
     */
    public AutomationsEditor() {
        super("Automations Editor", "/frontend-java/automations-editor.xml");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        if (c.getProperty("editor").equalsIgnoreCase("command")) {
            Command command = CommandPersistence.getCommand(c.getProperty("editable"));
//            ReactionList reactionList = new ReactionList(this);
            CustomizeCommand cc = new CustomizeCommand(getApi().getI18n(), command);
            cc.setVisible(true);
        } else {
            if (c.getProperty("editor").equalsIgnoreCase("trigger")) {
                Trigger trigger = TriggerPersistence.getTrigger(c.getProperty("editable"));
                //ReactionList reactionList = new ReactionList(this);
                CustomizeTrigger ct = new CustomizeTrigger(getApi().getI18n(), trigger);
                ct.setVisible(true);
            }
        }
    }

    /**
     *
     */
    @Override
    public void onStart() {
    }

    /**
     *
     */
    @Override
    public void onShowGui() {
        final JFrame frame = new JFrame();
        frame.setTitle(getApi().getI18n().msg("manage") + getApi().getI18n().msg("automations"));
        frame.setPreferredSize(new Dimension(700, 600));

        final ReactionsPanel panel = new ReactionsPanel(this);
        frame.setContentPane(panel);

        JButton ok = new JButton(getApi().getI18n().msg("ok"));
        ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                for (Component component : panel.getPanel().getComponents()) {
                    if (component instanceof ReactionEditor) {
                        ReactionEditor editor = (ReactionEditor) component;
                        editor.finalizeEditing();
                    }
                }

                frame.dispose();
            }
        });
        frame.add(ok, BorderLayout.SOUTH);
        frame.pack();
        bindGuiToPlugin(frame);
    }

    /**
     *
     */
    @Override
    public void onStop() {
        //release resources
        try {
            gui.dispose();
        } catch (Exception e) {
        }
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
