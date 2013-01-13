/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.gpulido.harvester;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import it.freedomotic.util.Info;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Date;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpt
 */
public class HarvesterProtocol extends Protocol {

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

    public HarvesterProtocol() {
        super("HarvesterProtocol", "/es.gpulido.harvester/harvester-manifest.xml");
        this.setName("Harvester");
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
            if ("h2".equals(dbType))  shortDBType =  'h';
            if ("mysql".equals(dbType)) shortDBType = 'm';
            
            switch (shortDBType) {
                case ('h'):
                    dbBasePath = configuration.getStringProperty("dbpath", Info.getDevicesPath() + File.separator + "/es.gpulido.harvester/data");
                    dbPath = dbBasePath;
                    driverClass = "org.h2.Driver";
                    break;
                case ('m'):
                    dbBasePath = configuration.getStringProperty("dbpath", "localhost:3306");
                    dbPath = "//" + dbBasePath ;
                    driverClass="com.mysql.jdbc.Driver";
                    break;
                default:
                    dbPath = "";
            }
            Class.forName(driverClass);

            connection = DriverManager.getConnection("jdbc:" + dbType + ":" + dbPath + "/"+ dbName, dbUser, dbPassword);
            // add application code here
            Statement stat = connection.createStatement();
            //create Table
            stat.execute(createTable);
            this.setDescription("Connected to jdbc:" + dbType + ":" + dbPath + "/"+ dbName + " as user:"+ dbUser );
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
            stop();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
            this.setDescription("The " + driverClass + " Driver is not loaded");
            stop();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        try {
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            prep = connection.prepareStatement(insertStatement);
            System.out.println("Harvester: year: " + c.getProperty("event.date.year"));
            Timestamp ts = new java.sql.Timestamp(
                    Integer.parseInt(c.getProperty("event.date.year")) - 1900,
                    Integer.parseInt(c.getProperty("event.date.month")) -1,
                    Integer.parseInt(c.getProperty("event.date.day")),
                    Integer.parseInt(c.getProperty("event.time.hour")),
                    Integer.parseInt(c.getProperty("event.time.minute")),
                    Integer.parseInt(c.getProperty("event.time.second")),
                    0       );
            
            //Timestamp nts = new java.sql.Timestamp(System.currentTimeMillis());
            
            prep.setTimestamp(1, ts);
            prep.setString(2, c.getProperty("event.object.name"));
            prep.setString(3, c.getProperty("event.object.protocol"));
            prep.setString(4, c.getProperty("event.object.address"));

            //search for all objects behaviors changes    
            Pattern pat = Pattern.compile("^current\\.object\\.behavior\\.(.*)");
            for (Entry<Object, Object> entry : c.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                Matcher fits = pat.matcher(key);
                if (fits.find()) {
                    prep.setString(5, fits.group(1));
                    prep.setString(6, (String) entry.getValue());
                    prep.execute();
                }
            }

        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
