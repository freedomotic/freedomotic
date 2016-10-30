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

import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.DataUpgradeException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.persistence.DataUpgradeService;
import com.freedomotic.persistence.Repository;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.settings.Info;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import static com.freedomotic.util.FileOperations.writeSummaryFile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class ReactionRepositoryImpl implements ReactionRepository {

    private static final Logger LOG = LoggerFactory.getLogger(ReactionRepositoryImpl.class.getName());

    private static final List<Reaction> list = new ArrayList<Reaction>(); //for persistence purposes. ELEMENTS CANNOT BE MODIFIED OUTSIDE THIS CLASS
    private final DataUpgradeService dataUpgradeService;

    @Inject
    public ReactionRepositoryImpl(DataUpgradeService dataUpgradeService) {
        this.dataUpgradeService = dataUpgradeService;
    }

    /**
     * Saves all .xrea reaction files to a given folder.
     *
     * @param folder the folder where to save the files
     */
    public void saveReactions(File folder) {
        if (list.isEmpty()) {
            LOG.warn("There are no reactions to persist, {} will not be altered.", folder.getAbsolutePath());
            return;
        }

        if (!folder.isDirectory()) {
            LOG.warn("{} is not a valid reaction folder. Skipped", folder.getAbsoluteFile());
            return;
        }

        deleteReactionFiles(folder);

        try {
            LOG.info("Saving reactions to file in {}", folder.getAbsolutePath());
            StringBuffer summaryContent = new StringBuffer();
            for (Reaction reaction : list) {
                String uuid = reaction.getUuid();
                if ((uuid == null) || uuid.isEmpty()) {
                    reaction.setUuid(UUID.randomUUID().toString());
                }
                String fileName = reaction.getUuid() + ".xrea";
                File file = new File(folder + "/" + fileName);
                FreedomXStream.toXML(reaction, file);
				summaryContent.append(reaction.getUuid()).append("\t\t\t").append(reaction.toString()).append("\t\t\t")
						.append(reaction.getDescription()).append("\n");
            }
            
            writeSummaryFile(new File(folder, "index.txt"), "#Filename \t\t #Reaction \t\t\t #Description\n", summaryContent.toString());
            
        } catch (Exception e) {
            LOG.error("Error while saving reations", e);
        }
    }

    /**
     * Deletes all .xrea reaction files from a given folder.
     *
     * @param folder the folder to save the files from
     */
    private void deleteReactionFiles(File folder) {
        File[] files;

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() && file.getName().endsWith(".xrea");
                    }
                };

        files = folder.listFiles(objectFileFileter);

        for (File file : files) {
            file.delete();
        }
    }

    /**
     * Loads all .xrea reaction files from a given folder.
     *
     * @param folder the folder to load reaction files from
     */
    public synchronized void loadReactions(File folder) {
        XStream xstream = FreedomXStream.getXstream();

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
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
            if (files != null) {
                for (File file : files) {
                    Reaction reaction = null;
                    String xml;
                    //validate the object against a predefined DTD
                    try {
                        xml = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/reaction.dtd");
                    } catch (IOException ex) {
                        throw new RepositoryException(ex.getMessage(), ex);
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
                        xml = (String) dataUpgradeService.upgrade(Reaction.class, xml, fromVersion);
                        reaction = (Reaction) xstream.fromXML(xml);

                    } catch (DataUpgradeException dataUpgradeException) {
                        throw new RepositoryException("Cannot upgrade Reaction file " + file.getAbsolutePath(), dataUpgradeException);
                    } catch (XStreamException e) {
                        throw new RepositoryException("XML parsing error. Readed XML is \n" + xml, e);
                    }

                    if (reaction.getTrigger() != null && reaction.getTrigger().getName() != null) {
                        add(reaction);
                    } else {
                        LOG.error("Cannot add reaction {}: it has empty Trigger", file.getName());
                        continue;
                    }

                    if (reaction.getCommands().isEmpty()) {
                        LOG.warn("Reaction {} has no valid commands. Maybe related objects are missing or not configured properly.", reaction.toString());
                    }
                }
            } else {
                LOG.debug("No reactions to load from this folder {}", folder.toString());
            }
        } catch (Exception e) {
            LOG.error("Exception while loading reaction in {}", new Object[]{folder.getAbsolutePath()}, e);
        }
    }

    /**
     *
     * @param r
     */
    @Deprecated
    public void add(Reaction r) {
        if (!exists(r)) { //if not already loaded
            //if it's a new reaction validate it's commands
            if (r.getCommands() != null && !r.getCommands().isEmpty()) {
                try {
                    r.getTrigger().register(); //trigger starts to listen on its channel
                } catch (Exception e) {
                    LOG.warn("Cannot register trigger");
                }
                list.add(r);
                r.setChanged();
                LOG.debug("Added new reaction {}", r.getDescription());
            }
        } else {
            // Exists but has no commands
            if (r.getCommands().isEmpty()) {
                LOG.info("The reaction ''{}'' has no associated commands and will be unloaded.", r.getDescription());
                remove(r);
            }
            LOG.info("The reaction ''{}'' is already loaded so it is skipped.", r.getDescription());
        }
    }

    /**
     *
     * @param input
     */
    @Deprecated
    public void remove(Reaction input) {
        if (input != null) {
            boolean removed = list.remove(input);
            LOG.info("Removed reaction {}", input.getDescription());
            try {
                input.getTrigger().unregister();
            } catch (Exception e) {
                LOG.warn("Cannot unregister trigger");
            }

            if ((!removed) && (list.contains(input))) {
                LOG.warn("Error while removing Reaction {} from the list", input.getDescription());
            }
        }
    }

    /**
     *
     * @return
     */
    @Deprecated
    public Iterator<Reaction> iterator() {
        return list.iterator();
    }

    /**
     *
     * @return
     */
    @Deprecated
    public List<Reaction> getReactions() {
        return Collections.unmodifiableList(list);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Deprecated
    public Reaction getReaction(String uuid) {
        for (Reaction r : list) {
            if (r.getUuid().equalsIgnoreCase(uuid)) {
                return r;
            }
        }
        return null;
    }

    /**
     * Returns the number of Reactions. 
     * 
     * @return the number of reactions 
     */
    public int size() {
        return list.size();
    }

    /**
     * Checks if a reaction exists.
     * 
     * @param input the reaction to check
     * @return true if the reaction exists, false otherwise
     */
    public boolean exists(Reaction input) {
        if (input != null) {
            for (Iterator<Reaction> it = list.iterator(); it.hasNext();) {
                Reaction reaction = it.next();
                if (input.equals(reaction)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public List<Reaction> findAll() {
        //return Collections.unmodifiableList(list);
        Collections.sort(list, new ReactionNameComparator());
        return (list);
    }

    @Override
    public List<Reaction> findByName(String name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

    }

    @Override
    public Reaction findOne(String uuid) {
        for (Reaction r : list) {
            if (r.getUuid().equalsIgnoreCase(uuid)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public boolean create(Reaction item) {
        try {
            add(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean delete(Reaction item) {
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
    public Reaction modify(String uuid, Reaction data) {
        try {
            delete(uuid);
            data.setUuid(uuid);
            create(data);
            return data;
        } catch (Exception e) {
            LOG.error("Cannot modify reaction", e);
            return null;
        }
    }

    @Override
    public Reaction copy(Reaction rea) {
        try {
            Reaction r = findOne(rea.getUuid());
            Reaction newOne = (Reaction) r.clone();
            create(newOne);
            return newOne;
        } catch (Exception e) {
            LOG.error("Cannot copy reaction", e);
            return null;
        }
    }

    @Override
    public void deleteAll() {
        try {
            for (Reaction r : findAll()) {
                delete(r);
            }
        } catch (Exception e) {
        } finally {
            list.clear();
        }
    }

    class ReactionNameComparator implements Comparator<Reaction> {

        @Override
        public int compare(Reaction r1, Reaction r2) {
            return r1.getTrigger().getName().compareTo(r2.getTrigger().getName());
        }
    }

}
