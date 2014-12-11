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
package com.freedomotic.environment.impl;

import com.freedomotic.app.AppConfig;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.util.Info;
import com.freedomotic.util.SerialClone;
import com.freedomotic.util.UidGenerator;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 * Repository to manage the {@link Environment} loaded from filesystem
 *
 * @author Enrico
 */
class EnvironmentRepositoryImpl implements EnvironmentRepository {

    // Dependencies
    private final AppConfig appConfig;
    private final EnvironmentPersistenceFactory environmentPersistenceFactory;
    private final ThingRepository thingsRepository;

    // The Environments cache
    private static final List<EnvironmentLogic> environments = new ArrayList<EnvironmentLogic>();
    private static boolean initialized = false;

    /**
     * Creates a new repository
     *
     * @param appConfig Needed to get the default environment
     * @param environmentPersistenceFactory Creates the right environment loader
     * to manage the environment persistence
     * @throws com.freedomotic.exceptions.RepositoryException
     */
    @Inject
    EnvironmentRepositoryImpl(
            AppConfig appConfig,
            ThingRepository thingsRepository,
            EnvironmentPersistenceFactory environmentPersistenceFactory)
            throws RepositoryException {
        this.appConfig = appConfig;
        this.thingsRepository = thingsRepository;
        this.environmentPersistenceFactory = environmentPersistenceFactory;
    }
    
    @Override
    public void init() throws RepositoryException {
        cacheInitialData();
    }

    /**
     * Reads initial environment data from filesystem using the
     * {@link EnvironmentPersistence} class
     *
     * @throws RepositoryException
     */
    //TODO: change it to read all environments in the folder
    private synchronized void cacheInitialData() throws RepositoryException {
        if (initialized) {
            LOG.config("Environment repository is already initialized. Skip initialization phase");
            return;
        }
        File defaultEnvironmentFolder = getDefaultEnvironmentFolder();
        EnvironmentPersistence environmentPersistence = environmentPersistenceFactory.create(defaultEnvironmentFolder);
        Collection<Environment> loadedPojo = environmentPersistence.loadAll();

        for (Environment env : loadedPojo) {
            EnvironmentLogic envLogic = new EnvironmentLogic();
            envLogic.setPojo(env);
            envLogic.setSource(new File(defaultEnvironmentFolder + "/" + env.getUUID() + ".xenv"));
            // Activate this environment
            this.create(envLogic);
        }
        EnvironmentRepositoryImpl.initialized = true;
    }

    private File getDefaultEnvironmentFolder() throws RepositoryException {
        String envFilePath = appConfig.getProperty("KEY_ROOM_XML_PATH");
        File envFile = new File(Info.PATHS.PATH_DATA_FOLDER + "/furn/" + envFilePath);
        File folder = envFile.getParentFile();

        if (folder == null) {
            throw new RepositoryException("Application configuration does not specify the default environment to load."
                    + " A 'KEY_ROOM_XML_PATH' property is expected");
        }

        if (!folder.exists()) {
            throw new RepositoryException(
                    "Folder " + folder + " do not exists. Cannot load default "
                    + "environment from " + envFile.getAbsolutePath().toString());
        } else if (!folder.isDirectory()) {
            throw new RepositoryException(
                    "Environment folder " + folder.getAbsolutePath()
                    + " is supposed to be a directory");
        }
        return folder;
    }

