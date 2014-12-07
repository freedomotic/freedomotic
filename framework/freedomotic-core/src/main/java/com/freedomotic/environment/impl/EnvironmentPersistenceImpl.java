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

import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.persistence.FreedomXStream;
import com.freedomotic.persistence.XmlPreprocessor;
import com.freedomotic.util.Info;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
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

/**
 *
 * @author enrico
 */
class EnvironmentPersistenceImpl implements EnvironmentPersistence {

    private static final Logger LOG = Logger.getLogger(EnvironmentPersistenceImpl.class.getName());

    private File directory;
    private boolean savedAsNewEnvironment;

    @Inject
    EnvironmentPersistenceImpl(@Assisted File directory) {
        this.directory = directory;
    }

    /**
     *
     * @return
     */
    public boolean isSavedAsNewEnvironment() {
        return savedAsNewEnvironment;
    }

    /**
     *
     * @param saveAsNewEnvironment
     */
    public void setSavedAsNewEnvironment(boolean saveAsNewEnvironment) {
        this.savedAsNewEnvironment = saveAsNewEnvironment;
    }

    /**
     * Persists an environment on filesystem using XStream as serialization
     * engine (XML)
     *
     * @param environment
     * @throws RepositoryException
     */
    @Override
    public void persist(Environment environment) throws RepositoryException {
        if (!directory.isDirectory()) {
            throw new RepositoryException(directory.getAbsoluteFile() + " is not a valid environment folder. Skipped");
        }

        if (this.isSavedAsNewEnvironment()) {
            try {
                saveAs(environment);
            } catch (IOException ex) {
                throw new RepositoryException(ex);
            }
        } else {
            delete(environment);
            try {
                String uuid = environment.getUUID();
                if ((uuid == null) || uuid.isEmpty()) {
                    environment.setUUID(UUID.randomUUID().toString());
                }
                String fileName = environment.getUUID() + ".xenv";
                serialize(environment, new File(directory + "/" + fileName));
            } catch (IOException ex) {
                throw new RepositoryException(ex);
            }
        }
    }

    /**
     *
     * @param env
     * @throws IOException
     */
    public void saveAs(Environment env)
            throws IOException {
        String fileName = directory.getName();

        if (!directory.exists()) {
            directory.mkdir();
            new File(directory + "/data").mkdir();
            new File(directory + "/data/obj").mkdir();
            new File(directory + "/data/rea").mkdir();
            new File(directory + "/data/trg").mkdir();
            new File(directory + "/data/cmd").mkdir();
            new File(directory + "/data/resources").mkdir();
        }

        serialize(env, new File(directory + "/" + fileName + ".xenv"));
    }

    /**
     *
     * @param environment
     * @throws RepositoryException
     */
    @Override
    public void delete(Environment environment)
            throws RepositoryException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Loads an environment from filesystem using XStream as serialization
     * engine (XML)
     *
     * @return an Environment object or null if no environments are found in the
     * given folder
     * @throws RepositoryException
     */
    @Override
    public Collection<Environment> loadAll()
            throws RepositoryException {
        if (directory == null) {
            throw new RepositoryException("Cannot load environments from null directory");
        }

        // This filter only returns env files
        FileFilter envFileFilter
                = new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.isFile() && file.getName().endsWith(".xenv");
                    }
                };

        File[] files = directory.listFiles(envFileFilter);

        List<Environment> environments = new ArrayList<Environment>();
        for (File file : files) {
            environments.add(deserialize(file));

        }
        if (environments.isEmpty()) {
            return null;
        }
        return environments;
    }

    private void serialize(Environment env, File file) throws IOException {
        for (Zone zone : env.getZones()) {
            zone.setObjects(null);
        }
        LOG.log(Level.CONFIG, "Serializing environment to {0}", file);
        FreedomXStream.toXML(env, file);
        LOG.log(Level.INFO, "Application environment {0} succesfully serialized", env.getName());
    }

    /**
     *
     * @param file
     * @return
     * @throws RepositoryException
     */
    public static Environment deserialize(final File file) throws RepositoryException {
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

            return pojo;
        } catch (XStreamException e) {
            throw new RepositoryException("XML parsing error. Readed XML is \n" + xml, e);
        }
    }

}
