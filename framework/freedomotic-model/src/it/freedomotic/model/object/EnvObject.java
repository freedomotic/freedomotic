package it.freedomotic.model.object;

import it.freedomotic.model.geometry.FreedomShape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author enrico
 */
public class EnvObject implements Serializable {

    private String name;
    private String description;
    private String actAs;
    private String type;
    private String uuid;
    private String hierarchy;
    private String protocol;
    private String phisicalAddress;
    private ArrayList<Behavior> behaviors;
    private ArrayList<Representation> representation = new ArrayList<Representation>();
    private Properties actions;
    private Properties triggers;
    private int currentRepresentation;

//    public Properties getActions() {
//        return actions;
//    }
//
//    public Properties getTriggers() {
//        if (triggers == null) {
//            triggers = new Properties();
//        }
//        return triggers;
//    }
    public Properties getActions() {
        return actions;
    }

    public Properties getTriggers() {
        if (triggers == null) {
            triggers = new Properties();
        }
        return triggers;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    public void setCurrentRepresentation(int index) {
        if (representation.get(index) != null) {
            currentRepresentation = index;
        }
    }

    public Representation getCurrentRepresentation() {
        return representation.get(currentRepresentation);
    }

    public int getCurrentRepresentationIndex() {
        return currentRepresentation;
    }

    public ArrayList<Representation> getRepresentations() {
        return representation;
    }

    public String getProtocol() {
        if ((protocol == null) || (protocol.isEmpty())) {
            protocol = "unknown";
        }
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public ArrayList<Behavior> getActiveBehaviors() {
        ArrayList<Behavior> activeBehaviors = new ArrayList();
        for (Behavior behavior : behaviors) {
            if (behavior.isActive()) {
                activeBehaviors.add(behavior);
            }
        }
        return activeBehaviors;
    }

    public ArrayList<Behavior> getBehaviors() {
        return behaviors;
    }

    public Behavior getBehavior(String behavior) {
        for (Behavior b : behaviors) {
            if (b.getName().equalsIgnoreCase(behavior)) {
                return b;
            }
        }
        //Freedomotic.logger.warning("Searching for behavior named '" + behavior + "' but it doesen't exists for object '" + getName() + "'.");
        return null; //this behaviors doesn't exists for this object
    }

    public void setActAs(String actAs) {
        this.actAs = actAs;
    }

    public String getActAs() {
        return this.actAs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public String getPhisicalAddress() {
        if ((phisicalAddress == null) || (phisicalAddress.isEmpty())) {
            phisicalAddress = "unknown";
        }
        return phisicalAddress.trim();
    }

    public void setPhisicalAddress(String address) {
        phisicalAddress = address;
    }

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
    public HashMap getExposedProperties() {
        HashMap result = new HashMap();
        result.put("object.name", getName());
        result.put("object.address", getPhisicalAddress());
        result.put("object.protocol", getProtocol());
        result.put("object.type", getType());
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
        return getType().substring(
                getType().lastIndexOf(".") + 1).trim().toLowerCase();
    }

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
                if ((this.getPhisicalAddress().equalsIgnoreCase("unknown"))
                        || (this.getProtocol().equalsIgnoreCase("unknown"))) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return getName();
    }
}
