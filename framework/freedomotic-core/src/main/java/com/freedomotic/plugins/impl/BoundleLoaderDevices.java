/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.freedomotic.api.Client;
import com.freedomotic.api.Plugin;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.util.JarFilter;

/**
 *
 * @author Enrico Nicoletti
 */
class BoundleLoaderDevices implements BoundleLoader {

    private File path;

    BoundleLoaderDevices(File path) {
        this.path = path;
    }

    @Override
    public List<Client> loadBoundle() throws PluginLoadingException {
        File pluginRootFolder = new File(path.getAbsolutePath());
        List<Client> results = new ArrayList<>();

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
                        if (this.isParentClassnameAdmittable(clazz.getSuperclass())) {
                        	results.add(this.loadPlugin(clazz));
                        }
                    }
               
                } catch (Exception ex) {
                    throw new PluginLoadingException("Generic error while loading boundle " + pluginJar.getAbsolutePath(), ex);
                }
            }
        }
        
        return results;
    }

    private boolean isParentClassnameAdmittable(Class<?> superclass) {
    	if(superclass==null)
    		return false;
    	
    	String classname = superclass.getName();
  
		return classname.equals("com.freedomotic.api.Actuator")
        || classname.equals("com.freedomotic.api.Sensor")
        || classname.equals("com.freedomotic.api.Protocol")
        || classname.equals("com.freedomotic.api.Intelligence")
        || classname.equals("com.freedomotic.api.Tool");
    }
    
    private Plugin loadPlugin(Class<?> clazz) throws PluginLoadingException {
    	  try {
              return (Plugin) clazz.newInstance(); //later it gets injected by guice
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
    
    @Override
    public File getPath() {
        return path;
    }
    
    
}
