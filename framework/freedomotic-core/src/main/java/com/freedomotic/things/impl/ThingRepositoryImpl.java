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
package com.freedomotic.things.impl;

import com.freedomotic.core.SynchAction;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.DataUpgradeException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.DataUpgradeService;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.settings.Info;
import com.freedomotic.util.SerialClone;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import static com.freedomotic.util.FileOperations.writeSummaryFile;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class ThingRepositoryImpl implements ThingRepository {

    public static final boolean MAKE_UNIQUE = true;

    public static final boolean MAKE_NOT_UNIQUE = false;
    private static final Map<String, EnvObjectLogic> objectList = new HashMap<>();
    private static final Logger LOG = LoggerFactory.getLogger(ThingRepositoryImpl.class.getName());
    // Dependencies
    private final ThingFactory thingsFactory;
    private final DataUpgradeService dataUpgradeService;

    /**
     *
     * @param thingsFactory
     * @param environmentRepository
     */
    @Inject
    public ThingRepositoryImpl(ThingFactory thingsFactory, DataUpgradeService dataUpgradeService) {
        this.thingsFactory = thingsFactory;
        this.dataUpgradeService = dataUpgradeService;
    }

    @Deprecated
    @RequiresPermissions("objects:read")
    private static Collection<EnvObjectLogic> getObjectList() {
        return objectList.values();
    }

    /**
     *
     * @param folder
     * @throws RepositoryException
     */
    @RequiresPermissions("objects:save")
    private static void saveObjects(File folder) throws RepositoryException {
        if (objectList.isEmpty()) {
            throw new RepositoryException("There are no object to persist, " + folder.getAbsolutePath()
                    + " will not be altered.");
        }

        if (!folder.isDirectory()) {
            //TODO: create folder tree instead of just complaining
            throw new RepositoryException(folder.getAbsoluteFile() + " is not a valid object folder. Skipped");
        }

        deleteObjectFiles(folder);
        
        StringBuffer summaryContent = new StringBuffer();

        for (EnvObjectLogic envObject : objectList.values()) {
            String uuid = envObject.getPojo().getUUID();

            if ((uuid == null) || uuid.isEmpty()) {
                envObject.getPojo().setUUID(UUID.randomUUID().toString());
            }

            String fileName = envObject.getPojo().getUUID() + ".xobj";
            File file = new File(folder + "/" + fileName);
            FreedomXStream.toXML(envObject.getPojo(), file);
            summaryContent.append(fileName).append("\t\t").append(envObject.getPojo().getName()).append("\n");
        }
        
        try {
			writeSummaryFile(new File(folder, "index.txt"), "#Filename \t\t #ThingName\n", summaryContent.toString());
		} catch (IOException e) {
			LOG.error("Something went wrong while creating the index file for things.", e);
		}
    }

    private static void deleteObjectFiles(File folder) throws RepositoryException {
        if ((folder == null) || !folder.isDirectory()) {
            throw new IllegalArgumentException("Unable to delete objects files in a null or not valid folder");
        }

        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
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
            boolean deleted = file.delete();

            if (!deleted) {
                throw new RepositoryException("Unable to delete file " + file.getAbsoluteFile());
            }
        }
    }

    /**
     *
     * @return
     */
    @Deprecated
    @RequiresPermissions("objects:read")
    private static Iterator<EnvObjectLogic> iterator() {
        return objectList.values().iterator();
    }

    /**
     * Gets the object by name
     *
     * @param name
     * @return
     */
    @RequiresPermissions("objects:read")
    private static EnvObjectLogic getObjectByName(String name) {
        for (Iterator<EnvObjectLogic> it = ThingRepositoryImpl.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if (object.getPojo().getName().equalsIgnoreCase(name) //&& auth.isPermitted("objects:read:" + object.getPojo().getUUID())
                    ) {
                return object;
            }
        }

        return null;
    }

    /**
     *
     * @param tags
     * @return
     */
    @RequiresPermissions("objects:read")
    private static ArrayList<EnvObjectLogic> getObjectByTags(String tags) {
        ArrayList<EnvObjectLogic> results = new ArrayList<EnvObjectLogic>();
        // split tags string
        String[] tagList = tags.split(",");

        // search every object for at least one tag 
        for (EnvObjectLogic obj : objectList.values()) {
            Set<String> tagSet = new HashSet<String>();

            for (String tag : tagList) {
                if (!tag.trim().isEmpty()) {
                    tagSet.add(tag.trim());
                }
            }
            int prevCount = tagSet.size();
            tagSet.removeAll(obj.getPojo().getTagsList());
            if (prevCount > tagSet.size()) {
                results.add(obj);
            }
        }
        return results;
    }

    /**
     * Gets the object by name
     *
     * @param uuid
     * @return
     */
    @Deprecated
    @RequiresPermissions("objects:read")
    private static EnvObjectLogic getObjectByUUID(String uuid) {
        for (Iterator<EnvObjectLogic> it = iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if (object.getPojo().getUUID().equalsIgnoreCase(uuid)) {
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
     * @return EnvObjectLogic if one is found, else null is returned
     */
    @RequiresPermissions("objects:read")
    private static EnvObjectLogic getObjectByAddress(String protocol, String address) {
        if ((protocol == null)
                || (address == null)
                || protocol.isEmpty()
                || address.isEmpty()) {
            throw new IllegalArgumentException();
        }

        for (Iterator<EnvObjectLogic> it = ThingRepositoryImpl.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if ((object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim()))
                    && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(address.trim())) //           && auth.isPermitted("objects:read:" + object.getPojo().getUUID())
                    ) {
                return object;
            }
        }

        LOG.warn("An object with protocol '" + protocol + "' and address '"
                + address + "' doesn't exist");

        return null;
    }

    /**
     * Gets the object by its protocol
     *
     * @param protocol
     * @return
     */
    @RequiresPermissions("objects:read")
    private static ArrayList<EnvObjectLogic> getObjectByProtocol(String protocol) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = ThingRepositoryImpl.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if (object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim()) // && auth.isPermitted("objects:read:" + object.getPojo().getUUID())
                    ) {
                list.add(object);
            }
        }

        return list;
    }

    /**
     * Gets the object by its environment
     *
     * @param uuid
     * @return
     */
    @RequiresPermissions("objects:read")
    private static ArrayList<EnvObjectLogic> getObjectByEnvironment(String uuid) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = ThingRepositoryImpl.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if (object.getPojo().getEnvironmentID().equalsIgnoreCase(uuid) //&& auth.isPermitted("objects:read:" + object.getPojo().getUUID().substring(0, 7))
                    ) {
                list.add(object);
            }
        }

        return list;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    private static int size() {
        return objectList.size();
    }

    /**
     *
     * @param input
     */
    @Deprecated
    @RequiresPermissions("objects:delete")
    private static void remove(EnvObjectLogic input) {
        objectList.remove(input.getPojo().getUUID());
        input.setChanged(true); //force repainting on frontends clients
        input.destroy(); //free memory
    }

    private static List<String> getObjectsNames() {
        List<String> list = new ArrayList<String>();
        for (EnvObjectLogic obj : objectList.values()) {
            list.add(obj.getPojo().getName());
        }
        return list;
    }

    /**
     * Add an object to the environment. You can use
     * EnvObjectPersistnce.MAKE_UNIQUE to saveAll an object that will surely be
     * unique. Beware this means it is created with defensive copy of the object
     * in input and name, protocol, address and UUID are reset to a default
     * value.
     *
     * @param obj the environment object to add
     * @param MAKE_UNIQUE can be true or false. Creates a defensive copy
     * reference to the object in input.
     * @return A pointer to the newly created environment object
     */
    @Deprecated
    @RequiresPermissions("objects:create")
    private EnvObjectLogic add(final EnvObjectLogic obj, final boolean MAKE_UNIQUE) {
        if ((obj == null)) {
            throw new IllegalArgumentException("Cannot add a null object to the environment");
        }

        if ((obj.getPojo() == null)) {
            throw new IllegalArgumentException("Cannot add an object with null pojo to the environment");
        }

        if ((obj.getPojo().getName() == null) || obj.getPojo().getName().isEmpty()) {
            throw new IllegalArgumentException("The data pojo has no name");
        }

        EnvObjectLogic envObjectLogic = obj;

        if (MAKE_UNIQUE) {
            //defensive copy to not affect the passed object with the changes
            EnvObject pojoCopy = SerialClone.clone(obj.getPojo());
            pojoCopy.setName(getNextInOrder(obj.getPojo().getName()));
            pojoCopy.setProtocol(obj.getPojo().getProtocol());
            pojoCopy.setPhisicalAddress("unknown");
            for (Representation rep : pojoCopy.getRepresentations()) {
                rep.getOffset().setX(obj.getPojo().getCurrentRepresentation().getOffset().getX() + 30);
                rep.getOffset().setY(obj.getPojo().getCurrentRepresentation().getOffset().getY() + 30);
            }
            pojoCopy.setUUID(UUID.randomUUID().toString());

            try {
                envObjectLogic = thingsFactory.create(pojoCopy);
            } catch (RepositoryException ex) {
                LOG.warn(ex.getMessage());
            }
        }

        envObjectLogic.init();

        if (!objectList.containsValue(envObjectLogic)) {
            objectList.put(envObjectLogic.getPojo().getUUID(), envObjectLogic);
            try {
                envObjectLogic.setChanged(SynchAction.CREATED);
            } catch (Exception e) {
                LOG.warn("Thing was created, but cannot set it as Changed", e);
            }
        } else {
            throw new RuntimeException("Cannot add the same object more than one time");
        }

        return envObjectLogic;
    }

    private String getNextInOrder(String name) {
        String newName = name;
        int i = 0;
        while (!this.findByName(newName).isEmpty() && (i < 1000)) {
            // i < 1000 just to avoid infinite loop in case of errors
            i++;
            newName = name + "-" + i;
        }
        return newName;
    }

    /**
     *
     */
    @RequiresPermissions("objects:delete")
    @Override
    public void deleteAll() {
        try {
            for (EnvObjectLogic el : objectList.values()) {
                delete(el);
            }
        } catch (Exception e) {
        } finally {
            objectList.clear();
        }
    }

    @Override
    @RequiresPermissions("objects:read")
    public List<EnvObjectLogic> findAll() {
        List<EnvObjectLogic> el = new ArrayList<EnvObjectLogic>();
        el.addAll(objectList.values());
        return el;
    }

    @Override
    @RequiresPermissions("objects:read")
    public List<EnvObjectLogic> findByName(String name) {
        List<EnvObjectLogic> el = new ArrayList<EnvObjectLogic>();
        for (EnvObjectLogic e : findAll()) {
            if (e.getPojo().getName().equalsIgnoreCase(name)) {
                el.add(e);
            }
        }
        return el;
    }

    @Override
    @RequiresPermissions("objects:read")
    public EnvObjectLogic findOne(String uuid) {
        return getObjectByUUID(uuid);
    }

    @Override
    @RequiresPermissions("objects:create")
    public boolean create(EnvObjectLogic item) {
        try {
            int preSize = objectList.size();
            add(item, false);
            if (preSize + 1 == objectList.size()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.error("Cannot create object", e);
            return false;
        }
    }

    @Override
    @RequiresPermissions("objects:delete")
    public boolean delete(EnvObjectLogic item) {
        return delete(item.getPojo().getUUID());
    }

    @Override
    @RequiresPermissions("objects:delete")
    public boolean delete(String uuid) {
        try {
            EnvObjectLogic eol = objectList.remove(uuid);
            try {
                eol.setChanged(SynchAction.DELETED); //force repainting on frontends clients
            } catch (Exception e) {
                LOG.warn("Cannot notify object changes");
            }
            eol.destroy();
            return true;
        } catch (Exception e) {
            LOG.error("Cannot delete object" + uuid, e);
            return false;
        }
    }

    @Override
    @RequiresPermissions("objects:update")
    public EnvObjectLogic modify(String uuid, EnvObjectLogic data) {
        try {
            delete(uuid);
            data.getPojo().setUUID(uuid);
            create(data);
            return data;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    @RequiresPermissions("objects:create")
    public EnvObjectLogic copy(EnvObjectLogic thing) {
        return add(thing, true);
    }

    @Override
    public List<EnvObjectLogic> findByEnvironment(EnvironmentLogic env) {
        return getObjectByEnvironment(env.getPojo().getUUID());
    }

    @Override
    public List<EnvObjectLogic> findByEnvironment(String uuid) {
        return getObjectByEnvironment(uuid);
    }

    @Override
    public EnvObjectLogic findByAddress(String protocol, String address) {
        return getObjectByAddress(protocol, address);
    }

    /**
     *
     * @param file
     * @return
     * @throws RepositoryException
     */
    @Override
    public EnvObjectLogic load(File file) throws RepositoryException {

        // Arguments validation
        if (file == null) {
            throw new IllegalArgumentException("Cannot load a null Thing file");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("Thing file in input is not a file");
        }

        // Configure the deserialization engine
        LOG.debug("Loading Thing from file " + file.getAbsolutePath());
        XStream xstream = FreedomXStream.getXstream();

        // Validate the object against a predefined DTD
        String xml;
        try {
            // Validate the upgraded xml against a DTD file
            xml = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/object.dtd");
            //TODO: merge this upgrade code with the XmlPreprocessor (validation should be after the upgrade)
            // Upgrade the data to be compatible with the current version (skipped if already up to date)
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
                xml = (String) dataUpgradeService.upgrade(EnvObject.class, xml, fromVersion);
            } catch (DataUpgradeException dataUpgradeException) {
                throw new RepositoryException("Cannot upgrade Thing file " + file.getAbsolutePath(), dataUpgradeException);
            }
            // Deserialize the object from the upgraded and validated xml
            EnvObject pojo = (EnvObject) xstream.fromXML(xml);
            EnvObjectLogic objectLogic = thingsFactory.create(pojo);
            LOG.info("Loaded Thing {} [id:{}] of type {}",
                    new Object[]{objectLogic.getPojo().getName(), objectLogic.getPojo().getUUID(), objectLogic.getClass().getCanonicalName()});
            return objectLogic;
        } catch (IOException ex) {
            throw new RepositoryException("Cannot read Thing file " + file.getAbsolutePath(), ex);
        } catch (XStreamException e) {
            throw new RepositoryException("Error while deserializing Thing file " + file.getAbsolutePath(), e);
        }
    }

    @Override
    public List<EnvObjectLogic> loadAll(File folder) throws RepositoryException {
        this.deleteAll();
        List<EnvObjectLogic> results = new ArrayList<EnvObjectLogic>();

        // This filter only returns object files
        FileFilter objectFileFilter
                = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isFile() && file.getName().endsWith(".xobj")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };

        File[] files = folder.listFiles(objectFileFilter);

        if (files != null) {
            for (File file : files) {
                try {
                    EnvObjectLogic loaded = load(file);
                    results.add(loaded);
                } catch (RepositoryException ex) {
                    LOG.warn(ex.getMessage());
                }
            }
        }
        return results;
    }

    @Override
    public void saveAll(File folder) throws RepositoryException {
        saveObjects(folder);
    }

    @Override
    public List<EnvObjectLogic> findByProtocol(String protocolName) {
        return getObjectByProtocol(protocolName);
    }
}
