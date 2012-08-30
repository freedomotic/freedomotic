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
   String createTable ="CREATE TABLE IF NOT EXIST EVENTS"
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
       super("HarvesterProtocol", "/es.gpulido.harvester/harvester-protocol");       
       setPollingWait(-1); // disable polling
   }
    
    @Override
    protected void onRun() {
      
    }
    @Override
    public void onStart() {
     try {
            Class.forName("org.h2.Driver");
            
            //TODO: use config file database name
            String dbPath = Info.getDevicesPath()+File.separator +"/es.gpulido.harvester/"+"harvester";
            //Connection conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "");
            Connection conn = DriverManager.getConnection("jdbc:h2:"+dbPath, "sa", "");
            // add application code here
            Statement stat = conn.createStatement();                      
            //create Table
            stat.execute(createTable);                
            this.setDescription("Online");
        } catch (SQLException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(HarvesterProtocol.class.getName()).log(Level.SEVERE, null, ex);
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
            prep.setTimestamp(0,java.sql.Timestamp.valueOf(c.getProperty("date")));
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
