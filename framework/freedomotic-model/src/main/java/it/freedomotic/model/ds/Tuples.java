/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.ds;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A data structure to collect blocks of data in {@link HashMap} style
 * This are used for instance in plugin configuration file to define plugin level variables
 * @author Enrico
 */
public class Tuples implements Serializable {

    private static final long serialVersionUID = 3113993714552615957L;

    private ArrayList<HashMap<String, String>> tuples = new ArrayList<HashMap<String, String>>();

    public HashMap<String, String> getTuple(int i) {
        return tuples.get(i);
    }

    public String getProperty(int tupleIndex, String key) {
        return (String) tuples.get(tupleIndex).get(key);
    }

    public Iterator<Entry<String,String>> getPropertiesIterator(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet().iterator();
    }

    public int getPropertiesCount(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet().size();
    }

    public Set<Entry<String,String>> getProperties(int tupleIndex) {
        return tuples.get(tupleIndex).entrySet();
    }

    public void add(HashMap<String, String> map) {
        tuples.add(map);
    }

    public int size() {
        return tuples.size();
    }

    public String getStringProperty(int tupleIndex, String key, String defaultValue) {
        String result = getProperty(tupleIndex, key);
        if (result != null) {
            return result;
        } else {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " not found. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    public int getIntProperty(int tupleIndex, String key, int defaultValue) {
        try {
            Integer result = Integer.parseInt(getProperty(tupleIndex, key));
            if (result != null) {
                return result;
            } else {
                //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " not found. Using default value '" + defaultValue + "'");
                return defaultValue;
            }
        } catch (NumberFormatException e) {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " is not a valid integer. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

    public boolean getBooleanProperty(int tupleIndex, String key, boolean defaultValue) {
        try {
            Boolean result = Boolean.parseBoolean(getProperty(tupleIndex, key));
            if (result != null) {
                return result;
            } else {
                //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " not found. Using default value '" + defaultValue + "'");
                return defaultValue;
            }
        } catch (Exception e) {
            //Freedomotic.logger.warning("Property '" + key + "' in tuple " + tupleIndex + " is not a valid boolean value. Using default value '" + defaultValue + "'");
            return defaultValue;
        }
    }

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
}
