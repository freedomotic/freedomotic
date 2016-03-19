/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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

    /**
     *
     * @return
     */
    Set<Map.Entry<Object, Object>> propertiesSet();

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    boolean getBooleanProperty(String key, boolean defaultValue);

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    double getDoubleProperty(String key, double defaultValue);

    /**
     *
     * @param key
     * @param defaultValue
     * @return
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
     *
     * @param key
     * @param defaultValue
     * @return
     */
    String getStringProperty(String key, String defaultValue);

    /**
     *
     * @param key
     * @return
     */
    ArrayList<URL> getUrlListProperty(String key);

    /**
     *
     * @param key
     * @param value
     */
    void put(Object key, Object value);

    /**
     *
     * @param key
     * @param value
     */
    void setProperty(String key, String value);
    
    /**
     *
     * @return
     */
    AppConfig load();

    /**
     *
     */
    void save();
    
}
