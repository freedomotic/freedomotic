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

package com.freedomotic.plugins.devices.harvester_chart;

import com.freedomotic.annotations.ListenEventsOn;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.ObjectReceiveClick;
import com.freedomotic.things.BehaviorLogic;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.EnvObjectPersistence;
import com.freedomotic.reactions.Command;
import com.freedomotic.util.Info;
import java.io.File;
import java.sql.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.*;
import javax.swing.JFrame;

/**
 *
 * @author gpt
 */
public class HarvesterChart extends Protocol {

    Connection connection;
    PreparedStatement prep;
    String createTable = "CREATE TABLE IF NOT EXISTS EVENTS"
            + " (ID bigint auto_increment, "
            + "DATE dateTime, "
            + "OBJECT VARCHAR(200),"
            + "PROTOCOL VARCHAR(200),"
            + "ADDRESS  VARCHAR(200),"
            + "BEHAVIOR VARCHAR(200),"
            + "VALUE VARCHAR(20), "
            + "PRIMARY KEY (ID))";
    String insertStatement = "INSERT INTO EVENTS"
            + " (DATE, OBJECT, PROTOCOL,ADDRESS,BEHAVIOR,VALUE) "
            + "VALUES (?,?,?,?,?,?)";

    public HarvesterChart() {
        super("HarvesterChart", "/harvester-chart/harvester-chart-manifest.xml");
        this.setName("HarvesterChart");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onRun() {
    }

    @Override
    public void onStart() {
        String dbType = configuration.getStringProperty("driver", "h2");
        String dbUser = configuration.getStringProperty("dbuser", "sa");
        String dbPassword = configuration.getStringProperty("dbpassword", "");
        String dbName = configuration.getStringProperty("dbname", "harvester");
        String driverClass = "";
        try {
            String dbBasePath, dbPath;
            char shortDBType = 'z';
            if ("h2".equals(dbType)) {
                shortDBType = 'h';
            }
            if ("mysql".equals(dbType)) {
                shortDBType = 'm';
            }
            if ("sqlserver".equals(dbType)) {
                shortDBType = 's';
            }
            if ("sqlite".equals(dbType)) {
                shortDBType = 'l';
            }
            //System.out.println("Wilson Kong Debug Message:" + dbType);
            switch (shortDBType) {
                case ('h'):
                    dbBasePath = configuration.getStringProperty("dbpath", Info.getDevicesPath() + File.separator + "/es.gpulido.harvester/data");
                    dbPath = dbBasePath;
                    driverClass = "org.h2.Driver";
                    break;
                case ('m'):
                    dbBasePath = configuration.getStringProperty("dbpath", "localhost");
                    dbPath = "//" + dbBasePath;
                    driverClass = "com.mysql.jdbc.Driver";
                    break;
                case ('s'):
                    dbBasePath = configuration.getStringProperty("dbpath", "localhost");
                    dbPath = "//" + dbBasePath;
                    driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    break;
                case ('l'):
                    dbBasePath = configuration.getStringProperty("dbpath", Info.getDevicesPath() + File.separator + "es.gpulido.harvester" + File.separator + "data");
                    dbPath = dbBasePath + File.separator + dbName;
                    driverClass = "org.sqlite.JDBC";
                    break;
                default:
                    dbPath = "";
            }
            //System.out.println("Wilson Kong Message: jdbc:" + dbType + ":" + dbPath + File.separator + dbName);
            Class.forName(driverClass);

            connection = DriverManager.getConnection("jdbc:" + dbType + ":" + dbPath, dbUser, dbPassword);
            // add application code here

            this.setDescription("Connected to jdbc:" + dbType + ":" + dbPath + File.separator + dbName + " as user:" + dbUser);
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterChart.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            System.out.println("Wilson Kong Error: " + ex.toString());
            //ex.printStackTrace();
            stop();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HarvesterChart.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            this.setDescription("The " + driverClass + " Driver is not loaded");
            System.out.println("Wilson Kong Error: " + ex.toString());
            stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //try {
        //connection.close();
        //} catch (SQLException ex) {
        //Logger.getLogger(HarvesterChart.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
        //}
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onCommand(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ListenEventsOn(channel = "app.event.sensor.object.behavior.clicked")
    public void onObjectClicked(EventTemplate event) {
        List<String> behavior_list = new ArrayList<String>();
        System.out.println("received event " + event.toString());
        ObjectReceiveClick clickEvent = (ObjectReceiveClick) event;
        //PRINT EVENT CONTENT WITH
        System.out.println(clickEvent.getPayload().toString());
        String objectName = clickEvent.getProperty("object.name");
        String protocol = clickEvent.getProperty("object.protocol");
        String address = clickEvent.getProperty("object.address");

        try {
            Statement stat = connection.createStatement();
            System.out.println("Protocol=" + protocol + ",Address=" + address);

            //for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("wifi_id")){
            //EnvObjectLogic object = EnvObjectPersistence.getObjectByName(objectName);

            for (EnvObjectLogic object : EnvObjectPersistence.getObjectByAddress(protocol, address)) {
                for (BehaviorLogic behavior : object.getBehaviors()) {
                    System.out.println(behavior.getName());
                }
            }

            //String query = "select date,value from events where protocol='"+clickEvent.getProperty("object.protocol")+"' and behavior='power' ORDER BY ID DESC LIMIT 1000;";
            String query = "select date,value from events where object='" + objectName + "' and behavior='power' ORDER BY ID DESC LIMIT 1000;";
            System.out.println(query);
            //String query = "select datetime(date, 'unixepoch', 'localtime') as TIME,value from events where protocol='remote_receiver' and behavior='button'";

            ResultSet rs = stat.executeQuery(query);
            //JFreeChart chart = ChartFactory.createLineChart("Test", "Id", "Score", dataset, PlotOrientation.VERTICAL, true, true, false); 

            //System.out.println("Wilson Kong Debug:"+rs.getLong("date"));
            final TimeSeries series = new TimeSeries("Data1", Millisecond.class);


            while (rs.next()) {
                Date resultdate = new Date(rs.getLong("date") * 1000);
                Millisecond ms_read = new Millisecond(resultdate);
                series.addOrUpdate(ms_read, rs.getDouble("value"));
                //series.add((Millisecond)rs.getLong("date"),(double)rs.getLong("value"));
            }
            XYDataset xyDataset = new TimeSeriesCollection(series);

            JFreeChart chart = ChartFactory.createTimeSeriesChart("Chart",
                    "TIME", "VALVE",
                    xyDataset,
                    true, // legend
                    true, // tooltips
                    false // urls
                    );
            ChartPanel chartPanel = new ChartPanel(chart);
            chartPanel.setPreferredSize(new java.awt.Dimension(800, 500));
            JFrame f = new JFrame("Chart");
            f.setContentPane(chartPanel);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.pack();
            f.setVisible(true);
            //if (...) {
            //MyFrame myFrame = new MyFrame();
            //bindGuiToPlugin(myFrame);
            //showGui(); //triggers the showing of your frame. Before it calls onShowGui()
            //}
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterChart.class.getName()).log(Level.SEVERE, ex.getLocalizedMessage());
            System.out.println("Wilson Kong Error: " + ex.toString());
            //ex.printStackTrace();
            stop();
        }
    }

    public void onShowGui() {
        //make your plugin do something when a gui is requested.
        //can also be empty if GUI is built on another method, or you can move here the creation of your GUI element
    }
}
