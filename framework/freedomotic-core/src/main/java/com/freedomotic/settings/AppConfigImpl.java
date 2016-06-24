/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.settings;

import com.freedomotic.settings.Info;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class AppConfigImpl implements AppConfig {

    private static final Logger LOG = LoggerFactory.getLogger(AppConfigImpl.class.getName());
    private static final long serialVersionUID = 1380975976029008480L;
    private final Properties properties = new Properties();

    public AppConfigImpl() {
        // Get configuration from filesystem
        load();
    }

    @Override
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

  
    @Override
    public String getProperty(String key) {
        String result = properties.getProperty(key);
        return result;
    }

    @Override
    public Set<Entry<Object, Object>> propertiesSet() {
        return properties.entrySet();
    }

    @Override
    public void put(Object key, Object value) {
        properties.put(key, value);
    }

    @Override
    public String getStringProperty(String key, String defaultValue) {
        String result = properties.getProperty(key);

        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("'" + getXmlFile() + "'  does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    @Override
    public int getIntProperty(String key, int defaultValue) {
        try {
            Integer result = Integer.parseInt(properties.getProperty(key));

            if (result != null) {
                return result;
            } else {
                //Freedomotic.logger.warning("'" + getXmlFile() + "' does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            //Freedomotic.logger.info(e.getMessage());
            return defaultValue;
        }
    }

    @Override
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String result = properties.getProperty(key);

        if (result != null) {
            if (result.trim().equalsIgnoreCase("true")) {
                return true;
            } else {
                if (result.trim().equalsIgnoreCase("false")) {
                    return false;
                }
            }
        }

        return defaultValue;
    }

    @Override
    public double getDoubleProperty(String key, double defaultValue) {
        Double result = Double.parseDouble(properties.getProperty(key));

        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("'" + getXmlFile() + "' does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    @Override
    public ArrayList<URL> getUrlListProperty(String key) {
        ArrayList<URL> list = new ArrayList<URL>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");

        while (t.hasMoreElements()) {
            String token = t.nextToken();

            try {
                list.add(new URL(token));
            } catch (MalformedURLException ex) {
                LOG.error(ex.getMessage());
            }
        }

        return list;
    }

    @Override
    public ArrayList<String> getPathListProperty(String key) {
        ArrayList<String> list = new ArrayList<String>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");

        while (t.hasMoreElements()) {
            String token = t.nextToken();

            try {
                list.add(token);
            } catch (Exception ex) {
                LOG.error(ex.getMessage());
            }
        }

        return list;
    }

    @Override
    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        Set<Entry<Object, Object>> entries = propertiesSet();
        Iterator<Entry<Object, Object>> it = entries.iterator();
        StringBuilder string = new StringBuilder();

        while (it.hasNext()) {
            Entry<Object, Object> entry = it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            string.append(key + "=" + value + "; ");
        }

        return string.toString();
    }

    @Override
    public final AppConfig load() {
        try {
            properties.load(new FileInputStream(Info.PATHS.PATH_CONFIG_FOLDER + "/config.xml"));
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
        return this;
    }

    @Override
    public void save() {
        try {
            properties.store(new FileOutputStream(Info.PATHS.PATH_CONFIG_FOLDER + "/config.xml"), null);
        } catch (IOException ex) {
            LOG.error(ex.getMessage());
        }
    }
}
