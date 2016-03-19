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
package com.freedomotic.marketplace;

import com.freedomotic.util.JarFilter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows programs to modify the classpath during runtime.
 */
public class ClassPathUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(ClassPathUpdater.class.getName());
    /**
     * Used to find the method signature.
     */
    private static final Class<?>[] PARAMETERS = new Class<?>[]{URL.class};
    /**
     * Class containing the private addURL method.
     */
    private static final Class<? extends ClassLoader> CLASS_LOADER = URLClassLoader.class;

    /**
     * Adds a new path to the classloader. If the given string points to a file,
     * then that file's parent file (i.e., directory) is used as the directory
     * to add to the classpath. If the given string represents a directory, then
     * the directory is directly added to the classpath.
     *
     * @param s The directory to add to the classpath (or a file, which will
     * relegate to its directory).
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static void add(String s)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        add(new File(s));
    }

    /**
     * Adds a new path to the classloader. If the given file object is a file,
     * then its parent file (i.e., directory) is used as the directory to add to
     * the classpath. If the given string represents a directory, then the
     * directory it represents is added.
     *
     * @param f The directory (or enclosing directory if a file) to add to the
     * classpath.
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static void add(File f)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        // f = f.isDirectory() ? f : f.getParentFile();
        if (f.isDirectory()) {
            File[] jarList = f.listFiles(new JarFilter());

            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    add(jar.toURI().toURL());
                }
            }
        } else {
            add(f.toURI().toURL());
        }
    }

    /**
     * Adds a new path to the classloader. The class must point to a directory,
     * not a file.
     *
     * @param url The path to include when searching the classpath.
     * @throws java.lang.reflect.InvocationTargetException
     */
    public static void add(URL url)
            throws IOException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method method = CLASS_LOADER.getDeclaredMethod("addURL", PARAMETERS);
        method.setAccessible(true);
        method.invoke(getClassLoader(),
                new Object[]{url});
    }

    private static URLClassLoader getClassLoader() {
        return (URLClassLoader) ClassLoader.getSystemClassLoader();
    }

    private ClassPathUpdater() {
    }
}
