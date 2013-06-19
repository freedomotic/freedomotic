package it.freedomotic.plugins;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.util.JarFilter;

import java.io.File;
import java.util.List;

/**
 *
 * @author Enrico
 */
public final class EventLoader {
    
    private EventLoader(){
        
    }

    public static void load(File path) {
        File dir = new File(path.getAbsolutePath());
        if (dir.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = dir.listFiles(new JarFilter());
        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    List<String> classNames = null;
                    try {
                        classNames = AddonLoader.getClassNames(jar.getAbsolutePath());
                        for (String className : classNames) {
                            String name = className.substring(0, className.length() - 6);
                            Class clazz = null;

                            clazz = AddonLoader.getClass(jar, name);
                            Class superclass = clazz.getSuperclass();
                            try {
                                clazz.newInstance();
                            } catch (Exception exception) {
                                Freedomotic.logger.info("Exception raised while loading this event. Skip it.");
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
