/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import java.awt.BorderLayout;
import java.util.Iterator;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

/**
 *
 * @author enrico
 */
public class ReactionsPanel
        extends JPanel {

    private AutomationsEditor plugin;
    private JPanel panel = new JPanel();
    private JScrollPane scrollPane;
    private final I18n I18n;
    private NlpCommand nlpCommands;

    /**
     * Creates new form ReactionList
     *
     * @param plugin
     */
    public ReactionsPanel(AutomationsEditor plugin, NlpCommand nlpCommands) {
        this.plugin = plugin;
        this.nlpCommands = nlpCommands;
        this.I18n = plugin.getApi().getI18n();
        init(null);
    }

    /**
     *
     * @param i18n
     * @param obj
     */
    public ReactionsPanel(I18n i18n, NlpCommand nlpCommands, EnvObjectLogic obj) {
        this.I18n = i18n;
        this.nlpCommands = nlpCommands;
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

        for (Trigger trigger : TriggerPersistence.getTriggers()) {
            if (!trigger.isHardwareLevel()) {
                //display already stored reactions related to this objects
                boolean found = false;
                int pos = 0;

                for (Reaction r : ReactionPersistence.getReactions()) {
                    if (r.getTrigger().equals(trigger) && !r.getCommands().isEmpty()) {
                        ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, r, this);
                        panel.add(editor, pos++);
                        found = true;
                    }
                }

                if (!found) { //add an empty reaction if none
                    pos = panel.getComponentCount();

                    ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, new Reaction(trigger), this);
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

        for (Trigger trigger : TriggerPersistence.getTriggers()) {
            boolean isRelated = false;

            if (!trigger.isHardwareLevel()) {
                Iterator it = trigger.getPayload().iterator();

                //chack if this trigger is related toi the object and set a flag
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
                    for (Reaction r : ReactionPersistence.getReactions()) {
                        if (r.getTrigger().equals(trigger)) {
                            ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, r, this);
                            panel.add(editor);
                            alreadyStored = true;
                        }
                    }

                    if (!alreadyStored) { //add an empty reaction if none

                        ReactionEditor editor = new ReactionEditor(I18n, nlpCommands, new Reaction(trigger), this);
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
