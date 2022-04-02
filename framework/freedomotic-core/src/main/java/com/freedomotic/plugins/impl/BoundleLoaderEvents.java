/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.api.Client;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.PluginLoadingException;
import com.freedomotic.util.JarFilter;

/**
 *
 * @author Enrico Nicoletti
 */
class BoundleLoaderEvents implements BoundleLoader {

    private static final Logger LOG = LoggerFactory.getLogger(BoundleLoaderEvents.class.getName());
    private File path;

    BoundleLoaderEvents(File path) {
        this.path = path;
    }

    @Override
    public List<Client> loadBoundle() throws PluginLoadingException {
        File dir = new File(path.getAbsolutePath());
        List<Client> results = new ArrayList<>();

        if (dir.isFile()) {
            //return an empty list
            return results;
        }

        //the list of jars in the current folder
        File[] jarList = dir.listFiles(new JarFilter());

        if (jarList != null) {
            //the list of files in the jar
            for (File jar : jarList) {
            	results.addAll(this.getClientsFromJar(jar));
            }
        }

        return results;
    }
    
    private List<Client> getClientsFromJar(File jar) {
    	List<Client> clients = new ArrayList<>();
    	if (jar.isFile()) {
            List<String> classNames = null;
            try {
                classNames = BoundleLoaderFactory.getClassesInside(jar.getAbsolutePath());
                for (String className : classNames) {
                    String name = className.substring(0, className.length() - 6);
                    Class<?> clazz = BoundleLoaderFactory.getClass(jar, name); 
                    Client client = this.loadClient(clazz);
                    if(client!=null)
                    	clients.add(client);
                }
            } catch (Exception ex) {
                LOG.error(Freedomotic.getStackTraceInfo(ex));
            }
        } 	
    	return clients;
    }
    
    
    private Client loadClient(Class<?> clazz) {
  	  try {
            return (Client) clazz.newInstance(); //later it gets injected by guice
        } catch (Exception ex) {
        	LOG.info("Exception raised while loading this event. Skip it.");
            LOG.error(Freedomotic.getStackTraceInfo(ex));
           return null;
        }
  }

    @Override
    public File getPath() {
        return path;
    }
}
