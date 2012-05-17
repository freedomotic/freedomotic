package it.freedomotic.persistence;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class EnvironmentPersistence {

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
            new File(folder + "/objects").mkdir();
        }
        save(new File(folder + "/" + fileName + ".xenv"));
        //TODO: Freedomotic.environment.getPojo().setObjectsFolder()
        EnvObjectPersistence.saveObjects(new File(folder + "/objects"));
    }

    public static EnvironmentLogic load(final File file) throws IOException {
        Freedomotic.logger.info("-- Initialization of Environment --");
        Freedomotic.logger.log(Level.INFO, "Deserializing environment from {0}", file.toString());
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
            envLogic.init();
        } catch (Exception ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
        }
        return envLogic;
    }
}
