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
package com.freedomotic.environment;

import com.freedomotic.api.Client;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.persistence.ContainerInterface;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.util.DOMValidateDTD;
import com.freedomotic.util.Info;
import com.freedomotic.util.SerialClone;
import com.freedomotic.util.UidGenerator;
import com.google.inject.Inject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author Enrico
 */
public final class EnvironmentPersistence implements ContainerInterface<EnvironmentLogic> {

    private static final List<EnvironmentLogic> environments = new ArrayList<EnvironmentLogic>();
    private final ClientStorage clientStorage;

    /**
     *
     * @param clientStorage
     */
    @Inject
    public EnvironmentPersistence(ClientStorage clientStorage) {
        //disable instance creation
        this.clientStorage = clientStorage;
    }

    /**
     *
     * @param folder
     * @throws DaoLayerException
     */
    @RequiresPermissions("environments:save")
    public static void saveEnvironmentsToFolder(File folder) throws DaoLayerException {
        if (environments.isEmpty()) {
            LOG.warning("There is no environment to persist, " + folder.getAbsolutePath() + " will not be altered.");
            return;
        }

        if (folder.exists() && !folder.isDirectory()) {
            throw new DaoLayerException(folder.getAbsoluteFile() + " is not a valid environment folder. Skipped");
        }
        createFolderStructure(folder);
        deleteEnvFiles(folder);

        try {
            // Create file
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #EnvName").append("\n");

            for (EnvironmentLogic environment : environments) {
                String uuid = environment.getPojo().getUUID();

                if ((uuid == null) || uuid.isEmpty()) {
                    environment.getPojo().setUUID(UUID.randomUUID().toString());
                }

                String fileName = environment.getPojo().getUUID() + ".xenv";
                save(environment,
                        new File(folder + "/" + fileName));
                summary.append(fileName).append("\t").append(environment.getPojo().getName()).append("\n");
            }

            //writing a summary .txt file with the list of commands in this folder
            FileWriter fstream = new FileWriter(folder + "/index.txt");
            BufferedWriter indexfile = new BufferedWriter(fstream);
            indexfile.write(summary.toString());
            //Close the output stream
            indexfile.close();
        } catch (IOException e) {
            throw new DaoLayerException(e.getCause());
        }
    }

