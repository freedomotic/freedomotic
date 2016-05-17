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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A data structure to collect blocks of data in {@link HashMap} style. Used,
 * for instance, in plugin configuration file to define its level variables
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Tuples
        implements Serializable {

    private static final long serialVersionUID = 3113993714552615957L;
    private final ArrayList<HashMap<String, String>> tuples = new ArrayList<HashMap<String, String>>();

    /**
     * Returns a tuple given its index.
     *
     * @param i index of the tuple
     * @return the tuple retrieved
     */
    public HashMap<String, String> getTuple(int i) {
        return tuples.get(i);
    }

    /**
     * Returns a property value inside a tuple.
     *
     * @param tupleIndex index of the tuple
     * @param key the key name
     * @return the value of the property
     */
    public String getProperty(int tupleIndex, String key) {
        return (String) tuples.get(tupleIndex).get(key);
    }

    /**
     * Returns an iterator for the properties of a tuple.
     *
     * @param tupleIndex index of the tuple
     * @return the iterator for the tuple properties
     */
    public Iterator<Entry<String, String>> getPropertiesIterator(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet().iterator();
    }

    /**
     * Returns the number of properties inside a tuple.
     *
     * @param tupleIndex index of the tuple
     * @return the number of properties
     */
    public int getPropertiesCount(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet().size();
    }

    /**
     * Returns the properties inside a tuple.
     *
     * @param tupleIndex index of the tuple
     * @return the set of properties
     */
    public Set<Entry<String, String>> getProperties(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet();
    }

    /**
     *
     * @param map
     */
    public void add(HashMap<String, String> map) {
        tuples.add(map);
    }

    /**
     * Returns the number of tuples.
     *
     * @return the number of tuples
     */
    public int size() {
        return tuples.size();
    }

    /**
     * Returns a string property value.
     *
     * @param tupleIndex index of the tuple
     * @param key the property name to retrieve
     * @param defaultValue the property default value
     * @return the property value if not null, otherwise the default value
     * passed as param
     */
    public String getStringProperty(int tupleIndex, String key, String defaultValue) {
        String result = getProperty(tupleIndex, key);

        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " not found. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     * Returns an int property value.
     *
     * @param tupleIndex index of the tuple
     * @param key the property name to retrieve
     * @param defaultValue the property default value
     * @return the property value if not null, otherwise the default value
     * passed as param
     */
    public int getIntProperty(int tupleIndex, String key, int defaultValue) {
        try {
            Integer result = Integer.parseInt(getProperty(tupleIndex, key));
            return result;
        } catch (NumberFormatException e) {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " is not a valid integer. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     * Returns a boolean property value.
     *
     * @param tupleIndex index of the tuple
     * @param key the property name to retrieve
     * @param defaultValue the property default value
     * @return the property value if not null, otherwise the default value
     * passed as param
     */
    public boolean getBooleanProperty(int tupleIndex, String key, boolean defaultValue) {
        try {
            Boolean result = Boolean.parseBoolean(getProperty(tupleIndex, key));
            return result;
        } catch (Exception e) {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " is not a valid boolean value. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     * Returns a double property value.
     *
     * @param tupleIndex index of the tuple
     * @param key the property name to retrieve
     * @param defaultValue the property default value
     * @return the property value if not null, otherwise the default value
     * passed as param
     */
    public double getDoubleProperty(int tupleIndex, String key, double defaultValue) {
        Double result = null;

        try {
            String value = getProperty(tupleIndex, key);

            if (value != null) {
                result = Double.parseDouble(value);
            }

            if (result != null) {
                return result;
            } else {
                //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " not found. Using default value '" + defaultValue + "'");
                return defaultValue;
            }
        } catch (NumberFormatException numberFormatException) {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " is not a valid double value. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    /**
     * Deletes all tuples.
     */
    public void clear() {
        tuples.clear();

    }

    /**
     * Removes a tuple given its index.
     *
     * @param i index of the tuple
     * @return true if the tuple removed, false otherwise
     */
    public boolean remove(int i) {
        return (tuples.remove(i) != null);
    }
}
