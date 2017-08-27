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
package com.freedomotic.things;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.Resolver;
import com.freedomotic.core.SynchAction;
import com.freedomotic.core.SynchThingRequest;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.geometry.FreedomShape;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.rules.Statement;
import com.freedomotic.util.TopologyUtils;
import com.google.inject.Inject;

/**
 *
 * @author Enrico Nicoletti
 */
public class EnvObjectLogic {

    private static final Logger LOG = LoggerFactory.getLogger(EnvObjectLogic.class.getName());
    private EnvObject pojo;
    private Map<String, Command> commandsMapping; //mapping between action name -> hardware command instance
    private Map<String, BehaviorLogic> behaviors = new HashMap<>();
    private EnvironmentLogic environment;

    @Inject
    protected EnvironmentRepository environmentRepository;
    @Inject
    protected TriggerRepository triggerRepository;
    @Inject
    protected CommandRepository commandRepository;
    @Inject
    protected ReactionRepository reactionRepository;
    @Inject
    private BusService busService;

    /**
     * Instantiation disabled from outside its package. Use
     * {@code EnvObjectFactory} to generate instances of {@code EnvObjectLogic}
     */
    protected EnvObjectLogic() {
        super();
    }

    /**
     * Gets the hardware command mapped to the action in input for example:
     * Action -> Hardware Command Turn on -> Turn on light with X10 Actuator
     * Turn off -> Turn off light with X10 Actuator
     *
     * @param action
     * @return a Command or null if action doesn't exist or the mapping is not
     * valid
     */
    @RequiresPermissions("objects:read")
    public final Command getHardwareCommand(String action) {
        if ((action != null) && (!action.trim().isEmpty())) {
            Command commandToSearch = commandsMapping.get(action.trim().toLowerCase());

            if (commandToSearch != null) {
                return commandToSearch;
            } else {
                LOG.error("Doesn''t exists a valid hardware command associated to action \"{}\" of thing \"{}"
                        + "\". \n"
                        + "These are the available mappings between action -> command for thing \"{}\": {}",
                        action, pojo.getName(), pojo.getName(), commandsMapping);

                return null;
            }
        } else {
            LOG.error("The action \"{}\" is not valid in thing \"{}\"", action, pojo.getName());

            return null;
        }
    }

