/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.environment.EnvironmentDAO;

import it.freedomotic.exceptions.DaoLayerException;

import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;

import it.freedomotic.objects.EnvObjectPersistence;

import it.freedomotic.persistence.FreedomXStream;

import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public class EnvironmentDAOXstream
        implements EnvironmentDAO {

    private File directory;
    private boolean savedAsNewEnvironment;

    @Inject
    EnvironmentDAOXstream(@Assisted File directory) {
        this.directory = directory;
    }

    public boolean isSavedAsNewEnvironment() {
        return savedAsNewEnvironment;
    }

    public void setSavedAsNewEnvironment(boolean saveAsNewEnvironment) {
        this.savedAsNewEnvironment = saveAsNewEnvironment;
    }

    /**
     * Persists an environment on filesystem using XStream as serialization
     * engine (XML)
     *
     * @param environment
     * @throws DaoLayerException
     */
    @Override
    public void save(Environment environment)
            throws DaoLayerException {
        if (!directory.isDirectory()) {
            throw new DaoLayerException(directory.getAbsoluteFile() + " is not a valid environment folder. Skipped");
        }

        if (this.isSavedAsNewEnvironment()) {
            try {
                saveAs(environment);
            } catch (IOException ex) {
                throw new DaoLayerException(ex);
            }
        } else {
            delete(environment);

            try {
                // Create file
                StringBuilder summary = new StringBuilder();
                //print an header for the index.txt file
                summary.append("#Filename \t\t #EnvName").append("\n");

                String uuid = environment.getUUID();

                if ((uuid == null) || uuid.isEmpty()) {
                    environment.setUUID(UUID.randomUUID().toString());
                }

                String fileName = environment.getUUID() + ".xenv";
                serialize(environment,
                        new File(directory + "/" + fileName));
                summary.append(fileName).append("\t").append(environment.getName()).append("\n");

                //writing a summary .txt file with the list of commands in this folder
                FileWriter fstream = new FileWriter(directory + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } catch (IOException ex) {
                throw new DaoLayerException(ex);
            }
        }
    }

    public void saveAs(Environment env)
            throws IOException {
        String fileName = directory.getName();

        if (!directory.exists()) {
            directory.mkdir();
            new File(directory + "/data").mkdir();
            new File(directory + "/data/obj").mkdir();
            new File(directory + "/data/rea").mkdir();
            new File(directory + "/data/trg").mkdir();
            new File(directory + "/data/cmd").mkdir();
            new File(directory + "/data/resources").mkdir();
        }

        serialize(env,
                new File(directory + "/" + fileName + ".xenv"));

        //TODO: Freedomotic.environment.getPojo().setObjectsFolder()
        //  EnvObjectPersistence.saveObjects(new File(folder + "/objects"));
    }

    @Override
    public void delete(Environment environment)
            throws DaoLayerException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Loads an environment from filesystem using XStream as serialization
     * engine (XML)
     *
     * @return an Environment object or null if no environments are found in the
     * given folder
     * @throws DaoLayerException
     */
    @Override
    public Environment load()
            throws DaoLayerException {
        if (directory == null) {
            throw new DaoLayerException("Cannot");
        }

        // This filter only returns env files
        FileFilter envFileFilter =
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isFile() && file.getName().endsWith(".xenv")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };

        File[] files = directory.listFiles(envFileFilter);

        for (File file : files) {
            Environment environment = deserialize(file);
            EnvObjectPersistence.loadObjects(new File(directory + "/data/obj"),
                    false);

            return environment;
        }

        return null;
    }

    private void serialize(Environment env, File file)
            throws IOException {
        XStream xstream = FreedomXStream.getEnviromentXstream();

        for (Zone zone : env.getZones()) {
            zone.setObjects(null);
        }

        String xml = xstream.toXML(env);
        FileWriter fstream;
        BufferedWriter out = null;

        try {
            LOG.config("Serializing environment to " + file);
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            LOG.info("Application environment " + env.getName() + " succesfully serialized");
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    public static Environment deserialize(final File file)
            throws DaoLayerException {
        XStream xstream = FreedomXStream.getXstream();

        //validate the object against a predefined DTD
        String xml;

        try {
            xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/environment.dtd");
        } catch (IOException ex) {
            throw new DaoLayerException(ex.getMessage(), ex);
        }

        Environment pojo = null;

        try {
            pojo = (Environment) xstream.fromXML(xml);

            return pojo;
        } catch (XStreamException e) {
            throw new DaoLayerException("XML parsing error. Readed XML is \n" + xml, e);
        }

//        EnvironmentLogic envLogic = new EnvironmentLogic();
//        if (pojo == null) {
//            throw new IllegalStateException("Object data cannot be null at this stage");
//        }
//        envLogic.setPojo(pojo);
//        envLogic.setSource(file);
//        // next line is commented as the method init() is called in the add()
//        //envLogic.init();
//        add(envLogic, false);
    }
    private static final Logger LOG = Logger.getLogger(EnvironmentDAOXstream.class.getName());
}
