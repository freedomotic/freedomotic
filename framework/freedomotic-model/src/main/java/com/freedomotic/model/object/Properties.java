/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.model.object;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author gpt
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class Properties implements Serializable {

    private static final long serialVersionUID = 1L;
    @XmlElement
    private HashMap<String, String> propertyList;

    /**
     *
     */
    public Properties() {
        propertyList = new HashMap<>();
    }

    /**
     *
     * @param prop
     */
    public Properties(Map<String, String> prop) {
        propertyList = new HashMap<>(prop);
    }

    /**
     *
     * @return
     */
    public Set<String> stringPropertyNames() {
        return propertyList.keySet();
    }

    /**
     *
     * @param name
     * @return
     */
    public String getProperty(String name) {
        return propertyList.get(name);
    }

    /**
     *
     * @param name
     * @param value
     */
    public void setProperty(String name, String value) {
        propertyList.put(name, value);
    }

    /**
     *
     * @return
     */
    public Set<Entry<String, String>> entrySet() {
        return propertyList.entrySet();
    }

    /**
     *
     * @return
     */
    public int size() {
        return propertyList.size();
    }
}
