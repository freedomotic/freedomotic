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
class TriggerRepositoryImpl implements TriggerRepository {

    private static final Logger LOG = LoggerFactory.getLogger(TriggerRepositoryImpl.class.getName());
    private static ArrayList<Trigger> list = new ArrayList<Trigger>();
    private final DataUpgradeService dataUpgradeService;

    @Inject
    public TriggerRepositoryImpl(DataUpgradeService dataUpgradeService) {
        this.dataUpgradeService = dataUpgradeService;
    }

    /**
     * Saves the triggers into the specified folder.
     *
     * @param folder the folder where to save the triggers
     */
    @Override
    public void saveTriggers(File folder) {
        if (list.isEmpty()) {
            LOG.warn("There are no triggers to persist, {} will not be altered", folder.getAbsolutePath());
            return;
        }

        if (!folder.isDirectory()) {
            LOG.warn("{} is not a valid trigger folder. Skipped", folder.getAbsoluteFile());
            return;
        }

        XStream xstream = FreedomXStream.getXstream();
        deleteTriggerFiles(folder);

        try {
            LOG.info("Saving triggers to file in {}", folder.getAbsolutePath());
            StringBuffer summaryContent = new StringBuffer();
            for (Trigger trigger : list) {
                if (trigger.isToPersist()) {
                    String uuid = trigger.getUUID();

                    if ((uuid == null) || uuid.isEmpty()) {
                        trigger.setUUID(UUID.randomUUID().toString());
                    }

                    String fileName = trigger.getUUID() + ".xtrg";
                    File file = new File(folder + "/" + fileName);
                    FreedomXStream.toXML(trigger, file);
                }
                
                summaryContent.append(trigger.getUUID()).append("\t\t").append(trigger.getName()).append("\t\t\t")
                .append(trigger.getChannel()).append("\n");
            }
            
            writeSummaryFile(new File(folder, "index.txt"), "#Filename \t\t #TriggerName \t\t\t #ListenedChannel\n", summaryContent.toString());
            
        } catch (Exception e) {
            LOG.error("Error while saving triggers ", e);
        }
    }

