/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.freedomotic.objects;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.XStreamException;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.environment.EnvironmentPersistence;

import it.freedomotic.exceptions.DaoLayerException;

import it.freedomotic.model.object.EnvObject;
import it.freedomotic.persistence.FreedomXStream;
import it.freedomotic.security.Auth;
import it.freedomotic.util.DOMValidateDTD;
import it.freedomotic.util.Info;
import it.freedomotic.util.SerialClone;
import it.freedomotic.util.UidGenerator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import com.thoughtworks.xstream.XStream;

import java.util.*;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;


/**
 *
 * @author Enrico
 */
public class EnvObjectPersistence {

    public static final boolean MAKE_UNIQUE = true;
    public static final boolean MAKE_NOT_UNIQUE = false;
    private static Map<String, EnvObjectLogic> objectList = new HashMap<String, EnvObjectLogic>();

    public EnvObjectPersistence() {
        //disable instance creation
    }

    @RequiresPermissions("objects:read")
    public static Collection<EnvObjectLogic> getObjectList() {
        return objectList.values();
    }

    @RequiresPermissions("objects:save")
    public static void saveObjects(File folder) throws DaoLayerException  {
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
                            .setEnvID(EnvironmentPersistence.getEnvironments().get(0).getPojo().getUUID());
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
        FileFilter objectFileFileter =
                new FileFilter() {
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
     */
    public synchronized static void loadObjects(File folder, boolean makeUnique)
            throws DaoLayerException {
        objectList.clear();

        File[] files = folder.listFiles();

        // This filter only returns object files
        FileFilter objectFileFilter =
                new FileFilter() {
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
                    add(loaded, makeUnique);
                }
            }
        }
    }

    /**
     * Loads the object file from file but NOT add the object to the list
     *
     * @param file
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
        //EnvObjectLogic objectLogic = EnvObjectFactory.create(pojo);
        //LOG.info("Created a new logic for " + objectLogic.getPojo().getName() + " of type " + objectLogic.getClass().getCanonicalName().toString());
        //add(objectLogic);
        return null;
    }

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
            if (object.getPojo().getName().equalsIgnoreCase(name)
                    && Auth.isPermitted("objects:read:" + object.getPojo().getUUID())) {
                return object;
            }
        }

        return null;
    }

    /**
     * Gets the object by name
     *
     * @param uuid
     * @return
     */
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
                    && (object.getPojo().getPhisicalAddress().equalsIgnoreCase(address.trim()))
                    && Auth.isPermitted("objects:read:" + object.getPojo().getUUID())) {
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
            if (object.getPojo().getProtocol().equalsIgnoreCase(protocol.trim())
                    && Auth.isPermitted("objects:read:" + object.getPojo().getUUID())) {
                list.add(object);
            }
        }

        return list;
    }

    /**
     * Gets the object by its environment
     *
     * @param  uuid
     * @return
     */
    @RequiresPermissions("objects:read")
    public static ArrayList<EnvObjectLogic> getObjectByEnvironment(String uuid) {
        ArrayList<EnvObjectLogic> list = new ArrayList<EnvObjectLogic>();
        for (Iterator<EnvObjectLogic> it = EnvObjectPersistence.iterator(); it.hasNext();) {
            EnvObjectLogic object = it.next();
            if (object.getPojo().getEnvironmentID().equalsIgnoreCase(uuid)
                    && Auth.isPermitted("objects:read:" + object.getPojo().getUUID().substring(0, 5))) {
                list.add(object);
            }
        }

        return list;
    }

    @RequiresPermissions("objects:read")
    public static int size() {
        return objectList.size();
    }

    /**
     * Add an object to the environment. You can use
     * EnvObjectPersistnce.MAKE_UNIQUE to create an object that will surely be
     * unique. Beware this means it is created with defensive copy of the object
     * in input and name, protocol, address and UUID are reset to a default
     * value.
     *
     * @param obj the environment object to add
     * @param MAKE_UNIQUE can be true or false. Creates a defensive copy
     * reference to the object in input.
     * @return A pointer to the newly created environment object
     */
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
            envObjectLogic.setChanged(true);
        } else {
            throw new RuntimeException("Cannot add the same object more than one time");
        }

        return envObjectLogic;
    }

    @RequiresPermissions("objects:delete")
    public static void remove(EnvObjectLogic input) {
        objectList.remove(input.getPojo().getUUID());
        input.setChanged(true); //force repainting on frontends clients
        input.destroy(); //free memory
    }

    @RequiresPermissions("objects:delete")
    public static void clear() {
        try {
            objectList.clear();
        } catch (Exception e) {
        }
    }
    private static final Logger LOG = Logger.getLogger(EnvObjectPersistence.class.getName());
}
