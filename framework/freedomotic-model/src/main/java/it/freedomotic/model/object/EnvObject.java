package it.freedomotic.model.object;

import it.freedomotic.model.geometry.FreedomShape;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.shiro.authz.annotation.RequiresPermissions;

public class EnvObject implements Serializable {

    private static final long serialVersionUID = -7253889516478184321L;
	
	private String name;
    private String description;
    private String actAs;
    private String type;
    private String uuid;
    private String hierarchy;
    private String protocol;
    private String phisicalAddress;
    private List<Behavior> behaviors;
    private List<Representation> representation ; //= new ArrayList<Representation>();
    private Set<String> tags;
    private Properties actions;
    private Properties triggers;
    private int currentRepresentation;
    private String envUUID;

    @RequiresPermissions("objects:read")
    public String getEnvironmentID() {
        return this.envUUID;
    }

    @RequiresPermissions("objects:update")
    public void setEnvID(String uuid) {
        this.envUUID = uuid;
    }

    @RequiresPermissions("objects:read")
    public Properties getActions() {
        return actions;
    }

    @RequiresPermissions("objects:read")
    public Properties getTriggers() {
        if (triggers == null) {
            triggers = new Properties();
        }
        return triggers;
    }

    @RequiresPermissions("objects:update")
    public void setName(String name) {
        this.name = name;
    }

    @RequiresPermissions("objects:read")
    public String getName() {
        return this.name;
    }

    @RequiresPermissions("objects:read")
    public String getUUID() {
        return uuid;
    }

    @RequiresPermissions("objects:update")
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    @RequiresPermissions("objects:read")
    public String getHierarchy() {
        return hierarchy;
    }

    @RequiresPermissions("objects:update")
    public void setHierarchy(String hierarchy) {
        this.hierarchy = hierarchy;
    }

    @RequiresPermissions("objects:update")
    public void setCurrentRepresentation(int index) {
        if (representation.get(index) != null) {
            currentRepresentation = index;
        }
    }

    @RequiresPermissions("objects:read")
    public Representation getCurrentRepresentation() {
        return representation.get(currentRepresentation);
    }

    @RequiresPermissions("objects:read")
    public int getCurrentRepresentationIndex() {
        return currentRepresentation;
    }

    @RequiresPermissions("objects:read")
    public List<Representation> getRepresentations() {
        return representation;
    }

    @RequiresPermissions("objects:read")
    public String getProtocol() {
        if ((protocol == null) || (protocol.isEmpty())) {
            protocol = "unknown";
        }
        return protocol;
    }

    @RequiresPermissions("objects:update")
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @RequiresPermissions("objects:read")
    public ArrayList<Behavior> getActiveBehaviors() {
        ArrayList<Behavior> activeBehaviors = new ArrayList<Behavior>();
        for (Behavior behavior : behaviors) {
            if (behavior.isActive()) {
                activeBehaviors.add(behavior);
            }
        }
        return activeBehaviors;
    }

    @RequiresPermissions("objects:read")
    public List<Behavior> getBehaviors() {
        return behaviors;
    }

    @RequiresPermissions("objects:read")
    public Behavior getBehavior(String behavior) {
        for (Behavior b : behaviors) {
            if (b.getName().equalsIgnoreCase(behavior)) {
                return b;
            }
        }
        //Freedomotic.logger.warning("Searching for behavior named '" + behavior + "' but it doesen't exists for object '" + getName() + "'.");
        return null; //this behaviors doesn't exists for this object
    }

    @RequiresPermissions("objects:update")
    public void setActAs(String actAs) {
        this.actAs = actAs;
    }

    @RequiresPermissions("objects:read")
    public String getActAs() {
        return this.actAs;
    }

    @RequiresPermissions("objects:read")
    public String getDescription() {
        return description;
    }

    @RequiresPermissions("objects:update")
    public void setDescription(String desc) {
        this.description = desc;
    }

    @RequiresPermissions("objects:update")
    public void setType(String type) {
        this.type = type;
    }

    @RequiresPermissions("objects:read")
    public String getType() {
        return this.type;
    }

    @RequiresPermissions("objects:read")
    public String getPhisicalAddress() {
        if ((phisicalAddress == null) || (phisicalAddress.isEmpty())) {
            phisicalAddress = "unknown";
        }
        return phisicalAddress.trim();
    }

    @RequiresPermissions("objects:update")
    public void setPhisicalAddress(String address) {
        phisicalAddress = address;
    }

    @RequiresPermissions("objects:read")
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
    @RequiresPermissions("objects:read")
    public HashMap<String, String> getExposedProperties() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("object.name", getName());
        result.put("object.address", getPhisicalAddress());
        result.put("object.protocol", getProtocol());
        result.put("object.type", getType());
        result.put("object.tags", getTagsString());
        return result;
    }

    /**
     * Returns only the last part of the type
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public String getSimpleType() {
        //get the part of the string after the last dot characher
        //eg: 'EnvObject.ElectricDevice.Light' -> returns 'light'
        return getType().substring(
                getType().lastIndexOf(".") + 1).trim().toLowerCase();
    }

    @Override
    @RequiresPermissions("objects:read")
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
    @RequiresPermissions("objects:read")
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    @Override
    @RequiresPermissions("objects:read")
    public String toString() {
        return getName();
    }
    
    @RequiresPermissions("objects:read")
    public Set<String> getTagsList(){
        return this.tags;
    }
    
    @RequiresPermissions("objects:read")       
    public String getTagsString(){
        String tagString = "";
        Boolean morethanone = false;
        for (String tag : getTagsList()){
            if (tag.trim() != ""){
            if (morethanone){
                tagString+=",";
            }
            tagString+=tag.trim();
            morethanone = true;
            }
        }
        return tagString;
    }
       
    public void initTags(){
        if (this.tags == null){
            this.tags = new HashSet();
        }
    }
    
}
