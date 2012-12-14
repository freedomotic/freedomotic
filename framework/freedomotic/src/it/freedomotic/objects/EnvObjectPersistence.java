package it.freedomotic.objects;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.Representation;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import it.freedomotic.util.SerialClone;
import it.freedomotic.util.UidGenerator;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Enrico
 */
public class EnvObjectPersistence {

    public static final boolean MAKE_UNIQUE = true;
    public static final boolean MAKE_NOT_UNIQUE = false;
    private static Map<String, EnvObjectLogic> objectList = new HashMap<String, EnvObjectLogic>();

    private EnvObjectPersistence() {
        //disable instance creation
    }

    public static Collection<EnvObjectLogic> getObjectList() {
        return objectList.values();
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
        for (File file : files) {
            file.delete();
        }
    }

    /**
     * Loads all objects file filesystem folder and adds the objects to the list
     *
     * @param folder
     * @param makeUnique
     */
    public synchronized static void loadObjects(File folder, boolean makeUnique) {
        objectList.clear();
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
                EnvObjectLogic loaded = loadObject(file);
                add(loaded, makeUnique);
            }
            //Freedomotic.logger.info("Loaded " + objectList.size() + " of " + files.length + " environment objects.");
        } catch (Exception e) {
            Freedomotic.logger.severe("Exception while loading this object.\n" + Freedomotic.getStackTraceInfo(e));
        }
    }

    /**
     * Loads the object file from file but NOT add the object to the list
     *
     * @param folder
     */
    public static EnvObjectLogic loadObject(File file) throws IOException {
        XStream xstream = FreedomXStream.getXstream();
        //validate the object against a predefined DTD
        String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/object.dtd");
        EnvObject pojo = null;
        try {
            pojo = (EnvObject) xstream.fromXML(xml);
        } catch (Exception e) {
            Freedomotic.logger.severe("XML parsing error. Readed XML is \n" + xml);
            
        }
        EnvObjectLogic objectLogic = EnvObjectFactory.create(pojo);
        Freedomotic.logger.config("Created a new logic for " + objectLogic.getPojo().getName() + " of type " + objectLogic.getClass().getCanonicalName().toString());
        //add(objectLogic);
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

    /**
     * Add an object to the environment. You can use EnvObjectPersistnce.MAKE_UNIQUE to create
     * an object that will surely be unique. Beware this means it is created with defensive copy
     * of the object in input and name, protocol, address and UUID are reset to a default value.
     * @param obj the environment object to add
     * @param MAKE_UNIQUE can be true or false. Creates a defensive copy reference to the object in input.
     * @return A pointer to the newly created environment object
     */
    public static EnvObjectLogic add(final EnvObjectLogic obj, final boolean MAKE_UNIQUE) {
        if (obj == null
                || obj.getPojo() == null
                || obj.getPojo().getName() == null
                || obj.getPojo().getName().isEmpty()) {
            throw new IllegalArgumentException("This is not a valid object");
        }
        EnvObjectLogic envObjectLogic = obj;
        if (MAKE_UNIQUE) {
            //defensive copy to not affect the passed object with the changes
            EnvObject pojoCopy = SerialClone.clone(obj.getPojo());
            pojoCopy.setName(obj.getPojo().getName() + "-" + UidGenerator.getNextStringUid());
            pojoCopy.setProtocol("unknown");
            pojoCopy.setPhisicalAddress("unknown");
            envObjectLogic = EnvObjectFactory.create(pojoCopy);
            envObjectLogic.getPojo().setUUID("");
        }
        envObjectLogic.init();
        if (!objectList.containsValue(envObjectLogic)) {
            objectList.put(envObjectLogic.getPojo().getName(), envObjectLogic);
            envObjectLogic.setChanged(true);
        } else {
            throw new RuntimeException("Cannot add the same object more than one time");
        }
        return envObjectLogic;
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
