/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.gpulido.harvester;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
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
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpt
 */
public class HarvesterProtocol extends Protocol {

    Connection connection;
    PreparedStatement prep;
    String createTable = "CREATE TABLE fdEVENTS"
            + " (ID int auto_increment, DATE dateTime, OBJECT VARCHAR(200),PROTOCOL VARCHAR(200), ADDRESS VARCHAR(200), BEHAVIOR VARCHAR(200), VALUE VARCHAR(20), "
            + "PRIMARY KEY (ID))";
    String createMSSQLTable = "CREATE TABLE fdEVENTS"
            + " (ID int identity, DATE dateTime, OBJECT VARCHAR(200),PROTOCOL VARCHAR(200), ADDRESS  VARCHAR(200), BEHAVIOR VARCHAR(200), VALUE VARCHAR(20), "
            + "PRIMARY KEY (ID));";
    String insertStatement = "INSERT INTO fdEVENTS"
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
        setDescription("Starting...");
        String dbType = configuration.getStringProperty("driver", "h2");
        String dbUser = configuration.getStringProperty("dbuser", "sa");
        String dbPassword = configuration.getStringProperty("dbpassword", "");
        String dbName = configuration.getStringProperty("dbname", "harvester");
        String driverClass = "";
        String dbBasePath, dbConnString = "jdbc:", dbPath = null;
        try {
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

            switch (shortDBType) {
                case ('h'):
                    dbBasePath = configuration.getStringProperty("dbpath", Info.getDevicesPath() + File.separator + "/es.gpulido.harvester/data");
                    dbPath = dbBasePath;
                    driverClass = "org.h2.Driver";
                    dbConnString += dbType + ":" + dbPath + "/" + dbName + ";user=" + dbUser + ";password=" + dbPassword + ";";
                    break;
                case ('m'):
                    dbBasePath = configuration.getStringProperty("dbpath", "localhost");
                    dbPath = "//" + dbBasePath;
                    driverClass = "com.mysql.jdbc.Driver";
                    dbConnString += dbType + ":" + dbPath + "/" + dbName + "?user=" + dbUser + "&password=" + dbPassword;
                    break;
                case ('s'):
                    dbBasePath = configuration.getStringProperty("dbpath", "localhost");
                    dbPath = "//" + dbBasePath;
                    driverClass = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    dbConnString += dbType + ":" + dbPath + ";databaseName=" + dbName + ";user=" + dbUser + ";password=" + dbPassword + ";";
                    break;
                default:
                    dbPath = "";
            }
            Class.forName(driverClass);
            connection = DriverManager.getConnection(dbConnString);
            // due to incompatible "if table not exists" statement, that's a general way of sorting the problem
            ResultSet tables = connection.getMetaData().getTables(null, null, "fdEVENTS", null);
            if (tables.next()) { // Table exists: DO NOTHING 
                //  Freedomotic.logger.warning(tables.getString(1) + ":" + tables.getString(2) + ":" +tables.getString(3));
            } else { // Table does not exist: CREATE IT
                Statement stat = connection.createStatement();
                if (shortDBType == 's') {
                    stat.execute(createMSSQLTable);
                } else {
                    stat.execute(createTable);
                }
            }
            this.setDescription("Connected to jdbc:" + dbConnString + " as user:" + dbUser);
        } catch (SQLException ex) {
            Freedomotic.logger.severe("Connecting to jdbc:" + dbConnString + " as user:" + dbUser);
            Freedomotic.logger.severe(ex.getLocalizedMessage());
            ex.printStackTrace();
            stop();
        } catch (ClassNotFoundException ex) {
            Freedomotic.logger.severe("Cannot load class: " + ex.getLocalizedMessage());
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
            Freedomotic.logger.severe(ex.getLocalizedMessage());
        }
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (connection != null) {
            try {

                prep = connection.prepareStatement(insertStatement);
                //System.out.println("Harvester: year: " + c.getProperty("event.date.year"));
                Timestamp ts = new java.sql.Timestamp(
                        Integer.parseInt(c.getProperty("event.date.year")) - 1900,
                        Integer.parseInt(c.getProperty("event.date.month")) - 1,
                        Integer.parseInt(c.getProperty("event.date.day")),
                        Integer.parseInt(c.getProperty("event.time.hour")),
                        Integer.parseInt(c.getProperty("event.time.minute")),
                        Integer.parseInt(c.getProperty("event.time.second")),
                        0);

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
                Freedomotic.logger.severe(ex.getLocalizedMessage());
            }
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
