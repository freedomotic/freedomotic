/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

import com.freedomotic.model.geometry.FreedomShape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"name", "description", "uuid", "protocol", "phisicalAddress", "type", "actAs",
    "representation", "currentRepresentation", "behaviors", "hierarchy", "actions", "triggers", "tags", "envUUID"})
public class EnvObject implements Serializable {

    private static final long serialVersionUID = -7253889516478184321L;
    private static final String UNKNOWN_VALUE = "unknown";

    private String name;
    private String description;
    private String actAs;
    private String type;
    private String uuid;
    private String hierarchy;
    private String protocol;
    private String phisicalAddress;
    private final List<Behavior> behaviors = new ArrayList<>();
    private final List<Representation> representation = new ArrayList<>();
    private Set<String> tags;
    private final Properties actions = new Properties();
    private Properties triggers = new Properties();
    private int currentRepresentation;
    private String envUUID;

    /**
     *
     * @return uuid
     */
    public String getEnvironmentID() {
        return this.envUUID;
    }

    /**
     *
     * @param uuid
     */
    public void setEnvironmentID(String uuid) {
        this.envUUID = uuid;
    }

    /**
     *
     * @return actions
     */
    public Properties getActions() {
        return actions;
    }

    /**
     *
     * @return triggers
     */
    public Properties getTriggers() {
        if (triggers == null) {
            triggers = new Properties();
        }

        return triggers;
    }

    /**
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    public String getName() {
        return this.name;
    }

    /**
     *
     * @return
     */
    public String getUUID() {
        return uuid;
    }

    /**
     *
     * @param uuid
     */
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    /**
     *
     * @return
     */
    public String getHierarchy() {
        return hierarchy;
    }

    /**
     *
     * @param hierarchy
     */
    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    /**
     *
     * @param index
     */
    public void setCurrentRepresentation(int index) {
        if (representation.get(index) != null) {
            currentRepresentation = index;
        }
    }

    /**
     *
     * @return
     */
    public Representation getCurrentRepresentation() {
        return representation.get(currentRepresentation);
    }

    /**
     *
     * @return
     */
    public int getCurrentRepresentationIndex() {
        return currentRepresentation;
    }

    /**
     *
     * @return
     */
    public List<Representation> getRepresentations() {
        return representation;
    }

    /**
     *
     * @return
     */
    public String getProtocol() {
        if ((protocol == null) || (protocol.isEmpty())) {
            protocol = UNKNOWN_VALUE;
        }

        return protocol;
    }

    /**
     *
     * @param protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     *
     * @return
     */
    public List<Behavior> getActiveBehaviors() {
        ArrayList<Behavior> activeBehaviors = new ArrayList<>();
        for (Behavior behavior : behaviors) {
            if (behavior.isActive()) {
                activeBehaviors.add(behavior);
            }
        }

        return activeBehaviors;
    }

    /**
     *
     * @return
     */
    public List<Behavior> getBehaviors() {
        return behaviors;
    }

    /**
     *
     * @param behavior
     * @return
     */
    public Behavior getBehavior(String behavior) {
        for (Behavior b : behaviors) {
            if (b.getName().equalsIgnoreCase(behavior)) {
                return b;
            }
        }
        return null; //this behaviors doesn't exists for this object
    }

    /**
     *
     * @param actAs
     */
    public void setActAs(String actAs) {
        this.actAs = actAs;
    }

    /**
     *
     * @return
     */
    public String getActAs() {
        return this.actAs;
    }

    /**
     *
     * @return
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param desc
     */
    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     *
     * @param type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     *
     * @return
     */
    public String getType() {
        return this.type;
    }

    /**
     *
     * @return
     */
    public String getPhisicalAddress() {
        if ((phisicalAddress == null) || (phisicalAddress.isEmpty())) {
            phisicalAddress = UNKNOWN_VALUE;
        }

        return phisicalAddress.trim();
    }

    /**
     *
     * @param address
     */
    public void setPhisicalAddress(String address) {
        phisicalAddress = address;
    }

    /**
     *
     * @return
     */
    public FreedomShape getShape() {
        return getCurrentRepresentation().getShape();
    }

    /**
     * Create an HashMap with all object properties useful in an event. In
     * EnvObjectLogic this method is used to get basic exposed data on which are
     * added behaviors related data.
     *
     * @return a set of key/values of object properties
     */
    public Map<String, String> getExposedProperties() {
        HashMap<String, String> result = new HashMap<>();
        result.put("object.name", getName());
        result.put("object.address", getPhisicalAddress());
        result.put("object.protocol", getProtocol());
        result.put("object.type", getType());
        result.put("object.tags", getTagsString());
        result.put("object.uuid", getUUID());
        return result;
    }

    /**
     * Returns only the last part of the type
     *
     * @return
     */
    public String getSimpleType() {
        //get the part of the string after the last dot characher
        //eg: 'EnvObject.ElectricDevice.Light' -> returns 'light'
        return getType().substring(getType().lastIndexOf('.') + 1).trim().toLowerCase();
    }

    /**
     *
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

        final EnvObject other = (EnvObject) obj;

        if (!this.name.equalsIgnoreCase(other.name)) {
            //if they have different names they cannot have same address/protocol
            //otherwise are the same object despite of the different name
            if ((this.getPhisicalAddress().equalsIgnoreCase(other.getPhisicalAddress()))
                    && (this.getProtocol().equalsIgnoreCase(other.getProtocol()))) {
                return (!this.getPhisicalAddress().equalsIgnoreCase(UNKNOWN_VALUE))
                        && (!this.getProtocol().equalsIgnoreCase(UNKNOWN_VALUE));
            } else {
                return false;
            }
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
        hash = (89 * hash) + ((this.name != null) ? this.name.hashCode() : 0);

        return hash;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     *
     * @return
     */
    public Set<String> getTagsList() {
        return this.tags;
    }

    /**
     *
     * @return
     */
    public String getTagsString() {
        StringBuilder tagString = new StringBuilder();
        Boolean morethanone = false;
        for (String tag : getTagsList()) {
            if (!tag.equals("")) {
                if (morethanone) {
                    tagString.append(",");
                }
                tagString.append(tag.trim());
                morethanone = true;
            }
        }
        return tagString.toString();
    }

    /**
     *
     */
    public void initTags() {
        if (this.tags == null) {
            this.tags = new HashSet();
        }
    }

}