    /**
     *
     * @param folder
     * @throws RepositoryException
     */
    @RequiresPermissions("environments:save")
    public void saveEnvironmentsToFolder(File folder) throws RepositoryException {
        if (environments.isEmpty()) {
            LOG.warning("There is no environment to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }
        if (folder.exists() && !folder.isDirectory()) {
            throw new RepositoryException(folder.getAbsoluteFile() + " is not a valid environment folder. Skipped");
        }
        createFolderStructure(folder);
        deleteEnvFiles(folder);
        try {
            for (EnvironmentLogic environment : environments) {
                String uuid = environment.getPojo().getUUID();

                if ((uuid == null) || uuid.isEmpty()) {
                    environment.getPojo().setUUID(UUID.randomUUID().toString());
                }

                String fileName = environment.getPojo().getUUID() + ".xenv";
                save(environment,
                        new File(folder + "/" + fileName));
            }
        } catch (IOException e) {
            throw new RepositoryException(e.getCause());
        }
    }

    private static void deleteEnvFiles(File folder)
            throws RepositoryException {
        if ((folder == null) || !folder.isDirectory()) {
            throw new IllegalArgumentException("Unable to delete environment files in a null or not valid folder");
        }

        // This filter only returns object files
        FileFilter objectFileFileter
                = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        if (file.isFile() && file.getName().endsWith(".xenv")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                };

        File[] files = folder.listFiles(objectFileFileter);

        for (File file : files) {
            boolean deleted = file.delete();

            if (!deleted) {
                throw new RepositoryException("Unable to delete file " + file.getAbsoluteFile());
            }
        }
    }

    /**
     * Loads all objects file filesystem folder and adds the objects to the list
     *
     * @param folder
     * @param makeUnique
     * @return
     * @throws com.freedomotic.exceptions.RepositoryException
     * @deprecated
     */
    @Deprecated
    @Override
    public boolean loadEnvironmentsFromDir(File folder, boolean makeUnique) throws RepositoryException {
        if (folder == null) {
            throw new RepositoryException("Cannot load enviornments from a null folder");
        }
        environments.clear();

        // This filter only returns env files
        FileFilter envFileFilter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isFile() && file.getName().endsWith(".xenv");
            }
        };

        File[] files = folder.listFiles(envFileFilter);

        for (File file : files) {
            loadEnvironmentFromFile(file);
        }

        // Load all objects in this environment
        thingsRepository.loadAll(EnvironmentRepositoryImpl.getEnvironments().get(0).getObjectFolder());

        // TODO: this return value makes no sense
        return true;
    }

    /**
     * Add an environment. You can use EnvObjectPersistnce.MAKE_UNIQUE to create
     * an object that will surely be unique. Beware this means it is created
     * with defensive copy of the object in input and name, protocol, address
     * and UUID are reset to a default value.
     *
     * @param obj the environment to add
     * @param MAKE_UNIQUE can be true or false. Creates a defensive copy
     * reference to the object in input.
     * @return A pointer to the newly created environment object
     */
    @RequiresPermissions("environments:create")
    @Deprecated
    public static EnvironmentLogic add(final EnvironmentLogic obj, boolean MAKE_UNIQUE) {
        if ((obj == null)
                || (obj.getPojo() == null)
                || (obj.getPojo().getName() == null)
                || obj.getPojo().getName().isEmpty()) {
            throw new IllegalArgumentException("This is not a valid environment");
        }

        EnvironmentLogic envLogic = obj;

        if (MAKE_UNIQUE) {

            envLogic = new EnvironmentLogic();

            //defensive copy to not affect the passed object with the changes
            Environment pojoCopy = SerialClone.clone(obj.getPojo());
            pojoCopy.setName(obj.getPojo().getName() + "-" + UidGenerator.getNextStringUid());
            pojoCopy.setUUID(""); // force to assign a new random and unique UUID
            //should be the last called after using setters on envLogic.getPojo()
            envLogic.setPojo(pojoCopy);
        }

        envLogic.init();
        //  if (!envList.contains(envLogic)) {
        environments.add(envLogic);

        //envLogic.setChanged(true);
        //  } else {
        //      throw new RuntimeException("Cannot add the same environment more than one time");
        //  }
        return envLogic;
    }

   

    /**
     *
     * @param input
     */
    @RequiresPermissions("environments:delete")
    @Deprecated
    public void remove(EnvironmentLogic input) {
        for (EnvObjectLogic obj : thingsRepository.findByEnvironment(input)) {
            thingsRepository.delete(obj);
        }

        environments.remove(input);
        input.clear();
    }

    /**
     *
     */
    @RequiresPermissions("environments:delete")
    @Override
    public void deleteAll() {
        try {
            for (EnvironmentLogic el : environments) {
                delete(el);
            }
        } catch (Exception e) {
        } finally {
            environments.clear();
        }
    }

    /**
     *
     * @return
     */
    public static int size() {
        return environments.size();
    }

