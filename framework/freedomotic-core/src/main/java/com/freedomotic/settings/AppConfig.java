/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * @author enrico
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
