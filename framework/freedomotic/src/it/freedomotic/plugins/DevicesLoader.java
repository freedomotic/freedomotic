package it.freedomotic.plugins;

import it.freedomotic.api.Plugin;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.util.JarFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class DevicesLoader implements AddonLoaderInterface {

    private Logger log = Freedomotic.logger;
    private boolean dataFolderAlreadyLoaded = false;

    @Override
    public void load(AddonLoader manager, File path) {
        File pluginRootFolder = new File(path.getAbsolutePath());
        String SEPARATOR = "\n";
        if (pluginRootFolder.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = pluginRootFolder.listFiles(new JarFilter());
        //the list of files in the jar
        for (File jar : jarList) {
            if (jar.isFile()) {
                Freedomotic.logger.info(SEPARATOR);
                log.info("Searching for Plugins in " + jar.getName());
                List<String> classNames = null;
                try {
                    classNames = manager.getClassNames(jar.getAbsolutePath());
                } catch (IOException ex) {
                    log.severe(ex.getLocalizedMessage());
                }
                Freedomotic.logger.info("[" + classNames.size() + " classes]");
                for (String className : classNames) {
                    String name = className.substring(0, className.length() - 6);
                    Class clazz = null;
                    try {
                        clazz = manager.getClass(jar, name);
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
                                log.info("---- " + clazz.getSimpleName() + " ----");
                                try {
                                    if (Plugin.isCompatible(path)) {
                                        plugin = (Plugin) clazz.newInstance();
                                        Plugin.mergePackageConfiguration(plugin, path);
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
                                if (!ClientStorage.alreadyLoaded(plugin)) {
                                    log.info(plugin.getName() + " added to plugins list.");
                                    Freedomotic.logger.info("\n");
                                    Freedomotic.clients.enqueue(plugin);
                                } else {
                                    log.warning("This plugin is already loaded or not valid. Skip it.");
                                    Freedomotic.logger.info("\n");
                                    plugin = null; //discard this entry
                                    Freedomotic.clients.createPlaceholder(clazz.getSimpleName(), "Plugin", null);
                                }
                            } else {
                                //Is not a valid plugin. Skip it
                            }
                        } catch (Exception exception) {
                            log.warning("Exception raised while loading this plugin. This plugin is not loaded.");
                            log.warning(Freedomotic.getStackTraceInfo(exception));
                            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(exception));
                            Freedomotic.logger.info("\n");
                        }
                    } else {
                        //null superclass --> the class is Object, cannot be loaded as plugin
                    }
                }
            }
        }
    }
}
