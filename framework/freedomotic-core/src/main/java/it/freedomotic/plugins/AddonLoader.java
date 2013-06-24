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

import it.freedomotic.app.Freedomotic;
import it.freedomotic.reactions.CommandPersistence;
import it.freedomotic.reactions.ReactionPersistence;
import it.freedomotic.reactions.TriggerPersistence;
import it.freedomotic.util.FetchHttpFiles;
import it.freedomotic.util.Info;
import it.freedomotic.util.Unzip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public final class AddonLoader {

    // Parameters
    private static final Class<?>[] PARAMETERS = new Class<?>[]{URL.class};

    private AddonLoader() {
    }

    public static void load(File directory) throws Exception {
        //intantiate the right loader based on the directory passed to the searchIn method
        String devicesPath = new File(Info.getPluginsPath() + "/devices/").toString();
        int numLoadedPlugins = 0;
        if (directory.toString().startsWith(devicesPath)) {
            DevicesLoader.load(directory);
            numLoadedPlugins++;
        }
        String objectsPath = new File(Info.getPluginsPath() + "/objects/").toString();
        if (directory.toString().startsWith(objectsPath)) {
            ObjectLoader.load(directory);
            numLoadedPlugins++;
        }
        String eventsPath = new File(Info.getPluginsPath() + "/events/").toString();
        if (directory.toString().startsWith(eventsPath)) {
            EventLoader.load(directory);
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
            recursiveCopy(new File(directory + "/data/resources"), destination);
        }
    }

    public static void recursiveSearchIn(File directory) {
        if (directory.isDirectory()) {
            //search in subfolder. Go down a level starting from /plugins/. Is not real recursive
            for (File subfolder : directory.listFiles()) {
                if (subfolder.isDirectory()) {
                    try {
                        load(subfolder);
                    } catch (Exception ex) {
                        Logger.getLogger(AddonLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
    }

    protected static List<String> getClassNames(String jarName) throws IOException {
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
        jarFile.close();
        return classes;
    }

    protected static Class<?> getClass(File file, String name) throws Exception {
        addURL(file.toURL());
        URLClassLoader clazzLoader;
        Class<?> clazz;
        String filePath = file.getAbsolutePath();
        filePath = "jar:file://" + filePath + "!/";
        URL url = new File(filePath).toURL();
        clazzLoader = new URLClassLoader(new URL[]{url});
        clazz = clazzLoader.loadClass(name);
        return clazz;
    }

    private static void addURL(URL u) throws IOException {
        URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        URL urls[] = sysLoader.getURLs();
        for (int i = 0; i < urls.length; i++) {
            if (urls[i].toString().equalsIgnoreCase(u.toString())) {
                return;
            }
        }
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        try {
            Method method = sysclass.getDeclaredMethod("addURL", PARAMETERS);
            method.setAccessible(true);
            method.invoke(sysLoader, new Object[]{u});
        } catch (Throwable t) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(t));
            throw new IOException("Error, could not add URL to system classloader");
        }
    }

    public static boolean installDevice(URL fromURL) {
        try {
            String url = fromURL.toString();
            if (url.lastIndexOf("&")>-1){
                //remove any parameter (starts with '&' char) at the end of file name
                url = url.substring(0, url.lastIndexOf('&'));
            }
            String filename = url.substring(url.lastIndexOf('/') + 1);
            System.out.println("DEBUG: file name: " + filename);
            //extracts plugin name from zip file name
            String pluginName = filename.substring(0, filename.indexOf("-"));
            System.out.println("DEBUG: plugin name: " + pluginName);
            //get the zip from the url and copy in plugin/device folder
            if (filename.endsWith(".device")) {
                File zipFile = new File(Info.getPluginsPath() + "/devices/" + filename);
                FetchHttpFiles.download(fromURL, new File(Info.getPluginsPath() + "/devices"), filename);
                System.out.println(zipFile);
                unzipAndDelete(zipFile);
                load(new File(Info.PATH_DEVICES_FOLDER + "/" + pluginName));
            } else {
                if (filename.endsWith(".object")) {
                    FetchHttpFiles.download(fromURL, new File(Info.getPluginsPath() + "/objects"), filename);
                    File zipFile = new File(Info.getPluginsPath() + "/objects/" + filename);
                    unzipAndDelete(zipFile);
                    load(new File(Info.PATH_OBJECTS_FOLDER + "/" + pluginName));
                } else {
                    Freedomotic.logger.warning("No installable Freedomotic plugins at URL " + fromURL);
                }
            }
        } catch (Exception ex) {
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
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

    private static void recursiveCopy(File source, File target) {
        InputStream input = null;
        OutputStream output = null;
        try {
            if (source.isDirectory()) {
                if (!target.exists()) {
                    target.mkdir();
                }

                String[] children = source.list();
                for (int i = 0; i < children.length; i++) {
                    recursiveCopy(new File(source, children[i]),
                            new File(target, children[i]));
                }
            } else {
                input = new FileInputStream(source);
                output = new FileOutputStream(target);
                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = input.read(buf)) > 0) {
                    output.write(buf, 0, len);
                }
            }
        } catch (FileNotFoundException foundEx) {
            Freedomotic.logger.config("No file to copy in " + source);
        } catch (IOException ex) {
            Logger.getLogger(AddonLoader.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {  //second catch block
            }
        }
    }
}