    private static void createFolderStructure(File folder) {
        if (!folder.exists()) {
            folder.mkdir();
            new File(folder + "/data").mkdir();
            new File(folder + "/data/obj").mkdir();
            new File(folder + "/data/rea").mkdir();
            new File(folder + "/data/trg").mkdir();
            new File(folder + "/data/cmd").mkdir();
            new File(folder + "/data/resources").mkdir();
        }
    }

    private static void save(EnvironmentLogic env, File file) throws IOException {
        for (Zone zone : env.getPojo().getZones()) {
            zone.setObjects(null);
        }
        LOG.log(Level.INFO, "Serializing environment to {0}", file);
        FreedomXStream.toXML(env.getPojo(), file);
        LOG.log(Level.INFO, "Application environment {0} succesfully serialized", env.getPojo().getName());
    }

    /**
     *
     * @param env
     * @param folder
     * @throws IOException
     */
    @RequiresPermissions("environments:save")
    @Override
    public void saveAs(EnvironmentLogic env, File folder) throws IOException {
        LOG.config("Serializing new environment to " + folder);

        createFolderStructure(folder);

        save(env, new File(folder + "/" + env.getPojo().getUUID() + ".xenv"));
    }

    /**
     *
     * @param file
     * @throws RepositoryException
     * @deprecated
     */
    @Deprecated
    public static void loadEnvironmentFromFile(final File file)
            throws RepositoryException {
        XStream xstream = FreedomXStream.getXstream();

        //validate the object against a predefined DTD
        String xml;

        try {
            xml = XmlPreprocessor.validate(file, Info.PATHS.PATH_CONFIG_FOLDER + "/validator/environment.dtd");
        } catch (IOException ex) {
            throw new RepositoryException(ex.getMessage(), ex);
        }

        Environment pojo = null;

        try {
            pojo = (Environment) xstream.fromXML(xml);
        } catch (XStreamException e) {
            throw new RepositoryException("XML parsing error. Readed XML is \n" + xml, e);
        }

        EnvironmentLogic envLogic = new EnvironmentLogic();

        if (pojo == null) {
            throw new IllegalStateException("Object data cannot be null at this stage");
        }

        envLogic.setPojo(pojo);
        envLogic.setSource(file);
        // next line is commented as the method init() is called in the add()
        //envLogic.init();
        add(envLogic, false);
    }

    /**
     *
     * @return
     */
    @Deprecated
    @RequiresPermissions("environments:read")
    public static List<EnvironmentLogic> getEnvironments() {
        return environments;
    }

    /**
     *
     * @param UUID
     * @return
     */
    @RequiresPermissions("environments:read")
    @Deprecated
    public static EnvironmentLogic getEnvByUUID(String UUID) {
        //     if (auth.isPermitted("environments:read:" + UUID)) {
        for (EnvironmentLogic env : environments) {
            if (env.getPojo().getUUID().equals(UUID)) {
                return env;
            }
        }
        //   }
        return null;
    }
    private static final Logger LOG = Logger.getLogger(EnvironmentRepositoryImpl.class.getName());

    @Override
    @RequiresPermissions("environments:read")
    public List<EnvironmentLogic> findAll() {
        return getEnvironments();
    }

    @Override
    @RequiresPermissions("environments:read")
    public List<EnvironmentLogic> findByName(String name) {
        List<EnvironmentLogic> el = new ArrayList<EnvironmentLogic>();
        for (EnvironmentLogic e : findAll()) {
            if (e.getPojo().getName().equalsIgnoreCase(name)) {
                el.add(e);
            }
        }
        return el;
    }

    @Override
    @RequiresPermissions("environments:read")
    public EnvironmentLogic findOne(String uuid) {
        return getEnvByUUID(uuid);
    }

    @Override
    @RequiresPermissions("environments:create")
    public boolean create(EnvironmentLogic item) {
        try {
            add(item, false);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @RequiresPermissions("environments:delete")
    public boolean delete(EnvironmentLogic item) {
        try {
            remove(item);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    @RequiresPermissions("environments:delete")
    public boolean delete(String uuid) {
        return delete(findOne(uuid));
    }

    @Override
    @RequiresPermissions("environments:update")
    public EnvironmentLogic modify(String uuid, EnvironmentLogic data) {
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
    @RequiresPermissions("environments:create")
    public EnvironmentLogic copy(EnvironmentLogic env) {
        return add(env, true);
    }

}
