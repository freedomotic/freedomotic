package it.freedomotic.plugins;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.JarFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public final class DevicesLoader {

    private static final Logger log = Freedomotic.logger;
    
    private DevicesLoader(){
        
    }

    public static void load(File path) {
        File pluginRootFolder = new File(path.getAbsolutePath());
        if (pluginRootFolder.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = pluginRootFolder.listFiles(new JarFilter());
        //the list of files in the jar
        for (File jar : jarList) {
            if (jar.isFile()) {
                List<String> classNames = null;
                try {
                    classNames = AddonLoader.getClassNames(jar.getAbsolutePath());
                } catch (IOException ex) {
                    log.severe(ex.getLocalizedMessage());
                }
                for (String className : classNames) {
                    String name = className.substring(0, className.length() - 6);
                    Class clazz = null;
                    try {
                        clazz = AddonLoader.getClass(jar, name);
                    } catch (Exception ex) {
                        Logger.getLogger(DevicesLoader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    Class superclass = clazz.getSuperclass();
                    if (superclass != null) { //null if class is Object
                        try {
                            //we allow the dynamic loading only to ADDONS of this classes
                            if ((superclass.getName().equals("it.freedomotic.api.Actuator"))
                                    || (superclass.getName().equals("it.freedomotic.api.Sensor"))
                                    || (superclass.getName().equals("it.freedomotic.api.Protocol"))
                                    || (superclass.getName().equals("it.freedomotic.api.Intelligence"))
                                    || (superclass.getName().equals("it.freedomotic.api.Tool"))) {
                                //creatae a new instance of the loaded class. It leads to the initialization using manifest xml as basis
                                Plugin plugin = null;
                                try {
                                    if (Plugin.isCompatible(path)) {
                                        plugin = (Plugin) clazz.newInstance();
                                        mergePackageConfiguration(plugin, path);
                                        if (!ClientStorage.isLoaded(plugin)) {
                                            log.config(plugin.getName() + " added to plugins list.");
                                            Freedomotic.clients.enqueue(plugin);
                                        } else {
                                            log.warning("This plugin is already loaded or not valid. Skip it.");
                                            plugin = null; //discard this entry
                                            Freedomotic.clients.createPlaceholder(clazz.getSimpleName(), "Plugin", null);
                                        }
                                    } else {
                                        Freedomotic.logger.severe("Plugin in " + path.getAbsolutePath()
                                                + " is not compatible with this framework version.");
                                        plugin = Freedomotic.clients.createPlaceholder(clazz.getSimpleName(), "Plugin",
                                                "Not compatible with this framework version");
                                    }
                                } catch (NoClassDefFoundError noClassDefFoundError) {
                                    Freedomotic.logger.severe("This plugin miss a library neccessary to work correctly or calls a method that no longer exists.\n\n"
                                            + Freedomotic.getStackTraceInfo(noClassDefFoundError));
                                } catch (Exception e) {
                                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
                                }
                            } else {
                                //Is not a valid plugin class. Skip it
                            }
                        } catch (Exception exception) {
                            log.warning("Exception raised while loading this plugin. This plugin is not loaded.");
                            log.warning(Freedomotic.getStackTraceInfo(exception));
                            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(exception));
                        }
                    } else {
                        //null superclass --> the class is Object, cannot be loaded as plugin
                    }
                }
            }
        }
    }

    public static void mergePackageConfiguration(Plugin plugin, File pluginFolder) {
        //seach for a file called PACKAGE
        Properties pack = new Properties();
        try {
            pack.load(new FileInputStream(new File(pluginFolder + "/PACKAGE")));
            //merges data found in file PACKGE to the the configuration of every single plugin in this package
            plugin.getConfiguration().setProperty("package.name", pack.getProperty("package.name"));
            plugin.getConfiguration().setProperty("package.nodeid", pack.getProperty("package.nodeid"));
            plugin.getConfiguration().setProperty("package.version",
                    pack.getProperty("build.major") + "."
                    + pack.getProperty("build.number"));
            plugin.getConfiguration().setProperty("framework.required.version",
                    pack.getProperty("framework.required.major") + "."
                    + pack.getProperty("framework.required.minor") + "."
                    + pack.getProperty("framework.required.build"));
            //TODO: add also the other properties

        } catch (IOException ex) {
            Freedomotic.logger.severe("Folder " + pluginFolder + " doesen't contains a PACKAGE file. This plugin is not loaded.");
        }
    }
}
