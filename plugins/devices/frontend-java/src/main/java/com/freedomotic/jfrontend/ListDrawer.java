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
package com.freedomotic.jfrontend;

import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.jfrontend.utils.SpringUtilities;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

/**
 *
 * @author Enrico Nicoletti
 */
public class ListDrawer extends Renderer {

    JComboBox cmbZone = new JComboBox();
    JPanel panel = new JPanel();
    Protocol master;

    /**
     *
     * @param master
     */
    public ListDrawer(JavaDesktopFrontend master) {
        super(master);
        this.master = master;
        cmbZone.removeAllItems();
        cmbZone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZoneLogic zone = (ZoneLogic) cmbZone.getSelectedItem();
                enlistObjects(zone);
            }
        });
        this.setLayout(new BorderLayout());
        this.setBackground(Color.white);
        JScrollPane scroll = new JScrollPane(panel);
        add(scroll);
        enlistZones();
        //autoselect the first element
        enlistObjects((ZoneLogic) cmbZone.getItemAt(0));
        //JScrollPane scroll = new JScrollPane(this);
        setPreferredSize(new Dimension(400, 300));
        //add(scroll);
        validate();
    }

    private void enlistZones() {
        for (ZoneLogic zone : getCurrEnv().getZones()) {
            cmbZone.addItem(zone);
        }
    }

    private void enlistObjects(ZoneLogic zone) {
        panel.removeAll();
        panel.updateUI();
        panel.setLayout(new SpringLayout());

        int row = 0;

        panel.add(new JLabel("Select a zone"));
        panel.add(cmbZone);
        panel.add(new JLabel(""));

        row++;

        if (zone.getPojo().getObjects().isEmpty()) {
            panel.add(new JLabel(""));
            panel.add(new JLabel("No objects in this zone"));
            panel.add(new JLabel(""));
            row++;
        } else {

            for (final EnvObject objPojo : zone.getPojo().getObjects()) {
                final EnvObjectLogic obj = master.getApi().things().findOne(objPojo.getUUID());

                //a coloumn with object name
                JLabel icon = new JLabel(renderSingleObject(obj.getPojo()));
                icon.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mouseClickObject(obj);
                    }
                });
                panel.add(icon);

                JTextArea lblName = new JTextArea(objPojo.getName() + "\n" + getCompleteDescription(obj));
                lblName.setBackground(getBackground());
                panel.add(lblName);
                //a configuration button with a listener
                JButton btnConfig = new JButton("Configure");
                btnConfig.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ObjectEditor objectEditor = new ObjectEditor(obj);
                        objectEditor.setVisible(true);
                    }
                });
                panel.add(btnConfig);
                row++;
            }
        }

        SpringUtilities.makeCompactGrid(panel,
                row, 3, //rows, cols
                5, 5, //initX, initY
                5, 5); //xPad, yPad
        validate();
    }

    private String getCompleteDescription(EnvObjectLogic obj) {
        StringBuilder description = new StringBuilder();
        description.append(obj.getPojo().getDescription()).append("\n");

        for (BehaviorLogic b : obj.getBehaviors()) {
            if (b.isActive()) {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Active]\n");
            } else {
                description.append(b.getName()).append(": ").append(b.getValueAsString())
                        .append(" [Inactive]\n");
            }
        }

        return description.toString();
    }

    private ImageIcon renderSingleObject(EnvObject obj) {
        if (obj != null) {
            if ((obj.getCurrentRepresentation().getIcon() != null)
                    && !obj.getCurrentRepresentation().getIcon().equalsIgnoreCase("")) {
                BufferedImage img = null;
                img = ResourcesManager.getResource(obj.getCurrentRepresentation().getIcon(),
                        48,
                        48); //-1 means no resizeing

                ImageIcon icon = new ImageIcon(img);

                return icon;
            }
        }

        return null;
    }

    /**
     *
     * @param obj
     */
    public void mouseClickObject(EnvObjectLogic obj) {
        ObjectReceiveClick event = new ObjectReceiveClick(this, obj, ObjectReceiveClick.SINGLE_CLICK);
        Freedomotic.sendEvent(event);
    }

    /**
     *
     * @param callout1
     */
    @Override
    public void createCallout(Callout callout1) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     *
     * @param b
     */
    @Override
    public void setNeedRepaint(boolean b) {
        enlistObjects((ZoneLogic) cmbZone.getSelectedItem());
    }
}
