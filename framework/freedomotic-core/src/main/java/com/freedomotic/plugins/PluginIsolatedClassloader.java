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

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * THIS IS CURRENTLY NOT USED A parent-last classloader that will try the child
 * classloader first and then the parent.
 */
public class PluginIsolatedClassloader extends ClassLoader {

    private ChildURLClassLoader childClassLoader;

    /**
     * Accepts a folder containing a set of jar files to load
     *
     * @param jarDir
     */
    public PluginIsolatedClassloader(String jarDir) {
        super(Thread.currentThread().getContextClassLoader());
        // search for JAR files in the given directory
        FileFilter jarFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".jar");
            }
        };
        // create URL for each JAR file found
        File[] jarFiles = new File(jarDir).listFiles(jarFilter);
        URL[] urls;
        if (null != jarFiles) {
            urls = new URL[jarFiles.length];
            for (int i = 0; i < jarFiles.length; i++) {
                try {
                    urls[i] = jarFiles[i].toURI().toURL();
                } catch (MalformedURLException e) {
                    throw new RuntimeException(
                            "Could not get URL for JAR file: " + jarFiles[i], e);
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

            System.out.println("Isolated classloader try to load class " + name);
            // first try to find a class inside the child classloader
            return childClassLoader.findClass(name);
        } catch (ClassNotFoundException e) {
            // didn't find it, try the parent
            return super.loadClass(name, resolve);
        }
    }

    /**
     * This class delegates (child then parent) for the findClass method for a
     * URLClassLoader. Need this because findClass is protected in
     * URLClassLoader
     */
    private class ChildURLClassLoader extends URLClassLoader {

        private ClassLoader realParent;

        public ChildURLClassLoader(URL[] urls, ClassLoader realParent) {
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
                    System.out.println("Class " + name + " is already loaded by " + cls.getClassLoader().toString());
                    return cls;
                }

                if (name.contains("org.slf4j")) {
                    System.out.println("Cannot load logging libraries. Delegate to parent");
                    return realParent.loadClass(name);
                }

                // first try to use the URLClassLoader findClass
                return super.findClass(name);
            } catch (ClassNotFoundException e) {
                // if that fails, ask real parent classloader to load the
                // class (give up)
                return realParent.loadClass(name);
            }
        }
    }
}
