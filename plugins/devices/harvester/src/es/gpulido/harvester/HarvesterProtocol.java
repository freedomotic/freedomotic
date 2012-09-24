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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class HarvesterProtocol extends Protocol{

   Connection conn;
   PreparedStatement prep;
   String createTable ="CREATE TABLE IF NOT EXISTS EVENTS"
                    + " (ID bigint auto_increment, "
                    + "DATE dateTime, "
                    + "OBJECT VARCHAR(200),"
                    + "BEHAVIOR VARCHAR(200), "
                    + "VALUE VARCHAR(20), "
                    + "PRIMARY KEY (ID))";
   String insertStatement = "INSERT INTO EVENTS"
                    + " (ID, DATE, OBJECT, BEHAVIOR,VALUE) "
                    + "VALUES (?,?,?,?,?)";            
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
            Connection connection = DriverManager.getConnection("jdbc:h2:"+dbPath, "sa", "");
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
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.setDescription("Disconnected");
        setPollingWait(-1); // disable polling
    }
            
    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        try {
            prep = conn.prepareStatement(insertStatement);            
            prep.setTimestamp(0, 
            new java.sql.Timestamp(
                    Integer.parseInt(c.getProperty("year")),
                    Integer.parseInt(c.getProperty("month")),
                    Integer.parseInt(c.getProperty("day")),
                    Integer.parseInt(c.getProperty("hour")),
                    Integer.parseInt(c.getProperty("minute")),
                    Integer.parseInt(c.getProperty("second")),
                    0                    
                    ));                        
            prep.setString(1,c.getProperty("object"));
            prep.setString(2,c.getProperty("behavior"));
            prep.setString(3,c.getProperty("value"));         
            prep.executeQuery();
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
