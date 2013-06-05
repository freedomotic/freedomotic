/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.gui;

import it.freedomotic.util.i18n;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Handler;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class LogWindow extends JFrame {
    
    int MAX_TABLE_ROWS = 100;
    DefaultTableModel model = new DefaultTableModel();
    String[] levels = {
        "ALL",
        "FINEST",
        "FINER",
        "FINE",
        "CONFIG",
        "INFO",
        "WARNING",
        "SEVERE",
        "OFF",};
    JComboBox cmbLevel = new JComboBox(levels);
    JTable table = new JTable(model);
    JTextPane areaDetail = new JTextPane();
    JToggleButton btnStop = new JToggleButton();
    private final Handler handler;

    public LogWindow(final Handler handler) {
        super("Log Window");
        this.handler = handler;
        setSize(600, 400);
        this.setLayout(new BorderLayout());
        areaDetail.setContentType("text/html");
        model.addColumn(i18n.msg("log_level"));
        //model.addColumn("Class");
        //model.addColumn("Method");
        model.addColumn(i18n.msg("message"));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDefaultRenderer(Object.class, new CustomRenderer());
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (table.getSelectedRow() != -1) {
                        areaDetail.setText("<html>"
                                + table.getValueAt(table.getSelectedRow(), 1).toString()
                                + "</html>");
                    }
                }
            }
        });
        setColumnWidth(table.getColumnModel().getColumn(0), 70);
        cmbLevel.setSelectedItem("ALL");
        cmbLevel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                handler.setLevel(Level.parse(cmbLevel.getSelectedItem().toString()));
            }
        });
        add(new JLabel(i18n.msg("log_level") +": "), BorderLayout.NORTH);
        cmbLevel.setEditable(false);
        add(cmbLevel, BorderLayout.NORTH);
        //add(btnStop, BorderLayout.PAGE_START);
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        areaDetail.setPreferredSize(new Dimension(600, 100));
        areaDetail.setMinimumSize(new Dimension(600, 100));
        JScrollPane scroll = new JScrollPane(areaDetail, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        //areaDetail.setLineWrap(true);
        add(scroll, BorderLayout.SOUTH);
    }

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

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            return c;
        }
    }
}
