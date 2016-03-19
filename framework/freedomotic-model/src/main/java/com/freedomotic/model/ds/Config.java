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
package com.freedomotic.model.ds;

import java.io.File;
import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class Config
        implements Serializable {

    private static final Logger LOG = LoggerFactory.getLogger(Config.class.getName());
    private static final long serialVersionUID = 1380975976029008480L;
    @XmlElement(name = "props")
    private final Properties properties = new Properties();
    private String xmlFile = "";
    private final Tuples tuples = new Tuples();

    /**
     *
     * @return
     */
    public String getXmlFile() {
        return xmlFile;
    }

    /**
     *
     * @param file
     */
    @XmlTransient
    public void setXmlFile(File file) {
        this.xmlFile = file.getName();
    }

    /**
     *
     * @param name
     */
    public void setXmlFile(String name) {
        xmlFile = name;
    }

    /**
     *
     * @param key
     * @param value
     */
    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
    }

    /**
     * @param key
     * @return the value of key or null if not key found
     */
    public String getProperty(String key) {
        String result = properties.getProperty(key);

        return result;
    }

    /**
     *
     * @return
     */
    public Tuples getTuples() {
        return tuples;
    }

    /**
     *
     * @return
     */
    public Set<Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }

    /**
     *
     * @param key
     * @param value
     */
    public void put(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public String getStringProperty(String key, String defaultValue) {
        String result = properties.getProperty(key);

        if (result != null) {
            return result;
        } else {
            //LOG.warn("'" + getXmlFile() + "'  does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public int getIntProperty(String key, int defaultValue) {
        try {
            Integer result = Integer.parseInt(properties.getProperty(key));

            if (result != null) {
                return result;
            } else {
                //LOG.warn("'" + getXmlFile() + "' does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            //LOG.info(e.getMessage());
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
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

    /**
     *
     * @param key
     * @param defaultValue
     * @return
     */
    public double getDoubleProperty(String key, double defaultValue) {
        Double result = Double.parseDouble(properties.getProperty(key));

        if (result != null) {
            return result;
        } else {
            //LOG.warn("'" + getXmlFile() + "' does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     *
     * @param key
     * @return
     */
    public ArrayList<URL> getUrlListProperty(String key) {
        ArrayList<URL> list = new ArrayList<URL>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");

        while (t.hasMoreElements()) {
            String token = t.nextToken();

            try {
                list.add(new URL(token));
            } catch (MalformedURLException ex) {
                LOG.error(ex.getLocalizedMessage());
            }
        }

        return list;
    }

    /**
     *
     * @param key
     * @return
     */
    public ArrayList<String> getPathListProperty(String key) {
        ArrayList<String> list = new ArrayList<String>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");

        while (t.hasMoreElements()) {
            String token = t.nextToken();

            try {
                list.add(token);
            } catch (Exception ex) {
                LOG.error(ex.getLocalizedMessage());
            }
        }

        return list;
    }

    /**
     *
     * @return
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        Set<Entry<Object, Object>> entries = entrySet();
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
}
