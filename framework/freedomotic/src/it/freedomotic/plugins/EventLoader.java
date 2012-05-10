package it.freedomotic.plugins;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.util.JarFilter;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author Enrico
 */
public class EventLoader implements AddonLoaderInterface {

    @Override
    public void load(AddonManager manager, File path) {
        File dir = new File(path.getAbsolutePath());
        String SEPARATOR = "\n";
        if (dir.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = dir.listFiles(new JarFilter());
        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    Freedomotic.logger.info(SEPARATOR);
                    System.out.print("Searching for Events in " + jar.getName());
                    List<String> classNames = null;
                    try {
                        classNames = manager.getClassNames(jar.getAbsolutePath());
                        Freedomotic.logger.info("[" + classNames.size() + " classes]");
                        for (String className : classNames) {
                            String name = className.substring(0, className.length() - 6);
                            Class clazz = null;

                            clazz = manager.getClass(jar, name);
                            Class superclass = clazz.getSuperclass();
                            try {
                                clazz.newInstance();
                                Freedomotic.logger.info("Event " + clazz.getCanonicalName() + " loaded");
                            } catch (Exception exception) {
                                Freedomotic.logger.info("Exception raised while loading this event. Skip it.");
                                Freedomotic.logger.info("\n");
                                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(exception));
                            }
                        }
                    }catch (Exception ex) {
                        Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                    }
                }
            }
        }
    }
}
