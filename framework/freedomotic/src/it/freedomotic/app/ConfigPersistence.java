/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.app;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.persistence.FreedomXStream;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gpt
 */
public class ConfigPersistence {

    public static void serialize(Config config, File file) {
        XStream xstream = FreedomXStream.getXstream();
        xstream.autodetectAnnotations(true);
        String xml = xstream.toXML(config);
        FileOutputStream fout;
        FileWriter fstream;
        try {
            fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            out.close();
            //Freedomotic.logger.info("  configuration succesfully serialized");
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Config deserialize(File file) throws IOException {
        Freedomotic.logger.config("Deserializing configuration from " + file.getAbsolutePath());
        XStream xstream = FreedomXStream.getXstream();
        xstream.autodetectAnnotations(true);
        String line;
        StringBuilder xml = new StringBuilder();

        FileInputStream fin = null;
        BufferedReader myInput = null;
        try {
            fin = new FileInputStream(file);
            myInput = new BufferedReader(new InputStreamReader(fin));
            while ((line = myInput.readLine()) != null) {
                xml.append(line).append("\n");
            }
            Config c = null;
            xstream.alias("config", Config.class);
            c = (Config) xstream.fromXML(xml.toString());
            c.setXmlFile(file);
            return c;
        } catch (FileNotFoundException fileNotFoundException) {
            Freedomotic.logger.warning(fileNotFoundException.getLocalizedMessage());
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }
        return new Config();
    }

    private ConfigPersistence() {
    }
}
