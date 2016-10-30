/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.reactions;

import com.freedomotic.exceptions.DataUpgradeException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.persistence.DataUpgradeService;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.settings.Info;
import com.freedomotic.util.FileOperations;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import com.thoughtworks.xstream.mapper.CannotResolveClassException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static com.freedomotic.util.FileOperations.writeSummaryFile;

/**
 *
 * @author Enrico Nicoletti
 */
class CommandRepositoryImpl implements CommandRepository {

    private static final Logger LOG = LoggerFactory.getLogger(CommandRepositoryImpl.class.getName());

    private static final Map<String, Command> userCommands = new HashMap<String, Command>();
    private static final Map<String, Command> hardwareCommands = new HashMap<String, Command>();
    private final DataUpgradeService dataUpgradeService;

    @Inject
    public CommandRepositoryImpl(DataUpgradeService dataUpgradeService) {
        this.dataUpgradeService = dataUpgradeService;
    }

    /**
     *
     * @param c
     */
    @Deprecated
    public static void add(Command c) {
        if (c != null) {
            if (!c.isHardwareLevel()) {
                if (!userCommands.containsKey(c.getName().trim().toLowerCase())) {
                    userCommands.put(c.getName(),
                            c);
                    LOG.trace("Added command ''{}'' to the list of user commands", c.getName());
                } else {
                    LOG.debug("Command ''{}'' already in the list of user commands. Skipped", c.getName());
                }
            } else if (!hardwareCommands.containsKey(c.getName().trim().toLowerCase())) {
                hardwareCommands.put(c.getName(),
                        c);
                LOG.trace("Added command ''{}'' to the list of hardware commands", c.getName());
            } else {
                LOG.debug("Command ''{}'' already in the list of hardware commands. Skipped", c.getName());
            }
        } else {
            LOG.warn("Attempt to add a null user command to the list. Skipped");
        }
    }

    /**
     *
     * @param input
     */
    @Deprecated
    public static void remove(Command input) {
        if (input.isHardwareLevel()) {
            hardwareCommands.remove(input.getName());
        } else {
            userCommands.remove(input.getName());
        }
    }

