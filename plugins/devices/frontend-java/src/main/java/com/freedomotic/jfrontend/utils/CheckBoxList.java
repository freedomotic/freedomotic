/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.jfrontend.utils;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

/**
 *
 * @author nicoletti
 */
public class CheckBoxList
        extends JList {

    /**
     *
     */
    protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

    /**
     *
     */
    public CheckBoxList() {
        setCellRenderer(new CellRenderer());

        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int index = locationToIndex(e.getPoint());

                if (index != -1) {
                    JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
                    checkbox.setSelected(!checkbox.isSelected());
                    repaint();
                }
            }
        });

        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    /**
     *
     */
    protected class CellRenderer
            implements ListCellRenderer {

        /**
         *
         * @param list
         * @param value
         * @param index
         * @param isSelected
         * @param cellHasFocus
         * @return
         */
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            JCheckBox checkbox = (JCheckBox) value;
            checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
            checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
            checkbox.setEnabled(isEnabled());
            checkbox.setFont(getFont());
            checkbox.setFocusPainted(false);
            checkbox.setBorderPainted(true);
            checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);

            return checkbox;
        }
    }
}
