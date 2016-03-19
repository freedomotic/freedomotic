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
package com.freedomotic.things;

import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.EnvObject;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class ThingFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ThingFactory.class.getName());

    @Inject
    private Injector injector;

    protected ThingFactory() {
        //do not build this class outsite this package
    }

    /**
     * Instantiate the right logic manager for an object pojo using the pojo
     * "type" field
     *
     * @param pojo
     * @return
     * @throws com.freedomotic.exceptions.RepositoryException
     */
    public EnvObjectLogic create(EnvObject pojo) throws RepositoryException {
        if (pojo == null) {
            throw new IllegalArgumentException("Cannot create an object logic from null object data");
        }

        try {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<?> clazz = classLoader.loadClass(pojo.getHierarchy()); //eg: com.freedomotic.things.impl.ElectricDevice
            EnvObjectLogic logic = null;
            try {
                logic = (EnvObjectLogic) clazz.newInstance();
                logic.setPojo(pojo);
                injector.injectMembers(logic);
            } catch (InstantiationException ex) {
                LOG.error(ex.getMessage());
            } catch (IllegalAccessException ex) {
                LOG.error(ex.getMessage());
            }

            return logic;
        } catch (ClassNotFoundException ex) {
            throw new RepositoryException("Class '" + pojo.getHierarchy() + "' not found. "
                    + "The related Thing plugin is not "
                    + "loaded succesfully or you have a wrong hierarchy "
                    + "value in your XML definition of the Thing."
                    + "The hierarchy value is composed by the package name plus the java file name "
                    + "like com.freedomotic.things.impl.Light not com.freedomotic.things.impl.ElectricDevice.Light", ex);
        }
    }
}
