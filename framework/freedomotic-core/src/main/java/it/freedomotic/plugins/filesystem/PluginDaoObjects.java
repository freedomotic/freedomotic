/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.filesystem;

import it.freedomotic.api.Client;

import it.freedomotic.app.Freedomotic;

import it.freedomotic.exceptions.PluginLoadingException;

import it.freedomotic.util.JarFilter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Loads plugin that are freedomotic objects
 *
 * @author enrico
 */
class PluginDaoObjects
        implements PluginDao {

    static final Logger log = Freedomotic.logger;
    private File path;

    PluginDaoObjects(File path) {
        this.path = path;
    }

    /**
     * Loads all plugins from filesystem. Note that this method always return an
     * empty list of object as the
     */
    @Override
    public List<Client> loadAll()
            throws PluginLoadingException {
        File pluginFolder = new File(path.getAbsolutePath());
        List<Client> results = new ArrayList<Client>();

        if (pluginFolder.isFile()) {
            //return an empty list
            return results;
        }

        //the list of jars in the current folder
        File[] jarList = pluginFolder.listFiles(new JarFilter());

        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    List<String> classNames = null;

                    try {
                        classNames = PluginDaoFactory.getClassesInside(jar.getAbsolutePath());
                    } catch (IOException ex) {
                        log.severe(Freedomotic.getStackTraceInfo(ex));
                    }

                    for (String className : classNames) {
                        String name = className.substring(0, className.length() - 6);
                        Class clazz;

                        try {
                            clazz = PluginDaoFactory.getClass(jar, name);

                            if (clazz.getName().startsWith("it.freedomotic.objects.")
                                    && !clazz.getName().contains("$")) {
                                Freedomotic.logger.log(Level.INFO,
                                        "Found object plugin " + clazz.getSimpleName().toString()
                                        + " in " + path);
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(PluginDaoObjects.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        } else {
            log.warning("No object can be found in folder " + pluginFolder.getAbsolutePath());
        }

        return results;
    }

    @Override
    public File getPath() {
        return path;
    }
}
