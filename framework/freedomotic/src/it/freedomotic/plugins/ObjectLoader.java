package it.freedomotic.plugins;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.util.CopyFile;
import it.freedomotic.util.Info;
import it.freedomotic.util.JarFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class ObjectLoader implements AddonLoaderInterface {

    Logger log = Freedomotic.logger;

    @Override
    public void load(AddonManager manager, File path) {
        File pluginFolder = new File(path.getAbsolutePath());
        String SEPARATOR = "\n";
        if (pluginFolder.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = pluginFolder.listFiles(new JarFilter());
        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    Freedomotic.logger.info(SEPARATOR);
                    log.info("Searching for Objects in " + jar.getName());
                    List<String> classNames = null;
                    try {
                        classNames = manager.getClassNames(jar.getAbsolutePath());
                    } catch (IOException ex) {
                        Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                    }
                    log.info("[" + classNames.size() + " classes]");
                    for (String className : classNames) {
                        String name = className.substring(0, className.length() - 6);
                        Class clazz = null;
                        try {
                            clazz = manager.getClass(jar, name);
                        } catch (Exception ex) {
                            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                        }
                        Class superclass = clazz.getSuperclass();
                        if (superclass != null) { //null if class is Object
                            try {
                                //check if this is a subclass of EnvObjectLogic
                                clazz.asSubclass(EnvObjectLogic.class);
                                if (superclass.getName().startsWith("it.freedomotic.objects.")) {
                                    clazz.newInstance();
                                    //for every envobject class a placeholder is created
                                    File sample = new File(pluginFolder.getAbsolutePath() + "/data/examples/" + clazz.getSimpleName().toLowerCase());
                                    if (sample.exists()) {
                                        Freedomotic.clients.createObjectPlaceholder(clazz, pluginFolder);
                                    }
                                    log.info("Object " + clazz.getCanonicalName() + " loaded");
                                }
                            } catch (NoClassDefFoundError err) {
                                log.warning(clazz.getName() + " is not in Freedomotic objects hierarchy. Skipped.");
                            } catch (ClassCastException e) {
                                log.warning(clazz.getName() + " is not in Freedomotic objects hierarchy. Skipped.");
                            } catch (Exception exception) {
                                log.severe("Exception raised while loading this object. This is not a valid Freedomotic object. Skip it. ");
                                Freedomotic.logger.info("\n");
                                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(exception));
                            }
                        } else {
                            //null superclass --> the class is Object
                        }
                    }
                }
            }
        } else {
            log.warning("No object can be found in folder " + pluginFolder.getAbsolutePath());
        }
    }
}
