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

import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.TriggerRepository;
import java.awt.BorderLayout;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 *
 * @author Enrico Nicoletti
 */
public class ReactionsPanel
        extends JPanel {

    private AutomationsEditor plugin;
    private JPanel panel = new JPanel();
    private JScrollPane scrollPane;
    private final I18n I18n;
    private NlpCommand nlpCommands;
    private TriggerRepository triggerRepository;
    private ReactionRepository reactionRepository;
    private CommandRepository commandRepository;

    /**
     * Creates new form ReactionList
     *
     * @param plugin
     * @param nlpCommands
     * @param triggerRepository
     * @param reactionRepository
     * @param commandRepository
     */
    public ReactionsPanel(
            AutomationsEditor plugin,
            NlpCommand nlpCommands,
            TriggerRepository triggerRepository,
            CommandRepository commandRepository,
            ReactionRepository reactionRepository) {
        this.plugin = plugin;
        this.nlpCommands = nlpCommands;
        this.I18n = plugin.getApi().getI18n();
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
        this.reactionRepository = reactionRepository;
        init(null);
    }

    /**
     *
     * @param i18n
     * @param nlpCommands
     * @param triggerRepository
     * @param obj
     * @param reactionRepository
     * @param commandRepository
     */
    public ReactionsPanel(
            I18n i18n,
            NlpCommand nlpCommands,
            TriggerRepository triggerRepository,
            CommandRepository commandRepository,
            EnvObjectLogic obj,
            ReactionRepository reactionRepository) {
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
        this.triggerRepository = triggerRepository;
        this.reactionRepository = reactionRepository;
        this.commandRepository = commandRepository;
        init(obj);
    }

    private void init(EnvObjectLogic obj) {
        this.setLayout(new BorderLayout());
        scrollPane = new JScrollPane(panel);
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        add(scrollPane);

        if (obj == null) {
            populateAllAutomations();
        } else {
            populateObjAutomations(obj);
        }
    }

    private void populateAllAutomations() {
        panel.removeAll();

        for (Trigger trigger : triggerRepository.findAll()) {
            if (!trigger.isHardwareLevel()) {
                //display already stored reactions related to this objects
                boolean found = false;
                int pos = 0;

                for (Reaction r : reactionRepository.findAll()) {
                    if (r.getTrigger().equals(trigger) && !r.getCommands().isEmpty()) {
                        ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, commandRepository, r, this, reactionRepository);
                        panel.add(editor, pos++);
                        found = true;
                    }
                }

                if (!found) { //add an empty reaction if none
                    pos = panel.getComponentCount();

                    ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, commandRepository, new Reaction(trigger), this, reactionRepository);
                    panel.add(editor, pos++);
                }

                panel.add(new JSeparator(),
                        pos);
            }
        }

        validate();
    }

    private void populateObjAutomations(EnvObjectLogic object) {
        panel.removeAll();

        for (Trigger trigger : triggerRepository.findAll()) {
            boolean isRelated = false;

            if (!trigger.isHardwareLevel()) {
                Iterator it = trigger.getPayload().iterator();

                //chack if this trigger is related to the object and set a flag
                while (it.hasNext()) {
                    Statement statement = (Statement) it.next();

                    if (statement.getValue().contains(object.getPojo().getName())) {
                        isRelated = true; //is a trigger related with this object
                        break; //no need to check the other statements in current trigger
                    }
                }

                //if this trigger is related to this object
                if (isRelated) { //current trigger is related to this env object

                    boolean alreadyStored = false;

                    //display already stored reactions related to this objects
                    for (Reaction r : reactionRepository.findAll()) {
                        if (r.getTrigger().equals(trigger)) {
                            ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, commandRepository, r, this, reactionRepository);
                            panel.add(editor);
                            alreadyStored = true;
                        }
                    }

                    if (!alreadyStored) { //add an empty reaction if none

                        ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, commandRepository, new Reaction(trigger), this, reactionRepository);
                        panel.add(editor);
                    }
                }
            }
        }

        validate();
    }

    /**
     *
     * @return
     */
    public JPanel getPanel() {
        return panel;
    }
}
