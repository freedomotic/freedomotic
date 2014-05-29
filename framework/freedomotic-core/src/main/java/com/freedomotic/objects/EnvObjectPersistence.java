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
package com.freedomotic.objects;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.exceptions.DaoLayerException;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.persistence.ContainerInterface;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.util.DOMValidateDTD;
import com.freedomotic.util.Info;
import com.freedomotic.util.SerialClone;
import com.freedomotic.util.UidGenerator;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author Enrico
 */
public class EnvObjectPersistence implements ContainerInterface<EnvObjectLogic> {

    /**
     *
     */
    public static final boolean MAKE_UNIQUE = true;

    /**
     *
     */
    public static final boolean MAKE_NOT_UNIQUE = false;
    private static final Map<String, EnvObjectLogic> objectList = new HashMap<String, EnvObjectLogic>();

    /**
     *
     */
    public EnvObjectPersistence() {
        //disable instance creation
    }

    /**
     *
     * @return
     */
    @Deprecated
    @RequiresPermissions("objects:read")
    public static Collection<EnvObjectLogic> getObjectList() {
        return objectList.values();
    }

    /**
     *
     * @param folder
     * @throws DaoLayerException
     */
    @RequiresPermissions("objects:save")
    public static void saveObjects(File folder) throws DaoLayerException {
        if (objectList.isEmpty()) {
            throw new DaoLayerException("There are no object to persist, " + folder.getAbsolutePath()
                    + " will not be altered.");
        }

        if (!folder.isDirectory()) {
            throw new DaoLayerException(folder.getAbsoluteFile() + " is not a valid object folder. Skipped");
        }

        try {
            XStream xstream = FreedomXStream.getXstream();
            deleteObjectFiles(folder);

            // Create file
            StringBuilder summary = new StringBuilder();
            //print an header for the index.txt file
            summary.append("#Filename \t\t #EnvObjectName \t\t\t #EnvObjectType \t\t\t #Protocol \t\t\t #Address")
                    .append("\n");

            for (EnvObjectLogic envObject : objectList.values()) {
                String uuid = envObject.getPojo().getUUID();

                if ((uuid == null) || uuid.isEmpty()) {
                    envObject.getPojo().setUUID(UUID.randomUUID().toString());
                }

                if ((envObject.getPojo().getEnvironmentID() == null)
                        || envObject.getPojo().getEnvironmentID().isEmpty()) {
                    envObject.getPojo()
                            .setEnvironmentID(EnvironmentPersistence.getEnvironments().get(0).getPojo().getUUID());
                }

                String fileName = envObject.getPojo().getUUID() + ".xobj";
                FileWriter fstream = new FileWriter(folder + "/" + fileName);
                BufferedWriter out = new BufferedWriter(fstream);
                out.write(xstream.toXML(envObject.getPojo())); //persist only the data not the logic
                summary.append(fileName).append("\t").append(envObject.getPojo().getName()).append("\t")
                        .append(envObject.getPojo().getType()).append("\t")
                        .append(envObject.getPojo().getProtocol()).append("\t")
                        .append(envObject.getPojo().getPhisicalAddress()).append("\n");
                //Close the output stream
                out.close();
                fstream.close();
            }

            //writing a summary .txt file with the list of commands in this folder
            FileWriter fstream = new FileWriter(folder + "/index.txt");
            BufferedWriter indexfile = new BufferedWriter(fstream);
            indexfile.write(summary.toString());
            //Close the output stream
            indexfile.close();
        } catch (IOException ex) {
            throw new DaoLayerException(ex);
        }
    }