    /**
     *
     * @return
     */
    public static int size() {
        return userCommands.size();
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static Iterator<Command> iterator() {
        return userCommands.values().iterator();
    }

    /**
     *
     * @param name
     * @return
     */
    @Deprecated
    public static Command getCommand(String name) {
        Command command = userCommands.get(name.trim());

        if (command != null) {
            return command;
        }

        Command hwCommand = getHardwareCommand(name);

        return hwCommand;
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Deprecated
    public static Command getCommandByUUID(String uuid) {
        for (Command c : userCommands.values()) {
            if (c.getUuid().equalsIgnoreCase(uuid)) {
                return c;
            }
        }
        for (Command c : hardwareCommands.values()) {
            if (c.getUuid().equalsIgnoreCase(uuid)) {
                return c;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    @Override
    public List<Command> findHardwareCommands() {
        return new ArrayList(hardwareCommands.values());
    }

    /**
     *
     * @return
     */
    @Override
    public List<Command> findUserCommands() {
        return new ArrayList(userCommands.values());
    }

    /**
     *
     * @param name
     * @return
     */
    @Deprecated
    public static Command getHardwareCommand(String name) {
        Command command = hardwareCommands.get(name.trim());

        if (command == null) {
            LOG.error("Missing command ''{}" + "''. "
                    + "Maybe the related plugin is not installed or cannot be loaded", name);
        }

        return command;
    }

    /**
     *
     * @param folder
     */
    @Override
    public void loadCommands(File folder) {
        XStream xstream = FreedomXStream.getXstream();
        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
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
                
                for (File file : files) {
                    Command command = null;
                    String xml;
                    try {
                        xml = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/command.dtd");
                    } catch (IOException ex) {
                        continue;
                    }
                    try {
                        Properties dataProperties = new Properties();
                        String fromVersion;
                        try {
                            dataProperties.load(new FileInputStream(new File(Info.PATHS.PATH_DATA_FOLDER + "/data.properties")));
                            fromVersion = dataProperties.getProperty("data.version");
                        } catch (IOException iOException) {
                            // Fallback to a default version for older version without that properties file
                            fromVersion = "5.5.0";
                        }
                        xml = (String) dataUpgradeService.upgrade(Command.class, xml, fromVersion);
                        command = (Command) xstream.fromXML(xml);

                    } catch (DataUpgradeException dataUpgradeException) {
                        throw new RepositoryException("Cannot upgrade Command file " + file.getAbsolutePath(), dataUpgradeException);
                    } catch (XStreamException e) {
                        throw new RepositoryException("XML parsing error. Readed XML is \n" + xml, e);
                    }
                    try {

                        if (command.isHardwareLevel()) { //an hardware level command
                            hardwareCommands.put(command.getName(),
                                    command);
                        } else { //a user level commmand

                            if (folder.getAbsolutePath().startsWith(Info.PATHS.PATH_PLUGINS_FOLDER.getAbsolutePath())) {
                                command.setEditable(false);
                            }

                            add(command);
                        }

                    } catch (CannotResolveClassException e) {
                        LOG.error("Cannot unserialize command due to unrecognized class ''{}'' in \n{}", new Object[]{e.getMessage(), xml});
                    }
                }

            } catch (Exception ex) {
                LOG.error("Error while loading command", ex);
            }
        } else {
            LOG.debug("No commands to load from this folder {}", folder.toString());
        }
    }

    /**
     *
     * @param folder
     */
    @Override
    public void saveCommands(File folder) {

        if (userCommands.isEmpty()) {
            LOG.warn("There are no commands to persist, {} will not be altered.", folder.getAbsolutePath());
            return;
        }

        if (!folder.isDirectory()) {
            LOG.warn("{} is not a valid command folder. Skipped", folder.getAbsoluteFile());
            return;
        }

        XStream xstream = FreedomXStream.getXstream();
        deleteCommandFiles(folder);

        try {
            LOG.info("Saving commands to file in " + folder.getAbsolutePath());
            StringBuffer summaryContent = new StringBuffer();
            for (Command c : userCommands.values()) {
                if (c.isEditable()) {
                    String uuid = c.getUuid();

                    if ((uuid == null) || uuid.isEmpty()) {
                        c.setUUID(UUID.randomUUID().toString());
                    }

                    String fileName = c.getUuid() + ".xcmd";
                    File file = new File(folder + "/" + fileName);
                    FreedomXStream.toXML(c, file);
                    summaryContent.append(fileName).append("\t\t").append(c.getName())
                    .append("\t\t\t").append(c.getReceiver()).append("\n");
                }
            }
            
            writeSummaryFile(new File(folder, "index.txt"), "#Filename \t\t #CommandName \t\t\t #Destination\n", summaryContent.toString());
            
        } catch (Exception e) {
            LOG.error("Error while saving commands to " + folder.getAbsolutePath(), e);
        }
    }

    private static void deleteCommandFiles(File folder) {
        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
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

    @Override
    public List<Command> findAll() {
        List<Command> cl = new ArrayList<Command>(userCommands.values());
        cl.addAll(hardwareCommands.values());
        return cl;

    }

    @Override
    public List<Command> findByName(String name) {
        List<Command> cl = new ArrayList<Command>();
        for (Command c : findAll()) {
            if (c.getName().equalsIgnoreCase(name)) {
                cl.add(c);
            }
        }
        return cl;
    }

    @Override
    public Command findOne(String uuid) {
        return getCommandByUUID(uuid);
    }

    @Override
    public boolean create(Command item) {
        try {
            add(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(Command item) {
        try {
            remove(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(String uuid) {
        return delete(findOne(uuid));
    }

    @Override
    public Command modify(String uuid, Command data) {
        try {
            delete(uuid);
            data.setUUID(uuid);
            add(data);
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Command copy(Command command) {
        try {
            Command c = findOne(command.getUuid()).clone();
            c.setName("Copy of " + c.getName());
            add(c);
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deleteAll() {
        try {
            for (Command c : findAll()) {
                delete(c);
            }
        } catch (Exception e) {
        } finally {
            hardwareCommands.clear();
            userCommands.clear();
        }
    }

}
