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
import java.sql.*;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author gpt
 */
public class HarvesterProtocol extends Protocol{

   Connection connection;
   PreparedStatement prep;
   String createTable ="CREATE TABLE IF NOT EXISTS EVENTS"
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
     try {
            Class.forName("org.h2.Driver");
            String dbName = configuration.getStringProperty("dbname", "harvester");
            String dbPath = Info.getDevicesPath()+File.separator +"/es.gpulido.harvester/data/"+dbName;            
            connection = DriverManager.getConnection("jdbc:h2:"+dbPath, "sa", "");
            // add application code here
            Statement stat = connection.createStatement();                      
            //create Table
            stat.execute(createTable);            
            this.setDescription("Connected to " +dbName + " database");
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);            
            stop();
        } catch (ClassNotFoundException ex) {            
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
            this.setDescription("The H2 Driver is not loaded");
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
             Timestamp ts= new java.sql.Timestamp(
                    Integer.parseInt(c.getProperty("event.date.year")),
                    Integer.parseInt(c.getProperty("event.date.month")),
                    Integer.parseInt(c.getProperty("event.date.day")),
                    Integer.parseInt(c.getProperty("event.time.hour")),
                    Integer.parseInt(c.getProperty("event.time.minute")),
                    Integer.parseInt(c.getProperty("event.time.second")),
                    0                    
                    );
             prep.setTimestamp(1, ts);
             prep.setString(2,c.getProperty("event.object.name"));
             prep.setString(3,c.getProperty("event.object.protocol"));
             prep.setString(4,c.getProperty("event.object.address"));
            
             //search for all objects behaviors changes    
            Pattern pat = Pattern.compile("^current\\.object\\.behavior\\.(.*)");
            for (Entry<Object,Object> entry : c.getProperties().entrySet()) {
                String key = (String)entry.getKey();
                Matcher fits = pat.matcher(key);                
                if (fits.find())
                {                    
                    prep.setString(5,fits.group(1));
                    prep.setString(6,(String)entry.getValue());                    
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
