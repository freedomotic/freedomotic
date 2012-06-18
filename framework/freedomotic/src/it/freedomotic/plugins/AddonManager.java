//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.com
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package it.freedomotic.plugins;

import it.freedomotic.api.Client;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.util.CopyFile;
import it.freedomotic.util.FetchHttpFiles;
import it.freedomotic.util.Info;
import it.freedomotic.util.Unzip;
import java.io.FileFilter;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class AddonManager {

    // Parameters
    private static final Class[] PARAMETERS = new Class[]{URL.class};
    public static final List<Plugin> ADDONS = new ArrayList<Plugin>();

    public AddonManager() {
    }

    public void searchIn(File directory) throws Exception {
        //intantiate the right loader based on the directory passed to the searchIn method
        String devicesPath = new File(Info.getPluginsPath() + "/devices/").toString();
        int numLoadedPlugins = 0;
        if (directory.toString().startsWith(devicesPath)) {
            Freedomotic.logger.info("--- Dynamic loading of a Plugin Archive from " + directory.toString() + " ---");
            AddonLoaderInterface device = new DevicesLoader();
            device.load(this, directory);
            numLoadedPlugins++;
        }
        String objectsPath = new File(Info.getPluginsPath() + "/objects/").toString();
        if (directory.toString().startsWith(objectsPath)) {
            Freedomotic.logger.info("--- Dynamic loading of Object Addons from " + directory.toString() + " ---");
            AddonLoaderInterface object = new ObjectLoader();
            object.load(this, directory);
            numLoadedPlugins++;
        }
        String eventsPath = new File(Info.getPluginsPath() + "/events/").toString();
        if (directory.toString().startsWith(eventsPath)) {
            Freedomotic.logger.info("--- Dynamic loading of Event Addons from " + directory.toString() + " ---");
            AddonLoaderInterface event = new EventLoader();
            event.load(this, directory);
            numLoadedPlugins++;
        }
        if (numLoadedPlugins > 0) { //at least one plugin in this package is loaded succesfully
            //now load data for this jar (can contain more than one plugin)
            //resources are mergend in the default resources folder
            CommandPersistence.loadCommands(new File(directory + "/data/cmd"));
            TriggerPersistence.loadTriggers(new File(directory + "/data/trg"));
            ReactionPersistence.loadReactions(new File(directory + "/data/rea"));
            //create ad-hoc subfolders of temp
            File destination = new File(Info.getResourcesPath() + "/temp/" + directory.getName());
            destination.mkdir();
            copyAllFiles(new File(directory + "/data/resources"), destination);
        }
    }

    public void recursiveSearchIn(File directory) {
        if (directory.isDirectory()) {
            //search in subfolder. Go down a level starting from /plugins/. Is not real recursive
            for (File subfolder : directory.listFiles()) {
                if (subfolder.isDirectory()) {
                    try {
                        searchIn(subfolder);
                    } catch (Exception ex) {
                        Logger.getLogger(AddonManager.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    protected List<String> getClassNames(String jarName) throws IOException {
        ArrayList<String> classes = new ArrayList<String>(10);
        JarInputStream jarFile = new JarInputStream(new FileInputStream(jarName));
        JarEntry jarEntry;
        while (true) {
            jarEntry = jarFile.getNextJarEntry();
            if (jarEntry == null) {
                break;
            }
            if (jarEntry.getName().endsWith(".class")) {
                classes.add(jarEntry.getName().replaceAll("/", "\\."));
            }
        }
        return classes;
    }

    public Class getClass(File file, String name) throws Exception {
        addURL(file.toURL());
        URLClassLoader clazzLoader;
        Class clazz;
        String filePath = file.getAbsolutePath();
        filePath = "jar:file://" + filePath + "!/";
        URL url = new File(filePath).toURL();
        clazzLoader = new URLClassLoader(new URL[]{url});
        clazz = clazzLoader.loadClass(name);
        return clazz;
    }

    public void addURL(URL u) throws IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].toString().equalsIgnoreCase(u.toString())) {
                return;
            }
        }
        Class sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", PARAMETERS);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{u});
        } catch (Throwable t) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(t));
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    protected boolean alreadyLoaded(Plugin p) {
        if (p == null) {
            return true; //an error occured, with true this plugin is skipped
        }
        Iterator it = ADDONS.iterator();
        while (it.hasNext()) {
            Plugin plugin = (Plugin) it.next();
            if (p.getName().equalsIgnoreCase(plugin.getName())) {
                return true;
            }
        }
        return false;
    }

    public static List<Plugin> getLoadedPlugins() {
        return ADDONS;
    }

    public static Client getPluginByName(String name) {
        Iterator it = ADDONS.iterator();
        while (it.hasNext()) {
            Plugin plugin = (Plugin) it.next();
            if (name.trim().equalsIgnoreCase(plugin.getName())) {
                return plugin;
            }
        }
        return null;
    }

    public static boolean installDevice(URL fromURL) {
        try {
            String url = fromURL.toString();
            String filename = url.substring(url.lastIndexOf('/') + 1);

            //get the zip from the url and copy in plugin/device folder
            if (filename.endsWith(".device")) {
                File zipFile = new File(Info.getPluginsPath() + "/devices/" + filename);
                FetchHttpFiles.download(fromURL, new File(Info.getPluginsPath() + "/devices"), filename);
                unzipAndDelete(zipFile);
            } else {
                if (filename.endsWith(".object")) {
                    FetchHttpFiles.download(fromURL, new File(Info.getPluginsPath() + "/objects"), filename);
                    File zipFile = new File(Info.getPluginsPath() + "/objects/" + filename);
                    unzipAndDelete(zipFile);
                } else {
                    Freedomotic.logger.warning("No installable Freedomotic plugins at URL " + fromURL);
                }
            }
        } catch (Exception ex) {
            return false; //not done
        }
        return true;
    }

    private static boolean unzipAndDelete(File zipFile) {
        Freedomotic.logger.info("Uncompressing plugin archive " + zipFile);
        try {
            Unzip.unzip(zipFile.toString());
        } catch (Exception e) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
            return false;
        }
        //remove zip file
        try {
            zipFile.delete();
        } catch (Exception e) {
            Freedomotic.logger.info("Unable to delete compressed file " + zipFile.toString());
        }
        return true; //done
    }

    private void copyAllFiles(File sfile, File dfile) {
        try {
            File[] files = sfile.listFiles();
            files = sfile.listFiles();
            //copy all files in local resources folder to the main resources folder under freedom_root/data/resources
            if (files != null) {
                for (File file : files) {
                    Freedomotic.logger.info("Copying file " + file.getAbsolutePath() + " to " + dfile.getAbsolutePath());
                    CopyFile.copy(file, new File(dfile + "/" + file.getName()));
                }
            } else {
                Freedomotic.logger.info("No file to copy in this folder");
            }
        } catch (Exception ex) {
            Freedomotic.logger.warning("Unable to copy this file " + ex.getMessage());
        }
    }
}
