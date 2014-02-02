/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.app;

import com.freedomotic.model.ds.Tuples;
import java.io.File;
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

    Set<Map.Entry<Object, Object>> entrySet();

    boolean getBooleanProperty(String key, boolean defaultValue);

    double getDoubleProperty(String key, double defaultValue);

    int getIntProperty(String key, int defaultValue);

    ArrayList<String> getPathListProperty(String key);

    Properties getProperties();

    /**
     * @param key
     * @return the value of key or null if not key found
     */
    String getProperty(String key);

    String getStringProperty(String key, String defaultValue);

    ArrayList<URL> getUrlListProperty(String key);

    void put(Object key, Object value);

    void setProperty(String key, String value);
    
    AppConfig load();
    void save();
    
}
