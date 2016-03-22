/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.JButton;

/**
 *
 * @author Enrico Nicoletti
 */
public class ReactionEditor
        extends javax.swing.JPanel {
    //private PropertiesPanel_1 panel = new PropertiesPanel_1(0, 1);

    private Reaction reaction;
    private List<GuessCommandBox> commandBoxes = new ArrayList<GuessCommandBox>();
    private Box cmdBox = Box.createVerticalBox();
    private Component parent = null;
    private final I18n I18n;
    private NlpCommand nlpCommands;
    private CommandRepository commandRepository;
    private ReactionRepository reactionRepository;

    /**
     * Creates new form ReactionEditor
     *
     * @param i18n
     * @param nlpCommands
     * @param commandRepository
     * @param parent
     * @param reaction
     * @param reactionRepository
     */
    public ReactionEditor(
            I18n i18n,
            NlpCommand nlpCommands,
            CommandRepository commandRepository,
            Reaction reaction,
            Component parent,
            ReactionRepository reactionRepository) {
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
        initComponents();
        this.reaction = reaction;
        this.parent = parent;
        init();
    }

    /**
     *
     * @param i18n
     * @param nlpCommands
     */
    public ReactionEditor(I18n i18n, NlpCommand nlpCommands) {
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
        initComponents();
        this.reaction = new Reaction();
        init();
    }

    private void init() {
        this.setLayout(new FlowLayout());

        //add trigger widget
        final Trigger trigger = reaction.getTrigger();
        final JButton btnTrigger = new JButton(trigger.getName());
        //btnTrigger.setEnabled(false);
        btnTrigger.setToolTipText(trigger.getDescription());
        btnTrigger.setPreferredSize(new Dimension(300, 30));
        btnTrigger.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (trigger != null) {
                    Command c = new Command();
                    c.setName("Edit a trigger");
                    c.setReceiver("app.actuators.nlautomationseditor.nlautomationseditor.in");
                    c.setProperty("editor", "trigger");
                    c.setProperty("editable",
                            trigger.getName()); //the default choice
                    c.setReplyTimeout(Integer.MAX_VALUE);

                    Freedomotic.sendCommand(c);
                    //String newTriggerName = reply.getProperty("edited");
                    //if (newTriggerName == null) {
                    //    throw new IllegalStateException("Trigger name in reply cannot be null");
                    //}
                    //btnTrigger.setName(newTriggerName);

                    //trigger = null;
                    //setText(INFO_MESSAGE);
                }
            }
        });
        this.add(btnTrigger, BorderLayout.WEST);
        this.add(cmdBox, BorderLayout.EAST);

        //add commands widget
        int i = 0;

        for (Command command : reaction.getCommands()) {
            GuessCommandBox box = new GuessCommandBox(I18n, this, nlpCommands, commandRepository, command);

            addBox(box);
        }

        //add empty command box to append new commands
        addEmptyBox();
    }

    private void addEmptyBox() {
        GuessCommandBox emptyBox = new GuessCommandBox(I18n, this, nlpCommands, commandRepository);
        addBox(emptyBox);
        this.validate();
        this.parent.validate();
    }

    private void addBox(GuessCommandBox box) {
        cmdBox.add(box);
        commandBoxes.add(box);
        this.validate();
        this.parent.validate();
    }

    private void removeBox(GuessCommandBox box) {
        cmdBox.remove(box);
        commandBoxes.remove(box);
        this.validate();
        this.parent.validate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(  )
    {
        setBorder( null );
        setLayout( new javax.swing.BoxLayout( this, javax.swing.BoxLayout.LINE_AXIS ) );
    } // </editor-fold>//GEN-END:initComponents
      // Variables declaration - do not modify//GEN-BEGIN:variables
      // End of variables declaration//GEN-END:variables

    void onCommandConfirmed(GuessCommandBox box) {
        int index = commandBoxes.indexOf(box);

        if (index >= reaction.getCommands().size()) { //the last box is now filled
            reaction.getCommands().add(box.getCommand());
            addEmptyBox();
        } else {
            reaction.getCommands().set(index,
                    box.getCommand());
        }

        reaction.setChanged();
        System.out.println("Temporary reaction added :" + reaction.toString());
    }

    void onCommandCleared(GuessCommandBox box) {
        //int index = list.indexOf(box);
        reaction.getCommands().remove(box.getCommand());
        removeBox(box);

        if (commandBoxes.size() <= reaction.getCommands().size()) {
            addEmptyBox();
        }

        reaction.setChanged();
        System.out.println("Temporary reaction removed :" + reaction.toString());
    }

    /**
     *
     * @return
     */
    public Reaction getReaction() {
        return reaction;
    }

    /**
     *
     */
    public void finalizeEditing() {
        reactionRepository.create(reaction);
    }
}
