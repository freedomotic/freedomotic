/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package it.freedomotic.plugins;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.util.JarFilter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public final class ObjectLoader {

    static final Logger log = Freedomotic.logger;
    
    private ObjectLoader(){
        
    }

    public static void load(File path) {
        File pluginFolder = new File(path.getAbsolutePath());
        if (pluginFolder.isFile()) {
            return;
        }
        //the list of jars in the current folder
        File[] jarList = pluginFolder.listFiles(new JarFilter());
        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
                if (jar.isFile()) {
                    List<String> classNames = null;
                    try {
                        classNames = AddonLoader.getClassNames(jar.getAbsolutePath());
                    } catch (IOException ex) {
                        log.severe(Freedomotic.getStackTraceInfo(ex));
                    }
                    for (String className : classNames) {
                        String name = className.substring(0, className.length() - 6);
                        Class<?> clazz = null;
                        try {
                            clazz = AddonLoader.getClass(jar, name);
                        } catch (Exception ex) {
                            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                        }
                        Class<?> superclass = clazz.getSuperclass();
                        if (superclass != null) { //null if class is Object
                            try {
                                //check if this is a subclass of EnvObjectLogic
                                clazz.asSubclass(EnvObjectLogic.class);
                                if (superclass.getName().startsWith("it.freedomotic.objects.")) {
                                    clazz.newInstance();
                                }
                            } catch (NoClassDefFoundError err) {
                                log.warning(clazz.getName() + " is not in Freedomotic objects hierarchy. Skipped.");
                            } catch (ClassCastException e) {
                                //log.warning(clazz.getName() + " is not in Freedomotic objects hierarchy. Skipped.");
                            } catch (Exception exception) {
                                log.severe("Exception raised while loading this object. This is not a valid Freedomotic object. Skip it. ");
                                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(exception));
                            }
                        } else {
                            //null superclass --> the class is Object
                        }
                    }
                }
            }
            File templatesFolder = new File(pluginFolder.getAbsolutePath() + "/data/templates/");
            loadTemplates(templatesFolder);
        } else {
            log.warning("No object can be found in folder " + pluginFolder.getAbsolutePath());
        }
    }

    private static void loadTemplates(File templatesFolder) {
        //for every envobject class a placeholder is created
        File[] templates = templatesFolder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".xobj"));
            }
        });
        for (File sample : templates) {
            Freedomotic.clients.createObjectTemplate(sample);
        }
    }
}
