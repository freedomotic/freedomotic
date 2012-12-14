package it.freedomotic.environment;

import it.freedomotic.objects.EnvObjectPersistence;
import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.persistence.FreedomXStream;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class EnvironmentPersistence {
    
    private EnvironmentPersistence(){
        //disable instance creation
    }

    public static void save(File file) throws IOException {
        XStream xstream = FreedomXStream.getEnviromentXstream();
        for (Zone zone : Freedomotic.environment.getPojo().getZones()) {
            zone.setObjects(null);
        }
        String xml = xstream.toXML(Freedomotic.environment.getPojo());
        FileWriter fstream;
        BufferedWriter out = null;
        try {
            Freedomotic.logger.config("Serializing environment to " + file);
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            Freedomotic.logger.info("Application environment succesfully serialized");
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    public static void saveAs(File folder) throws IOException {
        Freedomotic.logger.config("Serializing new environment to " + folder);
        String fileName = folder.getName();
        if (!folder.exists()) {
            folder.mkdir();
            new File(folder + "/data").mkdir();
            new File(folder + "/data/obj").mkdir();
            new File(folder + "/data/rea").mkdir();
            new File(folder + "/data/trg").mkdir();
            new File(folder + "/data/cmd").mkdir();
            new File(folder + "/data/resources").mkdir();
        }
        save(new File(folder + "/" + fileName + ".xenv"));
        //TODO: Freedomotic.environment.getPojo().setObjectsFolder()
        EnvObjectPersistence.saveObjects(new File(folder + "/objects"));
    }

    public static EnvironmentLogic load(final File file) throws IOException {
        final StringBuilder xml = new StringBuilder();
        FileInputStream fin;
        BufferedReader myInput = null;
        try {
            fin = new FileInputStream(file);
            myInput = new BufferedReader(new InputStreamReader(fin));
            String line = "";
            while (line != null) {
                line = myInput.readLine();
                xml.append(line);
            }
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
        Environment pojo;
        EnvironmentLogic envLogic = null;
        try {
            XStream xstream = FreedomXStream.getEnviromentXstream();
            pojo = (Environment) xstream.fromXML(xml.toString());
            envLogic = new EnvironmentLogic();
            envLogic.setPojo(pojo);
            envLogic.setSource(file);
            envLogic.init();
        } catch (Exception ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        }
        return envLogic;
    }
}
