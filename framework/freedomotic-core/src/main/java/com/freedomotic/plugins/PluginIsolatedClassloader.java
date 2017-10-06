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
package com.freedomotic.plugins;

import com.freedomotic.app.Freedomotic;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import com.freedomotic.exceptions.FreedomoticRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * THIS IS CURRENTLY NOT USED A parent-last classloader that will try the child
 * classloader first and then the parent.
 */
public class PluginIsolatedClassloader extends ClassLoader {

    /**
     * Class logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(PluginIsolatedClassloader.class.getName());

    /**
     * Child class loader
     */
    private ChildURLClassLoader childClassLoader;

    /**
     * Accepts a folder containing a set of jar files to load
     *
     * @param jarDir JAR directory
     */
    public PluginIsolatedClassloader(String jarDir) {
        super(Thread.currentThread().getContextClassLoader());

        // create URL for each JAR file found
        File[] jarFiles = new File(jarDir).listFiles((File pathname) ->
                pathname.getName().endsWith(".jar"));
        URL[] urls;
        if (null != jarFiles) {
            urls = new URL[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++) {
                try {
                    urls[i] = jarFiles[i].toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new FreedomoticRuntimeException(
                            "Could not get URL for JAR file: " + jarFiles[i]);
                }
            }
        } else {
            // no JAR files found
            urls = new URL[0];
        }
        childClassLoader = new ChildURLClassLoader(urls, this.getParent());
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve)
            throws ClassNotFoundException {
        try {

            LOG.info("Isolated classloader tries to load class \"{}\"", name);
            // first try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            // didn't find it, try the parent
            LOG.error(Freedomotic.getStackTraceInfo(e));
            return super.loadClass(name, resolve);
        }
    }

    /**
     * Delegates (child then parent) for the {@link URLClassLoader#findClass(String)} method.
     * Needed because findClass is protected in URLClassLoader
     */
    private class ChildURLClassLoader extends URLClassLoader {

        private ClassLoader realParent;

        ChildURLClassLoader(URL[] urls, ClassLoader realParent) {
            // pass null as parent so upward delegation disabled for first
            // findClass call
            super(urls, null);
            this.realParent = realParent;
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            try {
                // 1. is this class already loaded?
                Class cls = super.findLoadedClass(name);
                if (cls != null) {
                    LOG.info("Class \"{}\" is already loaded by \"{}\"", name, cls.getClassLoader().toString());
                    return cls;
                }

                if (name.contains("org.slf4j")) {
                    LOG.error("Cannot load logging libraries. Delegate to parent");
                    return realParent.loadClass(name);
                }

                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // if that fails, ask real parent classloader to load the
                // class (give up)
                LOG.error(Freedomotic.getStackTraceInfo(e));
                return realParent.loadClass(name);
            }
        }
    }
}
