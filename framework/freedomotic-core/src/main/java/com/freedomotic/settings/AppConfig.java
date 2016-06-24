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

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author Enrico Nicoletti
 */
public interface AppConfig extends Serializable {

    Set<Map.Entry<Object, Object>> propertiesSet();

    /**
     * Gets a boolean property value.
     * 
     * @param key property key to retrieve value from
     * @param defaultValue default 
     * @return the key value or the dafault value if the key doesn't exist
     */
    boolean getBooleanProperty(String key, boolean defaultValue);

    /**
     * Gets a double property value.
     * 
     * @param key property key to retrieve value from
     * @param defaultValue default 
     * @return the key value or the dafault value if the key doesn't exist
     */
    double getDoubleProperty(String key, double defaultValue);

   /**
     * Gets an integer property value.
     * 
     * @param key property key to retrieve value from
     * @param defaultValue default 
     * @return the key value or the dafault value if the key doesn't exist
     */
    int getIntProperty(String key, int defaultValue);

    /**
     *
     * @param key
     * @return
     */
    ArrayList<String> getPathListProperty(String key);

    /**
     *
     * @return
     */
    Properties getProperties();

    /**
     * @param key
     * @return the value of key or null if not key found
     */
    String getProperty(String key);

    /**
     * Gets a string property value.
     * 
     * @param key property key to retrieve value from
     * @param defaultValue default 
     * @return the key value or the dafault value if the key doesn't exist
     */
    String getStringProperty(String key, String defaultValue);

    /**
     *
     * @param key
     * @return
     */
    ArrayList<URL> getUrlListProperty(String key);

    /**
     * Puts a property. 
     * 
     * @param key property to add
     * @param value property value 
     */
    void put(Object key, Object value);

    /**
     * Sets a property value.
     * 
     * @param key property to set value
     * @param value value to set
     */
    void setProperty(String key, String value);

    /**
     * Loads properties from configuration file.
     * 
     */
    AppConfig load();

    /**
     * Saves properties to configuration file.
     *
     */
    void save();

}
