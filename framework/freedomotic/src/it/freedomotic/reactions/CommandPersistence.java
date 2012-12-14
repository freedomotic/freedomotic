package it.freedomotic.reactions;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class CommandPersistence {

    private static Map<String, Command> userCommands = new HashMap<String, Command>();
    private static Map<String, Command> hardwareCommands = new HashMap<String, Command>();

    public static void add(Command c) {
        if (c != null) {
            if (!userCommands.containsKey(c.getName().trim().toLowerCase())) {
                userCommands.put(c.getName(), c);
                Freedomotic.logger.fine("Added command '" + c.getName() + "' to the list of user commands");
            } else {
                Freedomotic.logger.config("Command '" + c.getName() + "' already in the list of user commands. Skipped");
            }
        } else {
            Freedomotic.logger.warning("Attempt to add a null user command to the list. Skipped");
        }
    }

    public static void remove(Command input) {
        userCommands.remove(input.getName());
    }

    public static int size() {
        return userCommands.size();
    }

    public static Iterator iterator() {
        return userCommands.values().iterator();
    }

    public static Command getCommand(String name) {
        Command command = userCommands.get(name);
        if (command != null) {
            return command;
        }
        Command hwCommand = getHardwareCommand(name);
        return hwCommand;
    }

    public static Collection<Command> getHardwareCommands() {
        return hardwareCommands.values();
    }

    public static Collection<Command> getUserCommands() {
        return userCommands.values();
    }

    public static Command getHardwareCommand(String name) {
        Command command = hardwareCommands.get(name);
        if (command == null) {
            Freedomotic.logger.severe("Missing command '" + name + "'. "
                    + "Maybe the related plugin is not installed or cannot be loaded");
        }
        return command;
    }

    public static void loadCommands(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xcmd")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        files = folder.listFiles(objectFileFileter);
        if (files != null) {
            FileWriter fstream = null;
            try {
                StringBuilder summary = new StringBuilder();
                //print an header for the index.txt file
                summary.append("#Filename \t\t #CommandName \t\t\t #Destination").append("\n");
                for (File file : files) {
                    String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/command.dtd");
                    Command command = null;
                    try {
                        command = (Command) xstream.fromXML(xml);
                        if (command.isHardwareLevel()) { //an hardware level command
                            hardwareCommands.put(command.getName(), command);
                        } else { //a user level commmand
                            if (folder.getAbsolutePath().startsWith(Info.getPluginsPath())) {
                                command.setEditable(false);
                            }
                            add(command);
                        }
                        summary.append(file.getName()).append("\t\t").append(command.getName()).append("\t\t\t").append(command.getReceiver()).append("\n");
                    } catch (CannotResolveClassException e) {
                        Freedomotic.logger.severe("Cannot unserialize command due to unrecognized class '" 
                                + e.getMessage() + "' in \n" + xml);
                    }
                }
                fstream = new FileWriter(folder + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } catch (IOException ex) {
                Logger.getLogger(CommandPersistence.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    fstream.close();
                } catch (IOException ex) {
                    Logger.getLogger(CommandPersistence.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            Freedomotic.logger.config("No commands to load from this folder " + folder.toString());
        }
    }

    public static void saveCommands(File folder) {
        try {
            if (userCommands.isEmpty()) {
                Freedomotic.logger.warning("There are no commands to persist, " + folder.getAbsolutePath() + " will not be altered.");
                return;
            }
            if (!folder.isDirectory()) {
                Freedomotic.logger.warning(folder.getAbsoluteFile() + " is not a valid command folder. Skipped");
                return;
            }
            XStream xstream = FreedomXStream.getXstream();
            deleteCommandFiles(folder);

            for (Command c : userCommands.values()) {
                if (c.isEditable()) {
                    String uuid = c.getUUID();
                    if (uuid == null || uuid.isEmpty()) {
                        c.setUUID(UUID.randomUUID().toString());
                    }
                    String fileName = c.getUUID() + ".xcmd";
                    FileWriter fstream = new FileWriter(folder + "/" + fileName);
                    BufferedWriter out = new BufferedWriter(fstream);
                    out.write(xstream.toXML(c));
                    //Close the output stream
                    out.close();
                }
            }
        } catch (Exception e) {
            Freedomotic.logger.info(e.getLocalizedMessage());
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteCommandFiles(File folder) {
        File[] files = folder.listFiles();
        // This filter only returns object files
        FileFilter objectFileFileter = new FileFilter() {
            public boolean accept(File file) {
                if (file.isFile() && file.getName().endsWith(".xcmd")) {
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

    private CommandPersistence() {
    }
}
