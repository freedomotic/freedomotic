/*Copyright 2009 Enrico Nicoletti
 eMail: enrico.nicoletti84@gmail.com

 This file is part of Freedomotic.

 Freedomotic is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 any later version.

 Freedomotic is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with EventEngine; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.model.ds;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Expression autor is undefined on line 12, column 14 in
 * Templates/Classes/Class.java.
 */
public class Config implements Serializable {

    private Properties properties = new Properties();
    private String xmlFile;
    private Tuples tuples = new Tuples();

    public String getXmlFile() {
        return xmlFile;
    }

    public void setXmlFile(File file) {
        this.xmlFile = file.getName();
    }

    public void setXmlFile(String name) {
        xmlFile = name;
    }

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

    public Tuples getTuples() {
        return tuples;
    }

    public Set<Entry<Object, Object>> entrySet() {
        return properties.entrySet();
    }

    public void put(Object key, Object value) {
        properties.put(key, value);
    }

    public String getStringProperty(String key, String defaultValue) {
        String result = properties.getProperty(key);
        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("'" + getXmlFile() + "'  does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

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

    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String result = properties.getProperty(key);
        if (result != null) {
            if (result.trim().equalsIgnoreCase("true")) {
                return true;
            } else {
                if (result.trim().equalsIgnoreCase("true")) {
                    return false;
                }
            }
        }
        return defaultValue;
    }

    public double getDoubleProperty(String key, double defaultValue) {
        Double result = Double.parseDouble(properties.getProperty(key));
        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("'" + getXmlFile() + "' does not contain property '" + key + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    public ArrayList<URL> getUrlListProperty(String key) {
        ArrayList<URL> list = new ArrayList<URL>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");
        while (t.hasMoreElements()) {
            String token = t.nextToken();
            try {
                list.add(new URL(token));
            } catch (MalformedURLException ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }

    public ArrayList<String> getPathListProperty(String key) {
        ArrayList<String> list = new ArrayList<String>();
        String s = getStringProperty(key, "");
        StringTokenizer t = new StringTokenizer(s, " ");
        while (t.hasMoreElements()) {
            String token = t.nextToken();
            try {
                list.add(token);
            } catch (Exception ex) {
                Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return list;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public String toString() {
        Set entries = entrySet();
        Iterator it = entries.iterator();
        StringBuilder string = new StringBuilder();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            string.append(key + "=" + value + "; ");
        }
        return string.toString();
    }
}
