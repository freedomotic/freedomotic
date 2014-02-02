/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.jfrontend.automationeditor;

import com.freedomotic.app.Freedomotic;

import com.freedomotic.reactions.Command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

/**
 *
 * @author enrico
 */
class GuessTriggerBox
        extends JComboBox {

    public GuessTriggerBox() {
    }
//                    btnTrigger.addActionListener(new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent ae) {
//                        if (trigger != null) {
//                            Command c = new Command();
//                            c.setName("Edit a trigger");
//                            c.setReceiver("app.actuators.nlautomationseditor.nlautomationseditor.in");
//                            c.setProperty("editor", "trigger");
//                            c.setProperty("editable", trigger.getName()); //the default choice
//                            Freedomotic.sendCommand(c);
//                            //trigger = null;
//                            //setText(INFO_MESSAGE);
//                        }
//                    }
//                });
}
