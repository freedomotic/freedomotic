/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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
import java.util.logging.Logger;

/**
 *
 * @author enrico
 */
public final class Command implements Serializable, Cloneable {

    private static final long serialVersionUID = -7287958816826580426L;
    private static final Logger LOG = Logger.getLogger(Command.class.getName());
    public static final String PROPERTY_BEHAVIOR = "behavior";
    public static final String PROPERTY_OBJECT_CLASS = "object.class";
    public static final String PROPERTY_OBJECT_ADDRESS = "object.address";
    public static final String PROPERTY_OBJECT_NAME = "object.name";
    public static final String PROPERTY_OBJECT_PROTOCOL = "object.protocol";
    public static final String PROPERTY_OBJECT_INCLUDETAGS = "object.includetags";
    public static final String PROPERTY_OBJECT_EXCLUDETAGS = "object.excludetags";
    public static final String PROPERTY_OBJECT = "object";
    // Nome del comando
    private String name;
    // Coda alla quale inviare il comando
    private String receiver;
    private String uuid;
    // Ritardo dall'esecuzione di questo comando e il successivo
    private int delay;
    private int timeout;
    private String description;
    private String stopIf;
    private HashSet<String> tags;
    //by default a command is userLevel, this means that can be used in reactions.
    //Hardware level commands cannot be used in reactions but only linked to an object action
    private boolean hardwareLevel;
    private boolean editable;
    private boolean executed;
    private Config properties = new Config();

    public Command() {
        if (isHardwareLevel()) { //an hardware level command
            setEditable(false); //it has not to me stored in root/data folder
        }
    }

    public HashSet<String> getTags() {
        if (tags == null) {
            tags = new HashSet<String>();
            tags.addAll(Arrays.asList(getName().toLowerCase().split(" ")));
        }

        return tags;
    }

    public void setTags(HashSet<String> tags) {
        this.tags = tags;
    }

    public String getDescription() {
        if (description == null) {
            description = getName();
        }

        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public boolean isHardwareLevel() {
        return hardwareLevel;
    }

    public void setHardwareLevel(boolean hardwareLevel) {
        this.hardwareLevel = hardwareLevel;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean persistence) {
        this.editable = persistence;
    }

    public String getStopIf() {
        return stopIf;
    }

    public void setStopIf(String continueIf) {
        this.stopIf = continueIf;
    }

    public String getBehavior() {
        if (properties.getProperty("behavior") != null) {
            return properties.getProperty("behavior");
        } else {
            LOG.warning("Undefined property 'behavior' in command '" + this.getName() + "'");

            return "undefined-behavior";
        }
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public void setProperty(String key, String value) {
        if (!key.isEmpty()) {
            properties.setProperty(key, value);
        }
    }

    public Config getProperties() {
        return properties;
    }

    /**
     * Creates an oredred list reading the command properties writed in format
     * "parameter[AN_INT_FROM_0_TO_999]" other properties format are ignored
     * (not added to the returned List) The indexs must be contiguous
     * (1,2,3,...) for example:
     *
     * <p> <li>parameter[0] = foo<li> <li>parameter[1] = bar</li>
     * <li>parameter[3] = asd</li> <li>object = Light 1</li> <li>another-param =
     * myValue</li> </p>
     *
     * <p>The returned ArrayList<String> is <li>[0]->foo</li> <li>[1]->bar</li>
     * because the index = 2 is missing. </p>
     *
     * @return an ordered ArrayList<String> of command parameter values
     */
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

    public int getDelay() {
        if (delay > 0) {
            return delay;
        } else {
            return 0;
        }
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public void setExecuted(boolean value) {
        executed = value;
    }

    public boolean isExecuted() {
        return executed;
    }

    /**
     * Two commands are considered equals if they have the same name
     * @param obj
     * @return
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = (53 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    @Override
    public Command clone()
            throws CloneNotSupportedException {
        super.clone();

        Command clonedCmd = new Command();
        clonedCmd.setName(getName() + "[CLONED]");
        clonedCmd.setDescription(getDescription());
        clonedCmd.setReceiver(getReceiver());
        clonedCmd.setDelay(getDelay());
        clonedCmd.setReplyTimeout(getReplyTimeout());
        clonedCmd.setExecuted(executed);
        Iterator<Entry<Object, Object>> it = getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> e = it.next();
            clonedCmd.setProperty(e.getKey().toString(), e.getValue().toString()); //adding the original command properties to its clone
        }

        clonedCmd.properties.setXmlFile(this.getName());

        return clonedCmd;
    }

    public void destroy() {
        name = null;
        receiver = null;
        description = null;
        stopIf = null;
        properties.getProperties().clear();
        properties = null;
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getReplyTimeout() {
        return timeout;
    }

    public void setReplyTimeout(int timeout) {
        this.timeout = timeout;
    }
}
