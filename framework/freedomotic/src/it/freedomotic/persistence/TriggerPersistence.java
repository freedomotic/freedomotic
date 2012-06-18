package it.freedomotic.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.persistence.FilePersistenceStrategy;
import com.thoughtworks.xstream.persistence.PersistenceStrategy;
import com.thoughtworks.xstream.persistence.XmlArrayList;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Trigger;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;

/**
 *
 * @author Enrico
 */
public final class TriggerPersistence {

    private static ArrayList<Trigger> list = new ArrayList<Trigger>();

    public static void saveTriggers(File folder) {
        if (list.isEmpty()) {
            Freedomotic.logger.warning("There are no triggers to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }
        if (!folder.isDirectory()) {
            Freedomotic.logger.warning(folder.getAbsoluteFile() + " is not a valid trigger folder. Skipped");
            return;
        }
        XStream xstream = FreedomXStream.getXstream();
        deleteTriggerFiles(folder);
        try {
            Freedomotic.logger.info("---- Saving triggers to file in " + folder.getAbsolutePath() + " ----");
            for (Trigger trigger : list) {
                if (trigger.isToPersist()) {
                    String uuid = trigger.getUUID();
                    if (uuid == null || uuid.isEmpty()) {
                        trigger.setUUID(UUID.randomUUID().toString());
                    }
                    String fileName = trigger.getUUID() + ".xtrg";
                    FileWriter fstream = new FileWriter(folder + "/" + fileName);
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(xstream.toXML(trigger)); //persist only the data not the logic
                    //Close the output stream
                    out.close();
                    fstream.close();
                }
            }
        } catch (Exception e) {
            Freedomotic.logger.info(e.getLocalizedMessage());
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteTriggerFiles(File folder) {
        File[] files = folder.listFiles();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xtrg")) {
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

    public static ArrayList<Trigger> getTriggers() {
        return list;
    }

    private TriggerPersistence() {
    }

    public synchronized static void loadTriggers(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        Freedomotic.logger.info("-- Initialization of Triggers --");
        Freedomotic.logger.info("Loading triggers from: " + folder.getAbsolutePath());

        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xtrg")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        File[] files = folder.listFiles(objectFileFileter);
        try {
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #TriggerName \t\t\t #ListenedChannel").append("\n");
            if (files != null) {
                for (File file : files) {
                    Freedomotic.logger.fine("Loading trigger file named " + file.getName() + " from folder '" + folder.getAbsolutePath());
                    //validate the object against a predefined DTD
                    String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/trigger.dtd");
                    Trigger trigger = (Trigger) xstream.fromXML(xml);
                    //addAndRegister trigger to the list if it is not a duplicate
                    if (!list.contains(trigger)) {
                        if (trigger.isHardwareLevel()) {
                            trigger.setPersistence(false); //it has not to me stored in root/data folder
                            addAndRegister(trigger); //in the list and start listening
                        } else {
                            if (folder.getAbsolutePath().startsWith(Info.getPluginsPath())) {
                                trigger.setPersistence(false);
                            } else {
                                trigger.setPersistence(true); //not hardware trigger and not plugin related
                            }
                            list.add(trigger); //only in the list not registred. I will be registred only if used in mapping
                        }
                    } else {
                        Freedomotic.logger.warning("Trigger '" + trigger.getName() + "' is already in the list");
                    }
                    summary.append(trigger.getUUID()).append("\t\t").append(trigger.getName()).append("\t\t\t").append(trigger.getChannel()).append("\n");
                }
                //writing a summary .txt file with the list of commands in this folder
                FileWriter fstream = new FileWriter(folder + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } else {
                Freedomotic.logger.info("No triggers to load from this folder");
            }
        } catch (Exception e) {
            Freedomotic.logger.severe("Exception while loading this trigger.\n" + Freedomotic.getStackTraceInfo(e));
        }
    }

    public static synchronized void addAndRegister(Trigger t) {
        int preSize = TriggerPersistence.size();
        if (!list.contains(t)) {
            list.add(t);
            t.register();
        } else {
            //this trigger is already in the list
            int old = list.indexOf(t);
            list.get(old).unregister();
            list.set(old, t);
            t.register();
        }
        int postSize = TriggerPersistence.size();
        if (!(postSize == preSize + 1)) {
            Freedomotic.logger.severe("Error while while adding and registering trigger '" + t.getName() + "'");
        }
    }

    public static synchronized void add(Trigger t) {
        int preSize = TriggerPersistence.size();
        if (!list.contains(t)) {
            list.add(t);
        } else {
            //this trigger is already in the list
            int old = list.indexOf(t);
            list.get(old).unregister();
            list.set(old, t);
        }
        int postSize = TriggerPersistence.size();
        if (!(postSize == preSize + 1)) {
            Freedomotic.logger.severe("Error while while adding trigger '" + t.getName() + "'");
        }
    }

    public static synchronized void remove(Trigger t) {
        int preSize = TriggerPersistence.size();
        try {
            t.unregister();
            list.remove(t);
        } catch (Exception e) {
            Freedomotic.logger.severe("Error while while unregistering the trigger '" + t.getName() + "'");
        }
        int postSize = TriggerPersistence.size();
        if (!(postSize == preSize - 1)) {
            Freedomotic.logger.severe("Error while while removing trigger '" + t.getName() + "'");
        }
    }

    /**
     * Get a trigger by its name
     *
     * @param name
     * @return a Trigger object with the name (ignore-case) as the String in
     * input
     */
    public static Trigger getTrigger(String name) {
        if ((name == null) || (name.isEmpty())) {
            return null;
        }
        for (Iterator it = list.iterator(); it.hasNext();) {
            Trigger trigger = (Trigger) it.next();
            if (trigger.getName().equalsIgnoreCase(name)) {
                return trigger;
            }
        }
        Freedomotic.logger.warning("Searching for a trigger named '" + name + "' but it doesen't exist.");
        return null;
    }

    public static Trigger getTrigger(Trigger input) {
        if (input != null) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                Trigger trigger = (Trigger) it.next();
                if (trigger.equals(input)) {
                    return trigger;
                }
            }
        }
        return null;
    }

    public static Trigger getTrigger(int i) {
        return list.get(i);
    }

    public static Iterator iterator() {
        return list.iterator();
    }

    public static int size() {
        return list.size();
    }
}
