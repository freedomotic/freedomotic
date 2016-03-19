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
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.util.JarFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class BoundleLoaderDevices implements BoundleLoader {

    private static final Logger LOG = LoggerFactory.getLogger(BoundleLoaderDevices.class.getName());
    private File path;

    BoundleLoaderDevices(File path) {
        this.path = path;
    }

    @Override
    public List<Client> loadBoundle() throws PluginLoadingException {
        File pluginRootFolder = new File(path.getAbsolutePath());
        List<Client> results = new ArrayList<Client>();

        if (pluginRootFolder.isFile()) {
            return results;
        }

        //the list of jars in the current folder
        File[] jarFiles = pluginRootFolder.listFiles(new JarFilter());

        //the list of files in the jar
        for (File pluginJar : jarFiles) {
            if (pluginJar.isFile()) {
                try {
                    List<String> classNames = BoundleLoaderFactory.getClassesInside(pluginJar.getAbsolutePath());

                    for (String className : classNames) {
                        //remove the .class at the end of file
                        String name = className.substring(0, className.length() - 6);
                        Class clazz = BoundleLoaderFactory.getClass(pluginJar, name);
                        Class superclass = clazz.getSuperclass();
                        Plugin plugin;

                        if (superclass != null) { //null if class is Object
                            //we allow the dynamic loading only to ADDONS of this classes

                            if ((superclass.getName().equals("com.freedomotic.api.Actuator"))
                                    || (superclass.getName().equals("com.freedomotic.api.Sensor"))
                                    || (superclass.getName().equals("com.freedomotic.api.Protocol"))
                                    || (superclass.getName().equals("com.freedomotic.api.Intelligence"))
                                    || (superclass.getName().equals("com.freedomotic.api.Tool"))) {
                                try {
                                    plugin = (Plugin) clazz.newInstance(); //later it gets injected by guice
                                    results.add(plugin);
                                } catch (InstantiationException ex) {
                                    throw new PluginLoadingException("Cannot instantiate plugin " + path.getAbsolutePath(), ex);
                                } catch (IllegalAccessException ex) {
                                    throw new PluginLoadingException(ex.getMessage(), ex);
                                } catch (NoClassDefFoundError noClassDefFoundError) {
                                    throw new PluginLoadingException("This plugin miss a library neccessary to work correctly or "
                                            + "calls a method that no longer exists. "
                                            + noClassDefFoundError.getMessage(),
                                            noClassDefFoundError);
                                }
                            }
                        }
                    }
                } catch (Exception ex) {
                    throw new PluginLoadingException("Generic error while loading boundle " + pluginJar.getAbsolutePath(), ex);
                }
            }
        }

        return results;
    }

    @Override
    public File getPath() {
        return path;
    }
}
