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
//Copyright 2009 Enrico Nicoletti
//eMail: enrico.nicoletti84@gmail.com
//
//This file is part of EventEngine.
//
//EventEngine is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//any later version.
//
//EventEngine is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with EventEngine; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
package com.freedomotic.reactions;

import com.freedomotic.model.ds.Config;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;
import org.slf4j.LoggerFactory;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.slf4j.Logger;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public final class Command implements Serializable, Cloneable {

    private static final long serialVersionUID = -7287958816826580426L;
    private static final Logger LOG = LoggerFactory.getLogger(Command.class.getName());

    public static final String PROPERTY_BEHAVIOR = "behavior";
    public static final String PROPERTY_OBJECT_CLASS = "object.class";
    public static final String PROPERTY_OBJECT_ADDRESS = "object.address";
    public static final String PROPERTY_OBJECT_NAME = "object.name";
    public static final String PROPERTY_OBJECT_PROTOCOL = "object.protocol";
    public static final String PROPERTY_OBJECT_INCLUDETAGS = "object.includetags";
    public static final String PROPERTY_OBJECT_EXCLUDETAGS = "object.excludetags";
    public static final String PROPERTY_OBJECT_ENVIRONMENT = "object.environment";
    public static final String PROPERTY_OBJECT_ZONE = "object.zone";
    public static final String PROPERTY_OBJECT = "object";

    private String name;
    private String receiver;
    private String uuid;
    private int delay;
    private int timeout;
    private String description;
    private String stopIf;
    private HashSet<String> tags = new HashSet<String>();
    //by default a command is userLevel, this means that can be used in reactions.
    //Hardware level commands cannot be used in reactions but only linked to an object action
    private boolean hardwareLevel;
    private boolean editable;
    private boolean executed;
    @XmlElement(name = "props")
    private Config properties = new Config();
    private final String type = "command";

    /**
     *
     */
    public Command() {
        this.uuid = UUID.randomUUID().toString();
        this.properties.setProperty("type", type);
        if (isHardwareLevel()) { //an hardware level command
            setEditable(false); //it has not to be stored in root/data folder
        }
    }

    /**
     * Gets the tags associated to the command.
     *
     * @return a set of tags
     */
    public HashSet<String> getTags() {
        if (tags == null) {
            tags = new HashSet<String>();
            tags.addAll(Arrays.asList(getName().toLowerCase().split(" ")));
        }

        return tags;
    }

    /**
     * Sets the tags associated to the command.
     *
     * @param tags the tags to associate
     */
    public void setTags(HashSet<String> tags) {
        this.tags = tags;
    }

    /**
     * Returns a command description.
     *
     * @return the string description
     */
    public String getDescription() {
        if (description == null) {
            description = getName();
        }

        return description;
    }

    /**
     * Sets a command description.
     *
     * @param description the command description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the command uuid.
     *
     * @return the command uuid
     */
    public String getUuid() {
        if (uuid == null || "".equals(uuid.trim())) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    /**
     * Sets the command uuid.
     *
     * @param uuid the command uuid to set
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Checks it is a hardware level command. This type of command can't be used
     * in reactions but only linked to an object action
     *
     * @return true if it's a hardware level command, false otherwise
     */
    public boolean isHardwareLevel() {
        return hardwareLevel;
    }

    /**
     * Sets the command as hardware level one or not.
     *
     * @param hardwareLevel
     */
    public void setHardwareLevel(boolean hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }

    /**
     * Checks if the command is persisted.
     *
     * @return true if the command is persisted, false otherwise
     */
    public boolean isEditable() {
        return editable;
    }

    /**
     * Sets the command persistence.
     *
     * @param persistence
     */
    public void setEditable(boolean persistence) {
        this.editable = persistence;
    }

    /**
     *
     * @return
     */
    public String getStopIf() {
        return stopIf;
    }

    /**
     *
     * @param continueIf
     */
    public void setStopIf(String continueIf) {
        this.stopIf = continueIf;
    }

    /**
     * Gets a "behavior" property.
     *
     * @return the string property
     */
    @XmlTransient
    public String getBehavior() {
        if (properties.getProperty("behavior") != null) {
            return properties.getProperty("behavior");
        } else {
            LOG.warn("Undefined property 'behavior' in command '" + this.getName() + "'");
            return "undefined-behavior";
        }
    }

    /**
     * Gets the value of a property.
     *
     * @param key the property key
     * @return the value of the key or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    /**
     * Gets a boolean property with a fallback default value.
     *
     * @param key the string key
     * @param defaultValue the value to use if the given key does not exists
     * @return the property value or the default value if the key doesn't exists
     */
    public boolean getBooleanProperty(String key, boolean defaultValue) {
        String result = properties.getProperty(key);

        if (result != null) {
            if ("true".equalsIgnoreCase(result.trim())) {
                return true;
            } else {
                if ("false".equalsIgnoreCase(result.trim())) {
                    return false;
                }
            }
        }

        return defaultValue;
    }

    /**
     * Sets a property value.
     * 
     * @param key the string key (empty or null values not allowed)
     * @param value the value associated to the property (empty or null values not allowed)
     */
    public void setProperty(String key, String value) {
        if (key == null || key.isEmpty() || value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Cannot add empty or null properties "
                    + "[" + key + " = " + value + "] in command '" + this.getName() + "'");
        } else {
            properties.setProperty(key, value);
        }
    }

    /**
     * Gets the command properties.
     *  
     * @return the command properties
     */
    public Config getProperties() {
        return properties;
    }

    /**
     * Creates an ordered list reading the command properties written in the format
     * "parameter[AN_INT_FROM_0_TO_999]". Other properties formats are ignored
     * (not added to the returned List.) The indexes must be contiguous
     * (1,2,3,...). For example:
     *
     * <p>
     * <li>parameter[0] = foo<li> <li>parameter[1] = bar</li>
     * <li>parameter[3] = asd</li> <li>object = Light 1</li> <li>another-param =
     * myValue</li> </p>
     *
     * <p>
     * The returned ArrayList<String> is <li>[0]->foo</li> <li>[1]->bar</li>
     * because the index = 2 is missing. </p>
     *
     * @return an ordered ArrayList<String> of command parameter values
     */
    @XmlTransient
    public ArrayList<String> getParametersAsList() {
        ArrayList<String> output = new ArrayList<String>();

        //99 is the max num of elements in list
        for (int i = 0; i < 99; i++) {
            String value = null;
            value = properties.getProperties().getProperty("parameter[" + i + "]");

            if (value != null) {
                //add to array
                output.add(value);
            } else {
                //if the elements are not contiguous return
                return output;
            }
        }

        return output;
    }

    /**
     * Gets the command delay.
     * 
     * @return the command delay
     */
    public int getDelay() {
        if (delay > 0) {
            return delay;
        } else {
            return 0;
        }
    }

    /**
     * Sets the command delay.
     * 
     * @param delay the delay to set
     */
    public void setDelay(int delay) {
        this.delay = delay;
    }

    /**
     * Gets the command name.
     * 
     * @return the command name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the command name.
     * 
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name.trim();
    }

    /**
     * Gets the channel the command listens to.
     * 
     * @return the channel the command listens to
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * Sets the channel the command listens to.
     * 
     * @param receiver the channel to set
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * Sets the command as "executed".
     * 
     * @param value the "executed" value
     */
    public void setExecuted(boolean value) {
        executed = value;
    }

    /**
     * Gets the command "executed" state.
     * 
     * @return true if the command has been executed, false otherwise
     */
    public boolean isExecuted() {
        return executed;
    }

    /**
     * Two commands are considered equals if they have the same name.
     *
     * @param obj the object to compare
     * @return true it the two commands are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Command other = (Command) obj;

        if ((this.name == null) ? (other.name != null) : (!this.name.equals(other.name))) {
            return false;
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 5;
        hash = (53 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    /**
     * Creates a clone of the command.
     * 
     * @return a clone of the command
     * @throws CloneNotSupportedException
     */
    @Override
    public Command clone()
            throws CloneNotSupportedException {
        super.clone();

        Command clonedCmd = new Command();
        clonedCmd.setName(getName());
        clonedCmd.setDescription(getDescription());
        clonedCmd.setReceiver(getReceiver());
        clonedCmd.setDelay(getDelay());
        clonedCmd.setReplyTimeout(getReplyTimeout());
        clonedCmd.setExecuted(executed);
        clonedCmd.setHardwareLevel(hardwareLevel);
        Iterator<Entry<Object, Object>> it = getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> e = it.next();
            clonedCmd.setProperty(e.getKey().toString(), e.getValue().toString()); //adding the original command properties to its clone
        }

        clonedCmd.properties.setXmlFile(this.getName());

        return clonedCmd;
    }

    /**
     * Cleares all variables.
     * 
     */
    public void destroy() {
        name = null;
        receiver = null;
        description = null;
        stopIf = null;
        properties.getProperties().clear();
        properties = null;
    }

    /**
     * 
     * 
     * @return
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Gets the command reply timeout.
     * 
     * @return the command reply timeout
     */
    public int getReplyTimeout() {
        return timeout;
    }

    /**
     * Sets the command reply timeout.
     * 
     * @param timeout the timeout to set 
     */
    public void setReplyTimeout(int timeout) {
        this.timeout = timeout;
    }

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
}
