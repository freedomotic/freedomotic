/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.impl;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.plugins.impl.BoundleLoader;
import com.freedomotic.util.JarFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
class BoundleLoaderDevices implements BoundleLoader {

    private static final Logger LOG = Logger.getLogger(BoundleLoaderDevices.class.getName());
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