    /**
     * Deletes all the triggers into the specified folder.
     *
     * @param folder the folder containing all the triggers to delete
     */
    private static void deleteTriggerFiles(File folder) {
        File[] files = folder.listFiles();
        // this filter only returns triggers files
        FileFilter objectFileFilter
                = new FileFilter() {
                    public boolean accept(File file) {
                        if (file.isFile() && file.getName().endsWith(".xtrg")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };
        files = folder.listFiles(objectFileFilter);
        for (File file : files) {
            file.delete();
        }
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static ArrayList<Trigger> getTriggers() {
        return list;
    }

    /**
     * Loads triggers from a specified folder.
     *
     * @param folder the folder to load triggers from
     */
    @Override
    public void loadTriggers(File folder) {
        XStream xstream = FreedomXStream.getXstream();

        // this filter only returns triggers files
        FileFilter objectFileFileter
                = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() && file.getName().endsWith(".xtrg");
                    }
                };

        File[] files = folder.listFiles(objectFileFileter);

        try {
           

            if (files != null) {
                for (File file : files) {
                    Trigger trigger = null;
                    String xml;
                    try {
                        //validate the object against a predefined DTD
                        xml = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/trigger.dtd");
                    } catch (IOException e) {
                        continue;
                    }
                    try {
                        Properties dataProperties = new Properties();
                        String fromVersion;
                        try {
                            dataProperties.load(new FileInputStream(new File(Info.PATHS.PATH_DATA_FOLDER + "/data.properties")));
                            fromVersion = dataProperties.getProperty("data.version");
                        } catch (IOException iOException) {
                            // fallback to a default version for older version without that properties file
                            fromVersion = "5.5.0";
                        }
                        xml = (String) dataUpgradeService.upgrade(Trigger.class, xml, fromVersion);
                        trigger = (Trigger) xstream.fromXML(xml);

                    } catch (DataUpgradeException dataUpgradeException) {
                        throw new RepositoryException("Cannot upgrade Trigger file " + file.getAbsolutePath(), dataUpgradeException);
                    } catch (XStreamException e) {
                        throw new RepositoryException("XML parsing error. Readed XML is \n" + xml, e);
                    }
                    //addAndRegister trigger to the list if it is not a duplicate
                    if (!list.contains(trigger)) {
                        if (trigger.isHardwareLevel()) {
                            trigger.setPersistence(false); //it has not to me stored in root/data folder
                            addAndRegister(trigger); //in the list and start listening
                        } else {
                            if (folder.getAbsolutePath().startsWith(Info.PATHS.PATH_PLUGINS_FOLDER.getAbsolutePath())) {
                                trigger.setPersistence(false);
                            } else {
                                trigger.setPersistence(true); //not hardware trigger and not plugin related
                            }

                            list.add(trigger); //only in the list not registred. I will be registred only if used in mapping
                        }
                    } else {
                        LOG.warn("Trigger '{}' is already in the list", trigger.getName());
                    }
                }
            } else {
                LOG.info("No triggers to load from this folder {}", folder.toString());
            }
        } catch (Exception e) {
            LOG.error("Exception while loading this trigger ", e);
        }
    }

    /**
     * Adds and registers a trigger.
     *
     * @param t the trigger to add and register
     */
    public static synchronized void addAndRegister(Trigger t) {
        int preSize = TriggerRepositoryImpl.size();

        if (!list.contains(t)) {
            list.add(t);
            t.register();
            int postSize = TriggerRepositoryImpl.size();
            if (!(postSize == (preSize + 1))) {
                LOG.error("Error while adding and registering trigger '{}'", t.getName());
            }
        } else {
            //this trigger is already in the list
            int old = list.indexOf(t);
            list.get(old).unregister();
            list.set(old, t);
            t.register();
        }

    }

    /**
     * Adds a trigger.
     *
     * @param t the trigger to add
     */
    public static synchronized void add(Trigger t) {
        if (t != null) {
            int preSize = TriggerRepositoryImpl.size();

            if (!list.contains(t)) {
                list.add(t);
            } else {
                //this trigger is already in the list
                int old = list.indexOf(t);
                list.get(old).unregister();
                list.set(old, t);
            }

            int postSize = TriggerRepositoryImpl.size();

            if (!(postSize == (preSize + 1))) {
                LOG.error("Error while adding trigger '{}'", t.getName());
            }
        }
    }

    /**
     * Removes a trigger.
     *
     * @param t the trigger to remove
     */
    public static synchronized void remove(Trigger t) {
        int preSize = TriggerRepositoryImpl.size();

        try {
            t.unregister();
            list.remove(t);
            int postSize = TriggerRepositoryImpl.size();

            if (!(postSize == (preSize - 1))) {
                LOG.error("Error while removing trigger '{}'", t.getName());
            }
        } catch (Exception e) {
            LOG.error("Error while unregistering the trigger '{}'", t.getName(), e);
        }
    }

    /**
     * Gets a trigger by its name.
     *
     * @param name
     * @return a trigger with the name (ignore-case) as the String in input
     */
    @Deprecated
    public static Trigger getTrigger(String name) {
        if ((name == null) || (name.trim().isEmpty())) {
            return null;
        }

        for (Trigger trigger : list) {
            if (trigger.getName().equalsIgnoreCase(name.trim())) {
                return trigger;
            }
        }
        LOG.warn("Searching for a trigger named ''{}'' but it doesn''t exist", name);
        return null;
    }

    /**
     *
     * @param input
     * @return
     */
    @Deprecated
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

    /**
     *
     * @param i
     * @return
     */
    @Deprecated
    public static Trigger getTrigger(int i) {
        return list.get(i);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Deprecated
    public static Trigger getTriggerByUUID(String uuid) {
        for (Trigger t : list) {
            if (t.getUUID().equalsIgnoreCase(uuid)) {
                return t;
            }
        }
        return null;
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static Iterator<Trigger> iterator() {
        return list.iterator();
    }

    /**
     *
     * @return
     */
    public static int size() {
        return list.size();
    }

    @Override
    public List<Trigger> findAll() {
        Collections.sort(list, new TriggerNameComparator());
        //return getTriggers();
        return list;
    }

    /**
     * Finds a trigger given its name.
     *
     * @param name the name of the trigger to find
     */
    @Override
    public List<Trigger> findByName(String name) {
        List<Trigger> tl = new ArrayList<Trigger>();
        for (Trigger t : findAll()) {
            if (t.getName().equalsIgnoreCase(name)) {
                tl.add(t);
            }
        }
        return tl;
    }

    @Override
    public Trigger findOne(String uuid) {
        return getTriggerByUUID(uuid);
    }

    @Override
    public boolean create(Trigger item) {
        try {
            add(item);
            return true;
        } catch (Exception e) {
            LOG.error("Cannot add trigger {} " + item.getName(), e);
            return false;
        }
    }

    @Override
    public boolean delete(Trigger item) {
        try {
            remove(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Deletes a trigger given its uuid.
     *
     * @param uuid the uuid of the trigger to delete
     */
    @Override
    public boolean delete(String uuid) {
        return delete(findOne(uuid));
    }

    @Override
    public Trigger modify(String uuid, Trigger data) {
        try {
            if (uuid == null || uuid.isEmpty() || data == null) {
                LOG.warn("Cannot even start modifying trigger, basic data missing");
                return null;
            } else {
                delete(uuid);
                data.setUUID(uuid);
                create(data);
                try {
                    data.register();
                } catch (Exception f) {
                    LOG.warn("Cannot register trigger {} ", data.getName(), f);
                }
                return data;
            }
        } catch (Exception e) {
            LOG.error("Error while modifying trigger {} " + data.getName(), e);
            return null;
        }
    }

    /**
     * Creates a copy of a given trigger.
     *
     * @param trg the trigger to copy
     */
    @Override
    public Trigger copy(Trigger trg) {
        try {
            Trigger t = findOne(trg.getUUID()).clone();
            t.setName("Copy of " + t.getName());
            create(t);
            return t;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Deletes all triggers.
     *
     */
    @Override
    public void deleteAll() {
        try {
            for (Trigger t : findAll()) {
                delete(t);
            }
        } catch (Exception e) {
        } finally {
            list.clear();
        }
    }

    class TriggerNameComparator implements Comparator<Trigger> {

        @Override
        public int compare(Trigger t1, Trigger t2) {
            return t1.getName().compareTo(t2.getName());
        }
    }
}
