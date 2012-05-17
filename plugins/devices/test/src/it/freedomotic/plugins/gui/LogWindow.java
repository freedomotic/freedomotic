/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.gui;

import java.awt.BorderLayout;
import java.awt.Color;
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
    //JTextArea areaDetail = new JTextArea();
    private final Handler handler;

    public LogWindow(final Handler handler) {
        super("");
        this.handler = handler;
        setSize(300, 300);
        this.setLayout(new BorderLayout());
        model.addColumn("Level");
        //model.addColumn("Class");
        //model.addColumn("Method");
        model.addColumn("Message");
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.setDefaultRenderer(Object.class, new CustomRenderer());
        ListSelectionModel selectionModel = table.getSelectionModel();
        selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        selectionModel.addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent e) {
                //areaDetail.setText(table.getValueAt(e.getFirstIndex(), 3).toString());
            }
        });
        setColumnWidth(table.getColumnModel().getColumn(0), 70);
        //setColumnWidth(table.getColumnModel().getColumn(1), 200);
//        setColumnWidth(table.getColumnModel().getColumn(2), 150);
        cmbLevel.setSelectedItem("INFO");
        cmbLevel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                handler.setLevel(Level.parse(cmbLevel.getSelectedItem().toString()));
            }
        });
        add(new JLabel("Level: "), BorderLayout.PAGE_START);
        cmbLevel.setEditable(false);
        add(cmbLevel, BorderLayout.PAGE_START);
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), BorderLayout.CENTER);
//        areaDetail.setPreferredSize(new Dimension(300, 200));
//        areaDetail.setMinimumSize(new Dimension(300, 200));
//        add(areaDetail, BorderLayout.SOUTH);
    }

    public void append(Object[] row) {
        model.insertRow(0, row);
        if (model.getRowCount() > 100){
            model.removeRow(model.getRowCount() -1);
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
