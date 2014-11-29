/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.objects;

import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.EnvObject;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class EnvObjectLogicFactory {

    
    /**
     * Instantiate the right logic manager for an object pojo using the pojo
     * "type" field
     *
     * @param pojo
     * @return
     * @throws com.freedomotic.exceptions.RepositoryException
     */
    public static EnvObjectLogic create(EnvObject pojo) throws RepositoryException {
        if (pojo == null) {
            throw new IllegalArgumentException("Cannot create an object logic from null object data");
        }

        try {
            URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
            Class<?> clazz = classLoader.loadClass(pojo.getHierarchy()); //eg: com.freedomotic.objects.impl.ElectricDevice
            EnvObjectLogic logic = null;
            try {
                logic = (EnvObjectLogic) clazz.newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(EnvObjectLogicFactory.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(EnvObjectLogicFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
            logic.setPojo(pojo);

            return logic;
            // } catch (InstantiationException ex) {
            //     throw new DaoLayerException(ex);
            // } catch (IllegalAccessException ex) {
            //     throw new DaoLayerException(ex);
        } catch (ClassNotFoundException ex) {
            throw new RepositoryException("Class '" + pojo.getHierarchy() + "' not found. "
                    + "The related object plugin is not "
                    + "loaded succesfully or you have a wrong hierarchy "
                    + "value in your XML definition of the object."
                    + "The hierarchy value is composed by the package name plus the java file name "
                    + "like com.freedomotic.objects.impl.Light not com.freedomotic.objects.impl.ElectricDevice.Light");
        }
    }

    private EnvObjectLogicFactory() {
        // Suppress default constructor for noninstantiability
        throw new AssertionError();
    }
}
