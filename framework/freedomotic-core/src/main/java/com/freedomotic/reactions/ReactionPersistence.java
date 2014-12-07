/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
import com.freedomotic.persistence.Repository;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.util.Info;
import com.thoughtworks.xstream.XStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class ReactionPersistence implements Repository<Reaction> {

    private static final List<Reaction> list = new CopyOnWriteArrayList<Reaction>(); //for persistence purposes. ELEMENTS CANNOT BE MODIFIED OUTSIDE THIS CLASS

    public ReactionPersistence() {
    }

    /**
     *
     * @param folder
     */
    public static void saveReactions(File folder) {
        if (list.isEmpty()) {
            LOG.log(Level.WARNING, "There are no reactions to persist, {0} will not be altered.", folder.getAbsolutePath());

            return;
        }

        if (!folder.isDirectory()) {
            LOG.log(Level.WARNING, "{0} is not a valid reaction folder. Skipped", folder.getAbsoluteFile());

            return;
        }

        XStream xstream = FreedomXStream.getXstream();
        deleteReactionFiles(folder);

        try {
            LOG.log(Level.INFO, "Saving reactions to file in {0}", folder.getAbsolutePath());

            for (Reaction reaction : list) {
                String uuid = reaction.getUuid();

                if ((uuid == null) || uuid.isEmpty()) {
                    reaction.setUuid(UUID.randomUUID().toString());
                }

                String fileName = reaction.getUuid() + ".xrea";
                File file= new File(folder + "/" + fileName);
                FreedomXStream.toXML(reaction, file);
            }
        } catch (Exception e) {
            LOG.info(e.getLocalizedMessage());
            LOG.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    private static void deleteReactionFiles(File folder) {
        File[] files = folder.listFiles();

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

        files = folder.listFiles(objectFileFileter);

        for (File file : files) {
            file.delete();
        }
    }

    /**
     *
     * @param folder
     */
    public synchronized static void loadReactions(File folder) {
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
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #Reaction \t\t\t #Description").append("\n");

            if (files != null) {
                for (File file : files) {
                    Reaction reaction = null;
                    //validate the object against a predefined DTD
                    try {
                        String xml
                                = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/reaction.dtd");

                        reaction = (Reaction) xstream.fromXML(xml);
                    } catch (Exception e) {
                        LOG.log(Level.SEVERE, "Reaction file {0} is not well formatted: {1}", new Object[]{file.getName(), e.getLocalizedMessage()});
                        continue;
                    }

                    if (reaction.getTrigger() != null && reaction.getTrigger().getName() != null) {
                        add(reaction);
                    } else {
                        LOG.log(Level.SEVERE, "Cannot add reaction {0}: it has empty Trigger", file.getName());
                        continue;
                    }

                    if (reaction.getCommands().isEmpty()) {
                        LOG.log(Level.WARNING, "Reaction {0} has no valid commands. Maybe related objects are missing or not configured properly.", reaction.toString());
                    }

                    summary.append(reaction.getUuid()).append("\t\t\t").append(reaction.toString())
                            .append("\t\t\t").append(reaction.getDescription()).append("\n");
                }

                //writing a summary .txt file with the list of commands in this folder
                FileWriter fstream = new FileWriter(folder + "/index.txt");
                BufferedWriter indexfile = new BufferedWriter(fstream);
                indexfile.write(summary.toString());
                //Close the output stream
                indexfile.close();
            } else {
                LOG.log(Level.CONFIG, "No reactions to load from this folder {0}", folder.toString());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Exception while loading reaction in {0}.\n{1}", new Object[]{folder.getAbsolutePath(), Freedomotic.getStackTraceInfo(e)});
        }
    }

    /**
     *
     * @param r
     */
    @Deprecated
    public static void add(Reaction r) {
        if (!exists(r)) { //if not already loaded
            //if it's a new reaction validate it's commands
            if (r.getCommands() != null && !r.getCommands().isEmpty()) {
                try {
                    r.getTrigger().register(); //trigger starts to listen on its channel
                } catch (Exception e) {
                    LOG.warning("Cannot register trigger");
                }
                list.add(r);
                r.setChanged();
                LOG.log(Level.CONFIG, "Added new reaction {0}", r.getDescription());
            }
        } else {
            LOG.log(Level.INFO, "The reaction ''{0}'' is already loaded so it is skipped.", r.getDescription());
        }
    }

    /**
     *
     * @param input
     */
    @Deprecated
    public static void remove(Reaction input) {
        if (input != null) {
            boolean removed = list.remove(input);
            LOG.log(Level.INFO, "Removed reaction {0}", input.getDescription());
            try {
                input.getTrigger().unregister();
            } catch (Exception e) {
                LOG.warning("Cannot unregister trigger");
            }

            if ((!removed) && (list.contains(input))) {
                LOG.log(Level.WARNING, "Error while removing Reaction {0} from the list", input.getDescription());
            }
        }
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static Iterator<Reaction> iterator() {
        return list.iterator();
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static List<Reaction> getReactions() {
        return Collections.unmodifiableList(list);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Deprecated
    public static Reaction getReaction(String uuid) {
        for (Reaction r : list) {
            if (r.getUuid().equalsIgnoreCase(uuid)) {
                return r;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    public static int size() {
        return list.size();
    }

    /**
     *
     * @param input
     * @return
     */
    public static boolean exists(Reaction input) {
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
    private static final Logger LOG = Logger.getLogger(ReactionPersistence.class.getName());

    @Override
    public List<Reaction> findAll() {
        return Collections.unmodifiableList(list);
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
            LOG.severe("Cannot modify reaction");
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
            LOG.severe("Cannot copy reaction");
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

}
