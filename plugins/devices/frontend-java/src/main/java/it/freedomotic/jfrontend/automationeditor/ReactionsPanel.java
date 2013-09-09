/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.jfrontend.automationeditor;

import it.freedomotic.objects.EnvObjectLogic;

import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.Statement;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.util.I18n.I18n;

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

    /**
     * Creates new form ReactionList
     */
    public ReactionsPanel(AutomationsEditor plugin) {
        this.plugin = plugin;
        this.I18n = plugin.getApi().getI18n();
        init(null);
    }

    public ReactionsPanel(I18n i18n, EnvObjectLogic obj) {
        this.I18n = i18n;
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
                        ReactionEditor editor = new ReactionEditor(I18n, r, this);
                        panel.add(editor, pos++);
                        found = true;
                    }
                }

                if (!found) { //add an empty reaction if none
                    pos = panel.getComponentCount();

                    ReactionEditor editor = new ReactionEditor(I18n, new Reaction(trigger),
                            this);
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
                            ReactionEditor editor = new ReactionEditor(I18n, r, this);
                            panel.add(editor);
                            alreadyStored = true;
                        }
                    }

                    if (!alreadyStored) { //add an empty reaction if none

                        ReactionEditor editor = new ReactionEditor(I18n, new Reaction(trigger),
                                this);
                        panel.add(editor);
                    }
                }
            }
        }

        validate();
    }

    public JPanel getPanel() {
        return panel;
    }
}
