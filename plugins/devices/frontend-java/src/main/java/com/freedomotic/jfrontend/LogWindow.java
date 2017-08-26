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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.apache.log4j.Logger;
import com.freedomotic.i18n.I18n;
import com.freedomotic.util.LogFormatter;

/**
 *
 * @author Enrico Nicoletti
 */
public class LogWindow extends JFrame {

    final int MAX_TABLE_ROWS = 100;
    DefaultTableModel model = new DefaultTableModel();
    String[] levels = {
        "ALL",
        "TRACE",
        "DEBUG",
        "INFO",
        "WARN",
        "ERROR",
        "OFF",};
    JComboBox cmbLevel = new JComboBox(levels);
    JTable table = new JTable(model);
    JTextPane areaDetail = new JTextPane();
    JToggleButton btnStop = new JToggleButton();
    private final transient Logger printableLogger;
    private final transient I18n I18N;

    /**
     *
     * @param i18n
     * @param handler
     */
    public LogWindow(I18n i18n, final Logger printableLogger) {
        super("Log Window");
        this.I18N = i18n;
        this.printableLogger = printableLogger;
        setSize(600, 400);
        this.setLayout(new BorderLayout());
        areaDetail.setContentType("text/html");
        model.addColumn(I18N.msg("log_level"));
        model.addColumn(I18N.msg("message"));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDefaultRenderer(Object.class, new CustomRenderer());
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (table.getSelectedRow() != -1) {
                        String text = table.getValueAt(table.getSelectedRow(), 1).toString();
                        if (text != null && !text.isEmpty()) {
                            areaDetail.setText("<html>" + LogFormatter.formatTextToHTML(text) + "</html>");
                        }
                    }
                }
            }
        });
        setColumnWidth(table.getColumnModel().getColumn(0), 70);
        cmbLevel.setSelectedItem("ALL");
        cmbLevel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	printableLogger.setLevel(org.apache.log4j.Level.toLevel(cmbLevel.getSelectedItem().toString()));
            }
        });
        add(new JLabel(I18N.msg("log_level") + ": "), BorderLayout.NORTH);
        cmbLevel.setEditable(false);
        add(cmbLevel, BorderLayout.NORTH);
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        areaDetail.setPreferredSize(new Dimension(600, 100));
        areaDetail.setMinimumSize(new Dimension(600, 100));
        JScrollPane scroll = new JScrollPane(areaDetail, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.SOUTH);
    }

    /**
     *
     * @param row
     */
    public synchronized void append(Object[] row) {
        if (table.getSelectedRow() == MAX_TABLE_ROWS) {
            table.clearSelection();
        }
        model.insertRow(0, row);
        if (model.getRowCount() > MAX_TABLE_ROWS && !btnStop.isSelected()) {
            model.removeRow(model.getRowCount() - 1);
        }
    }

    private void setColumnWidth(TableColumn column, int width) {
        column.setPreferredWidth(width);
        column.setMaxWidth(width);
    }

    class CustomRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return c;
        }
    }
    
	protected Logger getPrintableLogger() {
		return this.printableLogger;
	}
}