    private static void deleteObjectFiles(File folder)
            throws DaoLayerException {
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
                throw new DaoLayerException("Unable to delete file " + file.getAbsoluteFile());
            }
        }
    }

    /**
     * Loads all objects file filesystem folder and adds the objects to the list
     *
     * @param folder
     * @param makeUnique
     * @throws com.freedomotic.exceptions.DaoLayerException
     */
    public synchronized static void loadObjects(File folder, final boolean makeUnique)
            throws DaoLayerException {
        objectList.clear();
        System.out.println("DEBUG: loading objects from " + folder.getAbsolutePath());

        File[] files = folder.listFiles();

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

        files = folder.listFiles(objectFileFilter);

        if (files != null) {
            for (File file : files) {
                EnvObjectLogic loaded = loadObject(file);

                if (loaded != null) {
                    EnvironmentLogic env = EnvironmentPersistence.getEnvByUUID(loaded.getPojo().getEnvironmentID());
                    if (env != null) {
                        loaded.setEnvironment(env);
                    } else {
                        loaded.setEnvironment(EnvironmentPersistence.getEnvironments().get(0));
                        LOG.warning("Reset environment UUID of object " + loaded.getPojo().getName()
                                + " to the default environment. This is because the environment UUID "
                                + loaded.getPojo().getEnvironmentID() + " does not exists.");
                    }
                    add(loaded, makeUnique);
                }
            }
        }
    }

    /**
     * Loads the object file from file but NOT add the object to the list
     *
     * @param file
     * @return
     */
    public static EnvObjectLogic loadObject(File file)
            throws DaoLayerException {
        XStream xstream = FreedomXStream.getXstream();

        //validate the object against a predefined DTD
        String xml;

        try {
            xml = DOMValidateDTD.validate(file, Info.getApplicationPath() + "/config/validator/object.dtd");

            EnvObject pojo = (EnvObject) xstream.fromXML(xml);
            EnvObjectLogic objectLogic = null;

            try {
                objectLogic = EnvObjectFactory.create(pojo);
                LOG.config("Created a new logic for " + objectLogic.getPojo().getName()
                        + " of type " + objectLogic.getClass().getCanonicalName().toString());

                return objectLogic;
            } catch (DaoLayerException daoLayerException) {
                LOG.warning(daoLayerException.getMessage());
            }
        } catch (IOException ex) {
            throw new DaoLayerException(ex.getMessage(), ex);
        } catch (XStreamException e) {
            throw new DaoLayerException(e.getMessage(), e);
        }
        //EnvObjectLogic objectLogic = EnvObjectFactory.save(pojo);
        //LOG.info("Created a new logic for " + objectLogic.getPojo().getName() + " of type " + objectLogic.getClass().getCanonicalName().toString());
        //add(objectLogic);
        return null;
    }

    /**
     *
     * @return
     */
    @Deprecated
    @RequiresPermissions("objects:read")
    public static Iterator<EnvObjectLogic> iterator() {
        return objectList.values().iterator();
    }

    /**
     * Gets the object by name
     *
     * @param name
     * @return
     */
    @RequiresPermissions("objects:read")
    public static EnvObjectLogic getObjectByName(String name) {
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
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
    public static ArrayList<EnvObjectLogic> getObjectByTags(String tags) {
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
    public static EnvObjectLogic getObjectByUUID(String uuid) {
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
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
     * @return
     */
    @RequiresPermissions("objects:read")
    public static ArrayList<EnvObjectLogic> getObjectByAddress(String protocol, String address) {
        if ((protocol == null)
                || (address == null)
                || protocol.trim().equalsIgnoreCase("unknown")
                || address.trim().equalsIgnoreCase("unknown")
                || protocol.isEmpty()
                || address.isEmpty()) {
            throw new IllegalArgumentException();
        }

        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if ((object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim()))
                    && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(address.trim())) //           && auth.isPermitted("objects:read:" + object.getPojo().getUUID())
                    ) {
                list.add(object);
            }
        }

        if (list.isEmpty()) {
            LOG.warning("Don't exist an object with protocol '" + protocol + "' and address '"
                    + address + "'");
        }

        return list;
    }

    /**
     * Gets the object by its protocol
     *
     * @param protocol
     * @return
     */
    @RequiresPermissions("objects:read")
    public static ArrayList<EnvObjectLogic> getObjectByProtocol(String protocol) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
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
    public static ArrayList<EnvObjectLogic> getObjectByEnvironment(String uuid) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
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
    public static int size() {
        return objectList.size();
    }

    /**
     * Add an object to the environment. You can use
     * EnvObjectPersistnce.MAKE_UNIQUE to save an object that will surely be
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
    public static EnvObjectLogic add(final EnvObjectLogic obj, final boolean MAKE_UNIQUE) {
        if ((obj == null)
                || (obj.getPojo() == null)
                || (obj.getPojo().getName() == null)
                || obj.getPojo().getName().isEmpty()) {
            throw new IllegalArgumentException("This is not a valid object");
        }

        EnvObjectLogic envObjectLogic = obj;

        if (MAKE_UNIQUE) {
            //defensive copy to not affect the passed object with the changes
            EnvObject pojoCopy = SerialClone.clone(obj.getPojo());
            pojoCopy.setName(obj.getPojo().getName() + "-" + UidGenerator.getNextStringUid());
            pojoCopy.setProtocol(obj.getPojo().getProtocol());
            pojoCopy.setPhisicalAddress("unknown");
            pojoCopy.getCurrentRepresentation().getOffset().setX(obj.getPojo().getCurrentRepresentation().getOffset().getX() + 30);
            pojoCopy.getCurrentRepresentation().getOffset().setY(obj.getPojo().getCurrentRepresentation().getOffset().getY() + 30);
            pojoCopy.setUUID(UUID.randomUUID().toString());

            try {
                envObjectLogic = EnvObjectFactory.create(pojoCopy);
            } catch (DaoLayerException ex) {
                LOG.warning(ex.getMessage());
            }
        }

        envObjectLogic.init();

        if (!objectList.containsValue(envObjectLogic)) {
            objectList.put(envObjectLogic.getPojo().getUUID(),
                    envObjectLogic);
            try {
                envObjectLogic.setChanged(true);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Object was created, but cannot set is as Changed", e);
            }
        } else {
            throw new RuntimeException("Cannot add the same object more than one time");
        }

        return envObjectLogic;
    }

    /**
     *
     * @param input
     */
    @Deprecated
    @RequiresPermissions("objects:delete")
    public static void remove(EnvObjectLogic input) {
        objectList.remove(input.getPojo().getUUID());
        input.setChanged(true); //force repainting on frontends clients
        input.destroy(); //free memory
    }

    /**
     *
     */
    @RequiresPermissions("objects:delete")
    @Override
    public void clear() {
        try {
            for (EnvObjectLogic el : objectList.values()) {
                delete(el);
            }
        } catch (Exception e) {
        } finally {
            objectList.clear();
        }
    }
    private static final Logger LOG = Logger.getLogger(EnvObjectPersistence.class.getName());

    @Override
    @RequiresPermissions("objects:read")
    public List<EnvObjectLogic> list() {
        LOG.info("OBJECT LIST SIZE: " + objectList.size());
        List<EnvObjectLogic> el = new ArrayList<EnvObjectLogic>();
        el.addAll(objectList.values());
        return el;
    }

    @Override
    @RequiresPermissions("objects:read")
    public List<EnvObjectLogic> getByName(String name) {
        List<EnvObjectLogic> el = new ArrayList<EnvObjectLogic>();
        for (EnvObjectLogic e : list()) {
            if (e.getPojo().getName().equalsIgnoreCase(name)) {
                el.add(e);
            }
        }
        return el;
    }

    @Override
    @RequiresPermissions("objects:read")
    public EnvObjectLogic get(String uuid) {
        return getObjectByUUID(uuid);
    }

    @Override
    @RequiresPermissions("objects:create")
    public boolean create(EnvObjectLogic item) {
        try {
            int preSize = objectList.size();
            add(item, false);
            LOG.info("OBJECT LIST SIZE: " + objectList.size());
            if (preSize + 1 == objectList.size()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot create object", e);
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
                eol.setChanged(true); //force repainting on frontends clients
            } catch (Exception e) {
                LOG.warning("Cannot notify object changes");
            }
            eol.destroy();
            return true;
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Cannot delete object" + uuid, e);
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
    public EnvObjectLogic copy(String uuid) {
        return add(get(uuid), true);
    }
}
