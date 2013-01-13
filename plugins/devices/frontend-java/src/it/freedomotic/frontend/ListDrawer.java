/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.frontend;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.ResourcesManager;
import it.freedomotic.environment.ZoneLogic;
import it.freedomotic.events.ObjectReceiveClick;
import it.freedomotic.frontend.utils.SpringUtilities;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.objects.BehaviorLogic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SpringLayout;

/**
 *
 * @author enrico
 */
public class ListDrawer extends Drawer {

    JComboBox cmbZone = new JComboBox();

    public ListDrawer() {

        cmbZone.removeAllItems();
        cmbZone.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ZoneLogic zone = (ZoneLogic) cmbZone.getSelectedItem();
                enlistObjects(zone);
            }
        });
        enlistZones();
        //autoselect the first element
        enlistObjects((ZoneLogic) cmbZone.getItemAt(0));
        //JScrollPane scroll = new JScrollPane(this);
        setPreferredSize(new Dimension(400, 300));
        //add(scroll);
        validate();
    }

    private void enlistZones() {
        for (ZoneLogic zone : Freedomotic.environment.getZones()) {
            cmbZone.addItem(zone);
        }
    }

    private void enlistObjects(ZoneLogic zone) {
        removeAll();
        setLayout(new SpringLayout());
        int row = 0;
        if (zone.getPojo().getObjects().isEmpty()) {
            add(new JLabel("No objects in this zone"));
        }
        for (final EnvObject objPojo : zone.getPojo().getObjects()) {
            final EnvObjectLogic obj = EnvObjectPersistence.getObject(objPojo.getName());
            //a coloumn with object name
            JLabel icon = new JLabel(renderSingleObject(obj.getPojo()));
            icon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    mouseClickObject(obj);
                }
            });
            add(icon);
            StringBuilder description = new StringBuilder();

            JTextArea lblName = new JTextArea(objPojo.getName() + "\n\n" + getCompleteDescription(obj));
            lblName.setBackground(getBackground());
            add(lblName);
            //a configuration button with a listener
            JButton btnConfig = new JButton("Configure");
            btnConfig.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    ObjectEditor objectEditor = new ObjectEditor(obj);
                    objectEditor.setVisible(true);
                }
            });
            add(btnConfig);
            row++;
        }
        SpringUtilities.makeCompactGrid(this,
                row, 3, //rows, cols
                5, 5, //initX, initY
                5, 5);//xPad, yPad
        validate();
    }

    private String getCompleteDescription(EnvObjectLogic obj) {
        StringBuilder description = new StringBuilder();
        description.append(obj.getPojo().getDescription()).append("\n");
        for (BehaviorLogic b : obj.getBehaviors()) {
            if (b.isActive()) {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Active]\n");
            } else {
                description.append(b.getName()).append(": ").append(b.getValueAsString()).append(" [Inactive]\n");
            }
        }
        return description.toString();
    }

    private ImageIcon renderSingleObject(EnvObject obj) {
        if (obj != null) {
            if (obj.getCurrentRepresentation().getIcon() != null
                    && !obj.getCurrentRepresentation().getIcon().equalsIgnoreCase("")) {
                BufferedImage img = null;
                img = ResourcesManager.getResource(obj.getCurrentRepresentation().getIcon(), 48, 48); //-1 means no resizeing
                ImageIcon icon = new ImageIcon(img);
                return icon;
            }
        }
        return null;
    }

    public void mouseClickObject(EnvObjectLogic obj) {
        ObjectReceiveClick event = new ObjectReceiveClick(this, obj, ObjectReceiveClick.SINGLE_CLICK);
        Freedomotic.sendEvent(event);
    }

    @Override
    public void createCallout(Callout callout1) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setNeedRepaint(boolean b) {
        enlistObjects((ZoneLogic) cmbZone.getSelectedItem());
    }
}