    private static void deleteEnvFiles(File folder)
            throws DaoLayerException {
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
                throw new DaoLayerException("Unable to delete file " + file.getAbsoluteFile());
            }
        }
    }

    /**
     * Loads all objects file filesystem folder and adds the objects to the list
     *
     * @param folder
     * @param makeUnique
     * @return
     * @deprecated
     */
    @Deprecated
    public synchronized static boolean loadEnvironmentsFromDir(File folder, boolean makeUnique)
            throws DaoLayerException {
        if (folder == null) {
            throw new DaoLayerException("Cannot");
        }

        environments.clear();

        boolean check = true;

        // This filter only returns env files
        FileFilter envFileFilter
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

        File[] files = folder.listFiles(envFileFilter);

        for (File file : files) {
            loadEnvironmentFromFile(file);
        }

        if (check) {
            EnvObjectPersistence.loadObjects(EnvironmentPersistence.getEnvironments().get(0).getObjectFolder(),
                    false);
        }

        return check;
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
            envLogic = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);

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
     * @param clazz
     * @param name
     * @param protocol
     * @param address
     * @return
     */
    public EnvObjectLogic join(String clazz, String name, String protocol, String address) {
        EnvObjectLogic loaded = null;
        ObjectPluginPlaceholder objectPlugin = (ObjectPluginPlaceholder) clientStorage.get(clazz);

        if (objectPlugin == null) {
            LOG.warning("Doesn't exist an object class called " + clazz);

            return null;
        }

        File templateFile = objectPlugin.getTemplate();

        try {
            loaded = EnvObjectPersistence.loadObject(templateFile);
        } catch (DaoLayerException ex) {
            LOG.severe("Cannot join an object taken from template file " + templateFile);
        }

        //changing the name and other properties invalidates related trigger and commands
        //call init() again after this changes
        if ((name != null) && !name.isEmpty()) {
            loaded.getPojo().setName(name);
        } else {
            loaded.getPojo().setName(protocol);
        }

        loaded = EnvObjectPersistence.add(loaded, EnvObjectPersistence.MAKE_UNIQUE);
        loaded.getPojo().setProtocol(protocol);
        loaded.getPojo().setPhisicalAddress(address);
        loaded.setRandomLocation();

        //set the PREFERRED MAPPING of the protocol plugin (if any is defined in its manifest)
        Client addon = clientStorage.getClientByProtocol(protocol);

        if (addon != null) {
            for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
                Map tuple = addon.getConfiguration().getTuples().getTuple(i);
                String regex = (String) tuple.get("object.class");

                if ((regex != null) && clazz.matches(regex)) {
                    //map object behaviors to hardware triggers
                    for (Behavior behavior : loaded.getPojo().getBehaviors()) {
                        String triggerName = (String) tuple.get(behavior.getName());
                        if (triggerName != null) {
                            loaded.addTriggerMapping(TriggerPersistence.getTrigger(triggerName),
                                    behavior.getName());
                        }
                    }

                    for (String action : loaded.getPojo().getActions().stringPropertyNames()) {
                        String commandName = (String) tuple.get(action);

                        if (commandName != null) {
                            loaded.setAction(action,
                                    CommandPersistence.getHardwareCommand(commandName));
                        }
                    }
                }
            }
        }

        return loaded;
    }

    /**
     *
     * @param input
     */
    @RequiresPermissions("environments:delete")
    @Deprecated
    public static void remove(EnvironmentLogic input) {
        for (EnvObjectLogic obj : EnvObjectPersistence.getObjectByEnvironment(input.getPojo().getUUID())) {
            EnvObjectPersistence.remove(obj);
        }

        environments.remove(input);
        input.clear();
    }

    /**
     *
     */
    @RequiresPermissions("environments:delete")
    @Override
    public void clear() {
        try {
            for (EnvironmentLogic el : environments){
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

    private static void save(EnvironmentLogic env, File file)
            throws IOException {
        XStream xstream = FreedomXStream.getEnviromentXstream();

        for (Zone zone : env.getPojo().getZones()) {
            zone.setObjects(null);
        }

        String xml = xstream.toXML(env.getPojo());
        FileWriter fstream;
        BufferedWriter out = null;

        try {
            LOG.info("Serializing environment to " + file);
            fstream = new FileWriter(file);
            out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            LOG.info("Application environment " + env.getPojo().getName()
                    + " succesfully serialized");
        } catch (IOException ex) {
            Logger.getLogger(Environment.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            out.close();
        }
    }

    /**
     *
     * @param env
     * @param folder
     * @throws IOException
     */
    @RequiresPermissions("environments:save")
    public static void saveAs(EnvironmentLogic env, File folder) throws IOException {
        LOG.config("Serializing new environment to " + folder);

        createFolderStructure(folder);

        save(env,
                new File(folder + "/" + env.getPojo().getUUID() + ".xenv"));

        //TODO: Freedomotic.environment.getPojo().setObjectsFolder()
        //  EnvObjectPersistence.saveObjects(new File(folder + "/objects"));
    }

    /**
     *
     * @param file
     * @throws DaoLayerException
     * @deprecated
     */
    @Deprecated
    public static void loadEnvironmentFromFile(final File file)
            throws DaoLayerException {
        XStream xstream = FreedomXStream.getXstream();

        //validate the object against a predefined DTD
        String xml;

        try {
            xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/environment.dtd");
        } catch (IOException ex) {
            throw new DaoLayerException(ex.getMessage(), ex);
        }

        Environment pojo = null;

        try {
            pojo = (Environment) xstream.fromXML(xml);
        } catch (XStreamException e) {
            throw new DaoLayerException("XML parsing error. Readed XML is \n" + xml, e);
        }

        EnvironmentLogic envLogic = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);

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
    private static final Logger LOG = Logger.getLogger(EnvironmentPersistence.class.getName());

    @Override
    @RequiresPermissions("environments:read")
    public List<EnvironmentLogic> list() {
        return getEnvironments();
    }

    @Override
    @RequiresPermissions("environments:read")
    public List<EnvironmentLogic> getByName(String name) {
        List<EnvironmentLogic> el = new ArrayList<EnvironmentLogic>();
        for (EnvironmentLogic e : list()) {
            if (e.getPojo().getName().equalsIgnoreCase(name)) {
                el.add(e);
            }
        }
        return el;
    }

    @Override
    @RequiresPermissions("environments:read")
    public EnvironmentLogic get(String uuid) {
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
        return delete(get(uuid));
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
    public EnvironmentLogic copy(String uuid) {
        return add(get(uuid), true);
    }

    
    
    
}
