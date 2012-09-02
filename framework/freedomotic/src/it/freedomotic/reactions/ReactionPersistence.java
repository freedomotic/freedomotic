package it.freedomotic.reactions;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author Enrico
 */
public class ReactionPersistence {

    private static List<Reaction> list = new ArrayList<Reaction>();        //for persistence purposes. ELEMENTS CANNOT BE MODIFIED OUTSIDE THIS CLASS

    
    private ReactionPersistence(){
        //avoid instance creation
    }

    public static void saveReactions(File folder) {
        if (list.isEmpty()) {
            Freedomotic.logger.warning("There are no reactions to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }
        if (!folder.isDirectory()) {
            Freedomotic.logger.warning(folder.getAbsoluteFile() + " is not a valid reaction folder. Skipped");
            return;
        }
        XStream xstream = FreedomXStream.getXstream();
        deleteReactionFiles(folder);
        try {
            Freedomotic.logger.info("---- Saving reactions to file in " + folder.getAbsolutePath() + " ----");
            for (Reaction reaction : list) {
                String uuid = reaction.getUUID();
                if (uuid == null || uuid.isEmpty()) {
                    reaction.setUUID(UUID.randomUUID().toString());
                }
                String fileName = reaction.getUUID() + ".xrea";
                FileWriter fstream = new FileWriter(folder + "/" + fileName);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xstream.toXML(reaction)); //persist only the data not the logic
                //Close the output stream
                out.close();
                fstream.close();
            }
        } catch (Exception e) {
            Freedomotic.logger.info(e.getLocalizedMessage());
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteReactionFiles(File folder) {
        File[] files = folder.listFiles();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xrea")) {
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

    public synchronized static void loadReactions(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        Freedomotic.logger.info("-- Initialization of Reactions --");
        Freedomotic.logger.info("Loading reactions from: " + folder.getAbsolutePath());

        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {

            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xrea")) {
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
            summary.append("#Filename \t\t #Reaction \t\t\t #Description").append("\n");
            if (files != null) {
                for (File file : files) {
                    Freedomotic.logger.info("---- Loading reaction file named " + file.getName() + " from folder '" + folder.getAbsolutePath() + "' ----");
                    //validate the object against a predefined DTD
                    String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/reaction.dtd");
                    Reaction reaction = (Reaction) xstream.fromXML(xml);
                    if (reaction.getCommands().size() == 0) {
                        Freedomotic.logger.severe("Reaction " + reaction.toString() + " has no valid commands. Maybe related objects are missing or not configured properly.");
                    }
                    add(reaction);
                    summary.append(reaction.getUUID()).append("\t\t\t").append(reaction.toString()).append("\t\t\t").append(reaction.getDescription()).append("\n");
                }
                //writing a summary .txt file with the list of commands in this folder
                FileWriter fstream = new FileWriter(folder + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } else {
                Freedomotic.logger.info("No reactions to load from this folder");
            }
        } catch (Exception e) {
            Freedomotic.logger.severe("Exception while loading this object.\n" + Freedomotic.getStackTraceInfo(e));
        }
    }

    public static void add(Reaction r) {
        if ((r.getTrigger() == null) || ( r.getCommands()==null) || r.getCommands().isEmpty()) {
            Freedomotic.logger.warning("This reaction has no trigger or commands. It is skipped.");
            return;
        }
        if (!exists(r)) { //if not already loaded
            r.getTrigger().register(); //trigger starts to listen on its channel
            list.add(r);
            r.setChanged();
            Freedomotic.logger.finer("Added new reaction " + r.getDescription());
            for (Command command : r.getCommands()) {
                if (command == null) {
                    Freedomotic.logger.warning("The reaction '" + r.getDescription() + "' has broken link to a commands. Maybe this command doesn't exists or its name is misspelled.");
                }
            }
        } else {
            Freedomotic.logger.warning("The reaction '" + r.getDescription() + "' is already loaded so its skipped.");
        }
    }

    public static void remove(Reaction input) {
        if (input != null) {
            boolean removed = list.remove(input);
            Freedomotic.logger.finer("Removed reaction " + input.getDescription());
            input.getTrigger().unregister();
            if ((!removed) && (list.contains(input))) {
                Freedomotic.logger.info("Error while removing Reaction " + input.getDescription() + " from the list");
            }
        }
    }

    public static Iterator iterator() {
        return list.iterator();
    }

    public static List<Reaction> getReactions() {
        return Collections.unmodifiableList(list);
    }

    public static int size() {
        return list.size();
    }

    public static boolean exists(Reaction input) {
        if (input != null) {
            for (Iterator it = list.iterator(); it.hasNext();) {
                Reaction reaction = (Reaction) it.next();
                if (input.equals(reaction)) {
                    return true;
                }
            }
        }
        return false;
    }
}
