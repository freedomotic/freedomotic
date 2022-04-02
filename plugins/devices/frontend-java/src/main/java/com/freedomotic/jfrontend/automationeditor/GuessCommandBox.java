/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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

import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.exceptions.NoResultsException;
import com.freedomotic.reactions.Command;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.Nlp;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

/**
 *
 * @author Enrico Nicoletti
 */
public class GuessCommandBox
        extends JTextField {

    private Command command = null;
    private ReactionEditor editor;
    private transient NlpCommand nlpCommands;
    private final JButton btnAdd = new JButton();
    private final JButton btnCustomize;
    private final GuessCommandBox me = this;
    private final String ERROR_MESSAGE;
    private final String INFO_MESSAGE;
    private final transient I18n I18n;
    private transient CommandRepository commandRepository;

    /**
     *
     * @param i18n
     * @param editor
     * @param nlpCommands
     * @param commandRepository
     */
    public GuessCommandBox(I18n i18n, ReactionEditor editor, NlpCommand nlpCommands, CommandRepository commandRepository) {
        super();
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
        this.commandRepository = commandRepository;
        btnCustomize = new JButton(I18n.msg("edit"));
        ERROR_MESSAGE = I18n.msg("this_command_not_exists");
        INFO_MESSAGE = I18n.msg("write_here_command");

        this.editor = editor;
        init();
    }

    /**
     *
     * @param i18n
     * @param editor
     * @param nlpCommands
     * @param commandRepository
     * @param command
     */
    public GuessCommandBox(I18n i18n, ReactionEditor editor, NlpCommand nlpCommands, CommandRepository commandRepository, Command command) {
        super();
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
        this.commandRepository = commandRepository;
        btnCustomize = new JButton(I18n.msg("edit"));
        ERROR_MESSAGE = I18n.msg("this_command_not_exists");
        INFO_MESSAGE = I18n.msg("write_here_command");

        this.command = command;
        this.editor = editor;
        setEnabled(false);
        init();
    }

    private void init() {
        listen();
    }

    /**
     *
     * @return
     */
    public Command getCommand() {
        return command;
    }

    private void listen() {
        if (command != null) {
            setText(command.getName());
        } else {
            setText(INFO_MESSAGE);
        }

        this.setPreferredSize(new Dimension(300, 30));

        KeyListener keyListener
                = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent evt) {
                command = null;
                setForeground(Color.black);

                //for (String line : lines) {
                List<Nlp.Rank<Command>> mostSimilar = null;
                try {
                    mostSimilar = nlpCommands.computeSimilarity(getText(), 10);
                } catch (NoResultsException noResultsException) {
                    //do nothing if there are no results
                }
                JPopupMenu menu = new JPopupMenu();
                for (final Nlp.Rank<Command> rank : mostSimilar) {
                    JMenuItem menuItem = new JMenuItem(rank.getElement().toString());
                    menuItem.addActionListener((ActionEvent e) -> {
                        command = rank.getElement();
                        setText(command.getName());
                    });
                    menu.add(menuItem);
                }

                menu.show(evt.getComponent(),
                        evt.getComponent().getX() + (getText().length() * 10),
                        evt.getComponent().getY());
                requestFocus();
                new JComboBox();
            }
        };

        this.addKeyListener(keyListener);

        if (command == null) {
            btnAdd.setText(I18n.msg("confirm"));
            setToolTipText(I18n.msg("cmd_box_msg"));
        } else {
            btnAdd.setText(I18n.msg("remove"));
            setToolTipText(command.getDescription());
        }

        this.add(btnAdd);
        btnAdd.addActionListener((ActionEvent ae) -> {
            if (isEnabled()) {
                Command command1;
                List<Command> list = commandRepository.findByName(getText());
                if (!list.isEmpty()) {
                    command1 = list.get(0);
                } else {
                    throw new FreedomoticRuntimeException("No commands found with name " + getText());
                }
                if (command1 != null) {
                    setEnabled(false);
                    btnAdd.setText(I18n.msg("remove"));
                    editor.onCommandConfirmed(me);
                } else {
                    setForeground(Color.red);
                    setText(ERROR_MESSAGE);
                }
            } else {
                setEnabled(true);
                btnAdd.setText(I18n.msg("confirm"));
                editor.onCommandCleared(me);
                command = null;
                setText(INFO_MESSAGE);
            }
        });

        this.add(btnCustomize);
        btnCustomize.addActionListener((ActionEvent ae) -> {
            if (command != null) {
                Command c = new Command();
                c.setName("Edit a command");
                c.setReceiver("app.actuators.nlautomationseditor.nlautomationseditor.in");
                c.setProperty("editor", "command");
                c.setProperty("editable",
                        command.getName()); //the default choice
                Freedomotic.sendCommand(c);
                command = null;
                setText(INFO_MESSAGE);
            }
        });
    }

    /**
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        btnAdd.setBounds(getWidth() - 80, (getHeight() / 2) - 10, 70, 20);

        if ((command != null) && isEnabled()) {
            btnCustomize.setVisible(true);
            btnCustomize.setEnabled(true);
            btnCustomize.setBounds(getWidth() - 160, (getHeight() / 2) - 10, 70, 20);
        } else {
            btnCustomize.setVisible(false);
            btnCustomize.setEnabled(false);
        }
    }

    /**
     *
     * @param command
     */
    public void setCommand(Command command) {
        this.command = command;
    }
}
