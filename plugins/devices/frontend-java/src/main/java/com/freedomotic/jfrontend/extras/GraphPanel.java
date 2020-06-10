/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.jfrontend.extras;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.model.charting.UsageData;
import com.freedomotic.model.charting.UsageDataFrame;
import com.freedomotic.behaviors.DataBehaviorLogic;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import java.awt.Color;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.swing.table.AbstractTableModel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.DateTickUnitType;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYStepRenderer;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Matteo Mazzoni
 */
public class GraphPanel extends javax.swing.JFrame {

    private static final Logger LOG = LoggerFactory.getLogger(GraphPanel.class.getName());
    private UsageDataFrame points = new UsageDataFrame();
    private transient EnvObjectLogic obj;
    private TimeSeries series;
    private JFreeChart chart;
    private String title;
    private transient Protocol master;

    /**
     * Creates new form GraphWindow
     *
     * @param master
     * @param obj
     */
    public GraphPanel(Protocol master, EnvObjectLogic obj) {
        this.master = master;
        this.obj = obj;
        this.title = obj.getPojo().getPhisicalAddress();
        initComponents();

        this.setTitle(title);
        reDraw();

    }

    /**
     *
     */
    public final void reDraw() {
        DataBehaviorLogic dbl = (DataBehaviorLogic) obj.getBehavior("data");
        if (dbl.isChanged()) {
            this.points.setData(dbl.getData());
            createChart(this.points, title);
            jRawDatatxt.setText(dbl.getValueAsString());
            dataTable.setModel(new FreedomoticTableModel());
            setVisible(true);
        }

    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     *
     * @return A chart.
     */
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        graphPanel = new javax.swing.JPanel();
        tabDataPanel = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        rawDataPanel = new javax.swing.JScrollPane();
        jRawDatatxt = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jSpinnerStartDate = new javax.swing.JSpinner();
        jSpinnerStopDate = new javax.swing.JSpinner();
        jButton2 = new javax.swing.JButton();
        jComboGranularity = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();

        graphPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab("Graph", graphPanel);

        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        tabDataPanel.setViewportView(dataTable);

        jTabbedPane1.addTab("TableData", tabDataPanel);

        jRawDatatxt.setColumns(20);
        jRawDatatxt.setRows(5);
        rawDataPanel.setViewportView(jRawDatatxt);

        jTabbedPane1.addTab("RawData", rawDataPanel);

        jLabel1.setText("Start Date");

        jLabel2.setText("End Date");

        jButton1.setText("Apply filters");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jSpinnerStartDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        jSpinnerStopDate.setModel(new javax.swing.SpinnerDateModel(new java.util.Date(), null, null, java.util.Calendar.SECOND));

        jButton2.setText("Redraw");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jComboGranularity.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Year", "Month", "Day", "Hour", "Minute", "Second" }));
        jComboGranularity.setSelectedIndex(3);

        jLabel3.setText("Granularity");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE))
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jSpinnerStartDate)
                    .addComponent(jSpinnerStopDate)
                    .addComponent(jComboGranularity, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(336, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(17, 17, 17)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinnerStartDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinnerStopDate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboGranularity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 193, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Filters", jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 376, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Command command;
        List<Command> list = master.getApi().commands().findByName("Ask data from the harvester");
        if (!list.isEmpty()) {
            command = list.get(0);
        } else {
            throw new FreedomoticRuntimeException("No commands found with the specified name");
        }

        Command cloned;
        if (command != null) {
            try {
                cloned = command.clone();

                cloned.setProperty("startDate", Long.toString(((Date) jSpinnerStartDate.getValue()).getTime()));
                cloned.setProperty("stopDate", Long.toString(((Date) jSpinnerStopDate.getValue()).getTime()));
                cloned.setProperty("QueryAddress", obj.getPojo().getPhisicalAddress());
                master.notifyCommand(cloned);

            } catch (CloneNotSupportedException ex) {
                LOG.error(ex.getMessage());
            }
        }


    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        reDraw();
    }//GEN-LAST:event_jButton2ActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable dataTable;
    private javax.swing.JPanel graphPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JComboBox jComboGranularity;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextArea jRawDatatxt;
    private javax.swing.JSpinner jSpinnerStartDate;
    private javax.swing.JSpinner jSpinnerStopDate;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JScrollPane rawDataPanel;
    private javax.swing.JScrollPane tabDataPanel;
    // End of variables declaration//GEN-END:variables

    private void createChart(UsageDataFrame points, String title) {
        series = new TimeSeries(title);

        for (UsageData d : points.getData()) {
            Date resultdate = d.getDateTime();
            Millisecond msRead = new Millisecond(resultdate);
            int poweredValue = -1;
            if (d.getObjBehavior().equalsIgnoreCase("powered")) {
                poweredValue = d.getObjValue().equalsIgnoreCase("true") ? 1 : 0;
            } else if (d.getObjBehavior().equalsIgnoreCase("brigthness")) {
                try {
                    poweredValue = Integer.parseInt(d.getObjValue());
                } catch (NumberFormatException ex) {
                    poweredValue = -1;
                }
            }
            series.addOrUpdate(msRead, poweredValue);
        }

        XYDataset xyDataset = new TimeSeriesCollection(series);

        chart = ChartFactory.createTimeSeriesChart("Chart",
                "TIME", "VALUE",
                xyDataset,
                true, // legend
                true, // tooltips
                false // urls
        );
        chart.setAntiAlias(true);
        // Set plot styles
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(2.0, 2.0, 2.0, 2.0));
        // Set series line styles
        plot.setRenderer(new XYStepRenderer());

        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setShapesVisible(true);
            renderer.setShapesFilled(true);
        }

        // Set date axis style
        DateAxis axis = (DateAxis) plot.getDomainAxis();

        String formatString = "MM-dd HH";
        DateTickUnitType dtut = DateTickUnitType.HOUR;

        if (jComboGranularity.getSelectedItem().equals("Year")) {
            formatString = "yyyy";
            dtut = DateTickUnitType.YEAR;
        } else if (jComboGranularity.getSelectedItem().equals("Month")) {
            axis.setDateFormatOverride(new SimpleDateFormat("yyyy-MM"));
            dtut = DateTickUnitType.MONTH;
        } else if (jComboGranularity.getSelectedItem().equals("Day")) {
            axis.setDateFormatOverride(new SimpleDateFormat("MM-dd"));
            dtut = DateTickUnitType.DAY;
        } else if (jComboGranularity.getSelectedItem().equals("Minute")) {
            formatString = "MM-dd HH:mm";
            dtut = DateTickUnitType.MINUTE;
        } else if (jComboGranularity.getSelectedItem().equals("Second")) {
            formatString = "HH:mm:SS";
            dtut = DateTickUnitType.SECOND;
        }

        DateFormat formatter = new SimpleDateFormat(formatString);
        DateTickUnit unit = new DateTickUnit(dtut, 1, formatter);
        axis.setTickUnit(unit);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
        graphPanel.removeAll();
        graphPanel.add(chartPanel);

    }

    /**
     *
     * @param ev
     */
    public void addDataFromEvent(EventTemplate ev) {
        Date d = new Date(ev.getCreation());
        Millisecond msRead = new Millisecond(d);
        int valut = ev.getProperty("object.behavior.powered").equalsIgnoreCase("true") ? 1 : 0;
        series.addOrUpdate(msRead, valut);
        chart.fireChartChanged();

    }

    /**
     *
     */
    public class FreedomoticTableModel extends AbstractTableModel {

        /**
         *
         * @return
         */
        @Override
        public int getRowCount() {
            return points.getData().size();
        }

        /**
         *
         * @return
         */
        @Override
        public int getColumnCount() {
            return 6;
        }

        /**
         *
         * @param rowIndex
         * @param columnIndex
         * @return
         */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            UsageData item = points.getData().get(rowIndex);
            switch (columnIndex) {
                case 0:
                    return item.getDateTime();
                case 1:
                    return item.getObjName();
                case 2:
                    return item.getObjProtocol();
                case 3:
                    return item.getObjAddress();
                case 4:
                    return item.getObjBehavior();
                case 5:
                    return item.getObjValue();
                default:
                    return null;
            }
        }

        /**
         *
         * @param columnIndex
         * @return
         */
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Date.class;
            } else {
                return String.class;
            }
        }
        private final String[] columnNames = {"Datetime", "Name", "Protocol", "Address", "Behavior", "Value"};

        /**
         *
         * @param column
         * @return
         */
        @Override
        public String getColumnName(int column) {
            return columnNames[column];
        }
    }
}