    /**
     * Create an HashMap with all object properties useful in an event
     *
     * @return a set of key/values of object properties
     */
    @RequiresPermissions("objects:read")
    public Map<String, String> getExposedProperties() {
        return pojo.getExposedProperties();
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public Map<String, String> getExposedBehaviors() {
        Map<String, String> result = new HashMap<>();
        for (BehaviorLogic behavior : getBehaviors()) {
            result.put("object.behavior." + behavior.getName(),
                    behavior.getValueAsString());
        }

        return result;
    }

    /**
     *
     * @param newName
     */
    @RequiresPermissions("objects:update")
    public final void rename(String newName) {
        String oldName = this.getPojo().getName();
        String trimmedNewName = newName.trim();
        LOG.warn("Renaming thing \"{}\" in \"{}\"", oldName, trimmedNewName);
        //change the object name
        this.getPojo().setName(trimmedNewName);

        //change trigger references to this thing
        for (Trigger t : triggerRepository.findAll()) {
            renameValuesInTrigger(t, oldName, trimmedNewName);
        }

        //change commands references to this thing
        for (Command c : commandRepository.findUserCommands()) {
            renameValuesInCommand(c, oldName, trimmedNewName);
        }

        //rebuild reactions description
        for (Reaction r : reactionRepository.findAll()) {
            r.setChanged();
        }
    }

    /**
     *
     * @param action
     * @param command
     */
    @RequiresPermissions("objects:update")
    public void setAction(String action, Command command) {
        if ((action != null) && !action.isEmpty() && (command != null)) {
            commandsMapping.put(action.trim(),
                    command);
            pojo.getActions().setProperty(action.trim(),
                    command.getName());
        }
    }

    /**
     *
     * @param trigger
     * @param behaviorName
     */
    @RequiresPermissions("objects:update")
    public void addTriggerMapping(Trigger trigger, String behaviorName) {
        //checking input parameters
        if ((behaviorName == null) || behaviorName.isEmpty() || (trigger == null)) {
            throw new IllegalArgumentException("Behavior name and trigger cannot be null");
        }

        //parameters in input are ok, continue...
        Iterator<Entry<String, String>> it = pojo.getTriggers().entrySet().iterator();
        //remove old references if any
        while (it.hasNext()) {
            Entry<String, String> e = it.next();
            if (e.getValue().equals(behaviorName)) {
                it.remove(); //remove the old value that had to be updated
            }
        }

        pojo.getTriggers().setProperty(trigger.getName(), behaviorName);
        LOG.info("Trigger mapping in thing \"{}\": behavior \"{}\" is now associated to trigger named \"{}\"",
                this.getPojo().getName(), behaviorName, trigger.getName());
    }

    /**
     *
     * @param t
     * @return
     */
    @RequiresPermissions("objects:read")
    public String getBehaviorNameMappedToTrigger(String t) {
        return getPojo().getTriggers().getProperty(t);
    }

    /**
     * Notify that this Thing was created, deleted or updated. To just notify an
     * update is better to use the {@link setChanged(true)} method.
     *
     * @param action
     */
    public void setChanged(SynchAction action) {
        switch (action) {
            case CREATED:
                SynchThingRequest creationEvent = new SynchThingRequest(SynchAction.CREATED, getPojo());
                busService.send(creationEvent);
                break;
            case DELETED:
                SynchThingRequest deletionEvent = new SynchThingRequest(SynchAction.DELETED, getPojo());
                busService.send(deletionEvent);
                break;
            case UPDATED:
                // do nothing, the update is forced later
                break;
            default:
                throw new AssertionError(action.name());
        }
        setChanged(true); //force the update in any case
    }
   
    /**
     *
     * @param value
     */
    @RequiresPermissions("objects:update")
    public synchronized void setChanged(boolean value) {
        if (value) {
            ObjectHasChangedBehavior objectEvent = new ObjectHasChangedBehavior(this, this);
            //send multicast because an event must be received by all triggers registred on the destination channel
            if (LOG.isDebugEnabled()) {
                LOG.debug("Thing \"{}\" changes something in its status (eg: a behavior value)", this.getPojo().getName());
            }
            busService.send(objectEvent);
        }
    }

    /**
     * When defining an object logic the registration of its behaviors is needed
     * otherwise they are not used.
     *
     * @param b
     */
    @RequiresPermissions("objects:update")
    public final void registerBehavior(BehaviorLogic b) {
        if (behaviors.get(b.getName()) != null) {
            behaviors.remove(b.getName());
            LOG.warn("Re-registering existing behavior \"{}\" in thing \"{}\"",
            		b.getName(), this.getPojo().getName());
        }

        behaviors.put(b.getName(), b);
    }

    /**
     * Finds a behavior using its name (case sensitive)
     *
     * @param name
     * @return the reference to the behavior or null if it doesn't exists
     */
    @RequiresPermissions("objects:read")
    public final BehaviorLogic getBehavior(String name) {
        BehaviorLogic behaviorLogic = behaviors.get(name);
        // Manage the case the behavior is not found
        if (behaviorLogic == null) {
            // Create a list of available behaviors
            StringBuilder builder = new StringBuilder();
            for (BehaviorLogic behavior : behaviors.values()) {
                builder.append(behavior.getName()).append(" ");
            }
            
            String bhvrs = builder.toString();
            // Print an user friendly message
            LOG.error("Cannot find a behavior named \"{}\" for thing named \"{}\". "
                    + "Available behaviors for this thing are: \"{}\"",
                    name, getPojo().getName(), bhvrs);
        }
        return behaviorLogic;
    }

    /**
     * Caches developers level commands and creates user level commands as
     * specified in the createCommands() method of its subclasses
     */
    @RequiresPermissions("objects:read")
    public void init() {
        //validation
        if (pojo == null) {
            throw new IllegalStateException("An object must have a valid pojo before initialization");
        }
        pojo.initTags();
        createCommands();
        createTriggers();
        commandsMapping = new HashMap<>();
        cacheDeveloperLevelCommand();
        // assign object to an environment
        this.setEnvironment(environmentRepository.findOne(pojo.getEnvironmentID()));
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public EnvironmentLogic getEnvironment() {
        return this.environment;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public EnvObject getPojo() {
        return pojo;
    }

    /**
     *
     */
    @RequiresPermissions("objects:delete")
    public final void destroy() {
        pojo = null;
        commandsMapping.clear();
        commandsMapping = null;
        behaviors.clear();
        behaviors = null;
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    @RequiresPermissions("objects:read")
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final EnvObjectLogic other = (EnvObjectLogic) obj;

        return !((this.pojo != other.pojo) && (this.pojo == null || !this.pojo.equals(other.pojo)));
    }

    /**
     *
     * @return
     */
    @Override
    @RequiresPermissions("objects:read")
    public int hashCode() {
        int hash = 7;
        hash = (53 * hash) + ((this.pojo != null) ? this.pojo.hashCode() : 0);

        return hash;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public Iterable<BehaviorLogic> getBehaviors() {
        return behaviors.values();
    }

    /**
     *
     */
    @RequiresPermissions("objects:create")
    public final void setRandomLocation() {
    	Random random = new Random();
        int randomX = (random.nextInt() * environmentRepository.findAll().get(0).getPojo().getWidth());
        int randomY = (random.nextInt() * environmentRepository.findAll().get(0).getPojo().getHeight());
        setLocation(randomX, randomY);
    }

    /**
     *
     * @param x
     * @param y
     */
    @RequiresPermissions("objects:update")
    public void setLocation(int x, int y) {
        for (Representation rep : getPojo().getRepresentations()) {
            rep.setOffset(x, y);
        }

        updateTopology();
        //commit the changes to this object
        setChanged(true);
    }

    /**
     * Sets the object location without invoking an object change notification
     * An user should never use this method. It's needed by the framework and
     * reserver for it's exclusive use.
     */
    public void synchLocation(int x, int y) {
        for (Representation rep : getPojo().getRepresentations()) {
            rep.setOffset(x, y);
        }
        updateTopology();
    }

    @RequiresPermissions({"objects:read", "zones.update"})
    private void updateTopology() {
        FreedomShape shape = getPojo().getRepresentations().get(0).getShape();
        int xoffset = getPojo().getCurrentRepresentation().getOffset().getX();
        int yoffset = getPojo().getCurrentRepresentation().getOffset().getY();

        //now apply offset to the shape
        FreedomPolygon translatedObject = TopologyUtils.translate((FreedomPolygon) shape, xoffset, yoffset);

        //REGRESSION
        for (EnvironmentLogic locEnv : environmentRepository.findAll()) {
            for (ZoneLogic zone : locEnv.getZones()) {
                if (this.getEnvironment() == locEnv && TopologyUtils.intersects(translatedObject, zone.getPojo().getShape())) {
                    //add to the zones this object belongs
                    zone.getPojo().getObjects().add(this.getPojo());
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Thing \"{}\" is in zone \"{}\"", getPojo().getName(), zone.getPojo().getName());
                    }
                } else {
                    //remove from the zone
                    zone.getPojo().getObjects().remove(this.getPojo());
                }
            }
        }
    }

    /**
     * Changes a behavior value accordingly to the value property in the trigger
     * in input without firing a command on hardware. It updates only the
     * internal model
     *
     * @param trigger an hardware level trigger
     * @return true if the values is applied successfully, false otherwise
     */
    public final boolean executeTrigger(Trigger trigger) {
        // Get the behavior name connected to the trigger in input
        String behaviorName = getBehaviorNameMappedToTrigger(trigger.getName());
        // If missing because it's not an hardware trigger check if it is specified in the trigger itself
        if (behaviorName == null) {
            //check if the behavior name is written in the trigger
            behaviorName = trigger.getPayload().getStatements("behavior.name").isEmpty()
                    ? "" : trigger.getPayload().getStatements("behavior.name").get(0).getValue();
            if (behaviorName.isEmpty()) {
                return false;
            }
        }

        Statement valueStatement = trigger.getPayload().getStatements("behaviorValue").get(0);

        if (valueStatement == null) {
            LOG.warn(
                    "No value in hardware trigger \"{}\" to apply to behavior \"{}\" of thing \"{}\"",
                   trigger.getName(), behaviorName, getPojo().getName());
            return false;
        }

        LOG.info(
                "Sensors notification \"{}\" is going to change \"{}\" behavior \"{}\" to \"{}\"",
                trigger.getName(), getPojo().getName(), behaviorName, valueStatement.getValue());

        Config params = new Config();
        params.setProperty("value", valueStatement.getValue());
        // Validating the target behavior
        BehaviorLogic behavior = getBehavior(behaviorName);
        if (behavior != null) {
            behavior.filterParams(params, false); //false means not fire commands, only change behavior value
        } else {
            LOG.error("Cannot apply trigger \"{}\" to thing \"{}\"", trigger.getName(), getPojo().getName());
            return false;
        }
        return true;
    }

    /**
     * Executes the hardware command related to the action passed as paramenter
     * using an user command.
     *
     * @param action the name of the action to executeCommand as defined in the
     * object XML
     * @param params parameters of the event that have started the reaction
     * execution
     * @return true if the command is succesfully executed by the actuator and
     * false otherways
     */
    @RequiresPermissions("objects:read")
    protected final boolean executeCommand(final String action, final Config params) {
        LOG.debug("Executing action \"{}\" of thing \"{}\"", action, getPojo().getName());

        if ("virtual".equalsIgnoreCase(getPojo().getActAs())) {
            //it's a virtual object like a button, not needed real execution of a command
            LOG.info(
                    "The thing \"{}\" act as virtual device so its hardware commands are not executed.",
                    getPojo().getName());

            return true;
        }

        final Command command = getHardwareCommand(action.trim());

        if (command == null) {
            LOG.warn(
                    "The hardware level command for action \"{}\" in thing \"{}\" doesn''t exists or is not set",
                    action, pojo.getName());

            return false; //command not executed
        }

        //resolves developer level command parameters like myObjectName = "@event.object.name" -> myObjectName = "Light 1"
        //in this case the parameter in the userLevelCommand are used as basis for the resolution process (the context)
        //along with the parameters getted from the relative behavior (if exists)
        if (LOG.isDebugEnabled()) {
            LOG.debug("Environment object \"{}\" tries to \"{}\" itself using hardware command \"{}\"",
                    pojo.getName(), action, command.getName());
        }

        Resolver resolver = new Resolver();
        //adding a resolution context for object that owns this hardware level command. 'owner.' is the prefix of this context
        resolver.addContext("", params);
        resolver.addContext("owner.", getExposedProperties());
        resolver.addContext("owner.", getExposedBehaviors());

        try {
            final Command resolvedCommand = resolver.resolve(command); //eg: turn on an X10 device

            Command result;
            //mark the command as not executed if it is supposed to not return
            //an execution state value
            if (Boolean.valueOf(command.getProperty("send-and-forget"))) {
                LOG.info("Command \"{}\" is \"send-and-forget\". No execution result will be catched from plugin''s reply", resolvedCommand.getName());
                resolvedCommand.setReplyTimeout(-1); //disable reply request
                busService.send(resolvedCommand);
                return false;
            } else {
                //10 seconds is the default timeout if not already set
                if (resolvedCommand.getReplyTimeout() < 1) {
                    resolvedCommand.setReplyTimeout(10000); //enable reply request
                }
                result = busService.send(resolvedCommand); //blocking wait until timeout
            }

            if (result == null) {
                LOG.warn("Received null reply after sending hardware command \"{}\"", resolvedCommand.getName());
            } else if (result.isExecuted()) {
                return true; //succesfully executed
            }
        } catch (CloneNotSupportedException | VariableResolutionException ex) {
            LOG.error(ex.getMessage());
        } 
        return false; //command not executed
    }

    protected void createCommands() {
        //default empty implementation
    }

    protected void createTriggers() {
        //default empty implementation
    }

    /**
     *
     * @param pojo
     */
    @RequiresPermissions("objects:update")
    protected void setPojo(EnvObject pojo) {
        this.pojo = pojo;
    }

    @RequiresPermissions({"objects:update", "triggers:update"})
    private void renameValuesInTrigger(Trigger t, String oldName, String newName) {
        if (!t.isHardwareLevel()) {
            if (t.getName().contains(oldName)) {
                t.setName(t.getName().replace(oldName, newName));
                LOG.warn("Trigger name renamed to \"{}\"", t.getName());
            }
            Iterator<Statement> it = t.getPayload().iterator();
            while (it.hasNext()) {
                Statement statement = it.next();
                if (statement.getValue().contains(oldName)) {
                    statement.setValue(statement.getValue().replace(oldName, newName));
                    LOG.warn("Trigger value in payload renamed to \"{}\"", statement.getValue());
                }
            }
        }
    }

    @RequiresPermissions({"objects:read", "commands:update"})
    private void renameValuesInCommand(Command c, String oldName, String newName) {
        if (c.getName().contains(oldName)) {
            c.setName(c.getName().replace(oldName, newName));
            LOG.warn("Command name renamed to \"{}\"", c.getName());
        }
        
        String objectLabel = "object";

        if (c.getProperty(objectLabel) != null && c.getProperty(objectLabel).contains(oldName)) {
        	c.setProperty(objectLabel, c.getProperty(objectLabel).replace(oldName, newName));
        	LOG.warn("Property \"object\" in command renamed to \"{}\"", c.getProperty(objectLabel));
        }
    }

    private void cacheDeveloperLevelCommand() {
        if (commandsMapping == null) {
            commandsMapping = new HashMap<>();
        }

        for (String action : pojo.getActions().stringPropertyNames()) {
            String commandName = pojo.getActions().getProperty(action);
            Command command;
            List<Command> list = commandRepository.findByName(commandName);
            if (!list.isEmpty()) {
                command = list.get(0);
            } else {
                throw new FreedomoticRuntimeException("No commands found with name \"" + commandName + "\"");
            }

            if (command != null) {
                LOG.debug("Caching the command \"{}\" as related to action \"{}\" ", command.getName(), action);
                setAction(action, command);
            } else {
                LOG.warn(
                        "Doesn''t exist a command called \"{}\". It's not possible to bound this command to action \"{}\" of \"{}\"",
                        commandName, action, this.getPojo().getName());
            }
        }
    }

    /**
     *
     * @param selEnv
     */
    @RequiresPermissions("objects:update")
    public void setEnvironment(EnvironmentLogic selEnv) {
    	
    	EnvironmentLogic newEnvironment = selEnv;
    	
        if (newEnvironment == null) {
            LOG.warn("Trying to assign a null environment to thing \"" + this.getPojo().getName()
                    + "\". It will be relocated to the fallback environment");
            newEnvironment = environmentRepository.findAll().get(0);
            if (newEnvironment == null) {
                throw new IllegalArgumentException("Fallback environment is null for thing \"" + getPojo().getName() + "\"");
            }
        }
        this.environment = newEnvironment;
        getPojo().setEnvironmentID(newEnvironment.getPojo().getUUID());
        // update topology information
        updateTopology();
    }

    /**
     *
     * @param tagList
     */
    @RequiresPermissions("objects:update")
    public void addTags(String tagList) {
        String[] tags = tagList.toLowerCase().split(",");
        getPojo().getTagsList().addAll(Arrays.asList(tags));
    }
}
