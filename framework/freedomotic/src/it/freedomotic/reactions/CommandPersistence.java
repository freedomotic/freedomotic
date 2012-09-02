package it.freedomotic.reactions;

import com.thoughtworks.xstream.XStream;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.*;

/**
 *
 * @author Enrico
 */
public class CommandPersistence {

    private static Map<String, Command> userCommands = new TreeMap<String, Command>();
    private static Map<String, Command> hardwareCommands = new TreeMap<String, Command>();

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
            Freedomotic.logger.severe("The system is searching for an hardware command named '" + name + "' but it doesen't exists. Check the spelling!");
        }
        return command;
    }

    public static void loadCommands(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        Freedomotic.logger.info("---- Initialization of Commands ----");
        Freedomotic.logger.info("Loading commands from: " + folder.getAbsolutePath());
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
            try {
                StringBuilder summary = new StringBuilder();
                //print an header for the index.txt file
                summary.append("#Filename \t\t #CommandName \t\t\t #Destination").append("\n");
                for (File file : files) {
                    String xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/command.dtd");
                    Command command = (Command) xstream.fromXML(xml);
                    if (command.isHardwareLevel()) { //an hardware level command
                        hardwareCommands.put(command.getName(), command);
                        Freedomotic.logger.info("Loaded hardware command '" + command.getName() + "'");
                    } else { //a user level commmand
                        if (folder.getAbsolutePath().startsWith(Info.getPluginsPath())) {
                            command.setEditable(false);
                        }
                        add(command);
                        Freedomotic.logger.info("Loaded user command '" + command.getName() + "'");
                    }
                    summary.append(file.getName()).append("\t\t").append(command.getName()).append("\t\t\t").append(command.getReceiver()).append("\n");
                    //writing a summary .txt file with the list of commands in this folder
                    FileWriter fstream = new FileWriter(folder + "/index.txt");
                    BufferedWriter indexfile = new BufferedWriter(fstream);
                    indexfile.write(summary.toString());
                    //Close the output stream
                    indexfile.close();
                }
            } catch (Exception e) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
            }
        } else {
            Freedomotic.logger.info("No commands to load from this folder");
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
