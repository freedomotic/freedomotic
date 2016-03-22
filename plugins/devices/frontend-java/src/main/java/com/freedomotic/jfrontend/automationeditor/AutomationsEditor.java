/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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
 *
 * @author Enrico Nicoletti
 */
public class AutomationsEditor extends Protocol {

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
     *
     */
    public AutomationsEditor() {
        super("Automations Editor", "/frontend-java/automations-editor.xml");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        if (c.getProperty("editor").equalsIgnoreCase("command")) {
            Command command;
            List<Command> list = commandRepository.findByName(c.getProperty("editable"));
            if (!list.isEmpty()) {
                command = list.get(0);
            } else {
                throw new RuntimeException("No commands found with name " + c.getProperty("editable"));
            }
//            ReactionList reactionList = new ReactionList(this);
            CustomizeCommand cc = new CustomizeCommand(getApi().getI18n(), command, commandRepository);
            cc.setVisible(true);
            cc.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    refreshMainView();
                }
            });
        } else {
            if (c.getProperty("editor").equalsIgnoreCase("trigger")) {
                Trigger trigger = getApi().triggers().findByName(c.getProperty("editable")).get(0);
                //ReactionList reactionList = new ReactionList(this);
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

    private void refreshMainView() {
        frame.dispose();
        this.showGui();
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
        if (frame != null) {
            frame.removeAll();
        }
        frame = new JFrame();
        frame.setTitle(getApi().getI18n().msg("manage") + getApi().getI18n().msg("automations"));
        frame.setPreferredSize(new Dimension(700, 600));

        panel = new ReactionsPanel(this, nlpCommands, triggerRepository, commandRepository, reactionRepository);
        frame.setContentPane(panel);

        JButton ok = new JButton(getApi().getI18n().msg("ok"));
        ok.addActionListener(new ActionListener() {
            @Override
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
