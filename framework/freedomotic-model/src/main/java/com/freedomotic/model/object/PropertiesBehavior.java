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
package com.freedomotic.model.object;

import java.util.Set;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class PropertiesBehavior
        extends Behavior {

    private static final long serialVersionUID = 6808299643143488536L;

    private Properties properties = new Properties();

    /**
     *
     * @param key
     * @return
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
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
     *
     * @return
     */
    public Set<String> getPropertiesName() {
        return properties.stringPropertyNames();
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        if (properties.size() < 2) {
            return properties.size() + " record";
        } else {
            return properties.size() + " records";
        }
    }
}
