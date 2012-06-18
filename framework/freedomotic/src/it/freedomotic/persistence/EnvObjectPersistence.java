package it.freedomotic.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
import com.thoughtworks.xstream.persistence.XmlArrayList;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.AlreadyExistentException;
import it.freedomotic.exceptions.NotValidElementException;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.objects.EnvObjectFactory;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import it.freedomotic.util.UidGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import org.xml.sax.SAXParseException;

/**
 *
 * @author Enrico
 */
public class EnvObjectPersistence {

    private static Map<String, EnvObjectLogic> objectList = new TreeMap<String, EnvObjectLogic>();

    public static Collection<EnvObjectLogic> getObjectList() {
        return objectList.values();
    }

    private EnvObjectPersistence() {
    }

    public static void saveObjects(File folder) {
        if (objectList.isEmpty()) {
            Freedomotic.logger.warning("There are no object to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }
        if (!folder.isDirectory()) {
            Freedomotic.logger.warning(folder.getAbsoluteFile() + " is not a valid object folder. Skipped");
            return;
        }
        XStream xstream = FreedomXStream.getXstream();
        deleteObjectFiles(folder);
        try {
            Freedomotic.logger.info("---- Saving objects to file in " + folder.getAbsolutePath() + " ----");
            // Create file
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #EnvObjectName \t\t\t #EnvObjectType \t\t\t #Protocol \t\t\t #Address").append("\n");
            for (EnvObjectLogic envObject : objectList.values()) {
                String uuid = envObject.getPojo().getUUID();
                if (uuid == null || uuid.isEmpty()) {
                    envObject.getPojo().setUUID(UUID.randomUUID().toString());
                }
                String fileName = envObject.getPojo().getUUID() + ".xobj";
                FileWriter fstream = new FileWriter(folder + "/" + fileName);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xstream.toXML(envObject.getPojo())); //persist only the data not the logic
                summary.append(fileName).append("\t").append(envObject.getPojo().getName()).append("\t").append(envObject.getPojo().getType()).append("\t").append(envObject.getPojo().getProtocol()).append("\t").append(envObject.getPojo().getPhisicalAddress()).append("\n");
                //Close the output stream
                out.close();
                fstream.close();
            }
            //writing a summary .txt file with the list of commands in this folder
            FileWriter fstream = new FileWriter(folder + "/index.txt");
            BufferedWriter indexfile = new BufferedWriter(fstream);
            indexfile.write(summary.toString());
            //Close the output stream
            indexfile.close();
        } catch (Exception e) {
            Freedomotic.logger.info(e.getLocalizedMessage());
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteObjectFiles(File folder) {
        File[] files = folder.listFiles();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xobj")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        files = folder.listFiles(objectFileFileter);
        for (File file : files) {
            file.delete();
        }
    }

    public synchronized static void loadObjects(File folder, boolean makeUnique) {
        Freedomotic.logger.info("-- Initialization of Objects --");
        Freedomotic.logger.info("Loading environment objects from: " + folder.getAbsolutePath());
        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            @Override
            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xobj")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        files = folder.listFiles(objectFileFileter);
        try {
            for (File file : files) {
                EnvObjectLogic loaded = loadObject(file, makeUnique);
            }
            Freedomotic.logger.info("Loaded " + objectList.size() + " of " + files.length + " environment objects.");
        } catch (Exception e) {
            Freedomotic.logger.severe("Exception while loading this object.\n" + Freedomotic.getStackTraceInfo(e));
        }
    }

    public static EnvObjectLogic loadObject(File file, boolean makeUnique) throws IOException {
        XStream xstream = FreedomXStream.getXstream();
        Freedomotic.logger.info("---- Loading object file named " + file.getName() + " from folder '" + file.getAbsolutePath() + "' ----");
        //validate the object against a predefined DTD
        String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/object.dtd");
        EnvObject pojo = (EnvObject) xstream.fromXML(xml);
        EnvObjectLogic objectLogic = EnvObjectFactory.create(pojo);
        if (makeUnique) {
            objectLogic.getPojo().setName(objectLogic.getPojo().getName() + "-" + UidGenerator.getNextStringUid());
            objectLogic.getPojo().setProtocol("unknown");
            objectLogic.getPojo().setPhisicalAddress("unknown");
        }
        try {
            Freedomotic.logger.info("Created a new logic for " + objectLogic.getPojo().getName() + " of type " + objectLogic.getClass().getCanonicalName().toString());
            add(objectLogic);
        } catch (NotValidElementException notValidElementException) {
            Freedomotic.logger.warning("Null object or uncomplete parameters in its definition.");
        } catch (AlreadyExistentException alreadyExistentException) {
            Freedomotic.logger.warning("Object " + objectLogic.getPojo().getName() + " is already in the list. It is skipped.");
        }
        return objectLogic;
    }

    public static Iterator iterator() {
        return objectList.values().iterator();
    }

    /**
     * Gets the object by name
     *
     * @param name
     * @return
     */
    public static EnvObjectLogic getObject(String name) {
        for (Iterator it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic object = (EnvObjectLogic) it.next();
            if (object.getPojo().getName().equalsIgnoreCase(name)) {
                return object;
            }
        }
        return null;
    }

    /**
     * Gets the object by its address and protocol
     *
     * @param protocol
     * @param address
     * @return
     */
    public static ArrayList<EnvObjectLogic> getObject(String protocol, String address) {
        if (protocol == null
                || address == null
                || protocol.trim().equalsIgnoreCase("unknown")
                || address.trim().equalsIgnoreCase("unknown")
                || protocol.isEmpty()
                || address.isEmpty()) {
            throw new IllegalArgumentException();
        }
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic object = (EnvObjectLogic) it.next();
            if ((object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim()))
                    && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(address.trim()))) {
                //Freedomotic.logger.info("Found object " + object.getPojo().getName() + " {protocol = " + object.getPojo().getProtocol() + "; address = " + object.getPojo().getPhisicalAddress() + "}");
                list.add(object);
            }
        }
        if (list.isEmpty()) {
            Freedomotic.logger.warning("Don't exist an object with protocol '" + protocol + "' and address '" + address + "'");
        }
        return list;
    }

    /**
     * Gets the object by its protocol
     *
     * @param protocol
     * @return
     */
    public static ArrayList<EnvObjectLogic> getObjectByProtocol(String protocol) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic object = (EnvObjectLogic) it.next();
            if ((object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim()))) {
                list.add(object);
            }
        }
        return list;
    }

    public static int size() {
        return objectList.size();
    }

    public static void add(EnvObjectLogic obj) throws NotValidElementException, AlreadyExistentException {
        if (obj == null
                || obj.getPojo() == null
                || obj.getPojo().getName() == null
                || obj.getPojo().getName().isEmpty()) {
            throw new NotValidElementException();
        }
        if (!objectList.containsValue(obj)) {
            objectList.put(obj.getPojo().getName(), obj);
        } else {
            throw new AlreadyExistentException();
        }
        obj.setChanged(true);
    }

    public static void remove(EnvObjectLogic input) {
        objectList.remove(input.getPojo().getName());
        input.setChanged(true); //force repainting on frontends clients
        input.destroy(); //free memory
    }

    public static void clear() {
        try {
            objectList.clear();
        } catch (Exception e) {
        }
    }
}
