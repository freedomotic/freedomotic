/**
 * Copyright (c) 2009-2022 Freedomotic Team
 * http://www.freedomotic-iot.com
 * <p>
 * This file is part of Freedomotic
 * <p>
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 * <p>
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.jfrontend.automationeditor;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerRepository;
import com.google.inject.Inject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * AutomationsEditor is the gui for managing the automations.
 * It is one frame with many triggers in it and commands that will
 * happen when the case triggers. Its get its trigger form the trigger
 * repository and the commands from the command Repository.
 *
 * @author Enrico Nicoletti
 */
public class AutomationsEditor extends Protocol {

    private static final String AUTOMATIONS_EDITOR = "Automations Editor";
    private static final String MANIFEST = "/frontend-java/automations-editor.xml";
    private static final int WIDTH = 700;
    private static final int HEIGHT = 600;

    @Inject
    private NlpCommand nlpCommands;
    @Inject
    private TriggerRepository triggerRepository;
    @Inject
    private CommandRepository commandRepository;
    @Inject
    private ReactionRepository reactionRepository;

    private ReactionsPanel panel;
    private JFrame frame;

    /**
     * Create a new Protocol with the plugin for Automations Editor.
     */
    public AutomationsEditor() {
        super(AUTOMATIONS_EDITOR, MANIFEST);
    }

    /**
     * on start does nothing see onShowGui for more information about this class.
     */
    @Override
    public void onStart() {
        // do nothing
    }

    /**
     * Builds the gui of AutomationsEditor, a frame with a ReactionsPanel and an ok button.
     * ReactionsPanel have component according to the nlpCommands,
     * triggerRepository, commandRepository and the reactionRepository .
     * Each component is a test case by some trigger and near it you have the commands
     * that will happen to each specific trigger.
     *
     */
    @Override
    public void onShowGui() {
        if (frame != null) {
            frame.removeAll();
        }
        frame = new JFrame();
        frame.setTitle(getApi().getI18n().msg("manage") + getApi().getI18n().msg("automations"));
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        panel = new ReactionsPanel(this, nlpCommands, triggerRepository, commandRepository, reactionRepository);
        frame.setContentPane(panel);

        JButton save = new JButton(getApi().getI18n().msg("save"));
        save.addActionListener((ActionEvent e) -> {
            for (Component component : panel.getPanel().getComponents()) {
                if (component instanceof ReactionEditor) {
                    ReactionEditor editor = (ReactionEditor) component;
                    editor.finalizeEditing();
                }
            }
            frame.dispose();
        });
        frame.add(save, BorderLayout.SOUTH);
        frame.pack();
        bindGuiToPlugin(frame);
    }

    /**
     *  dispose the gui.
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
    protected void onCommand(Command command)
            throws IOException, UnableToExecuteException {
        if (command.getProperty("editor").equalsIgnoreCase("command")) {
            List<Command> editableCommands = commandRepository.findByName(command.getProperty("editable"));

            if (!editableCommands.isEmpty()) {
                command = editableCommands.get(0);
            } else {
                throw new UnableToExecuteException("No commands found with name \"" + command.getProperty("editable") + "\"");
            }

            CustomizeCommand customizeCommand = new CustomizeCommand(getApi().getI18n(), command, commandRepository);
            customizeCommand.setVisible(true);
            customizeCommand.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshMainView();
                }
            });
        } else {
            if (command.getProperty("editor").equalsIgnoreCase("trigger")) {
                Trigger trigger = getApi().triggers().findByName(command.getProperty("editable")).get(0);
                CustomizeTrigger ct = new CustomizeTrigger(getApi().getI18n(), trigger, triggerRepository);
                ct.setVisible(true);
                ct.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        refreshMainView();
                    }
                });
            }
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onRun() {
        // do nothing
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void refreshMainView() {
        frame.dispose();
        this.showGui();
    }
}
