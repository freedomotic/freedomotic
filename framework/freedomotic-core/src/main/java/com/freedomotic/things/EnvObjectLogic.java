/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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

import com.freedomotic.behaviors.BehaviorLogic;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.core.Resolver;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.environment.ZoneLogic;
import com.freedomotic.events.ObjectHasChangedBehavior;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.geometry.FreedomShape;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.util.TopologyUtils;
import com.google.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author Enrico
 */
public class EnvObjectLogic {

    private EnvObject pojo;
    private boolean changed;
    // private String message;
    private Map<String, Command> commandsMapping; //mapping between action name -> hardware command instance
    private Map<String, BehaviorLogic> behaviors = new HashMap<String, BehaviorLogic>();
    private EnvironmentLogic environment;

    @Inject
    protected EnvironmentRepository environmentRepository;

    /**
     * Instantiation disabled from outside its package. Use
     * {@code EnvObjectFactory} to generate instances of {@code EnvObjectLogic}
     */
    protected EnvObjectLogic() {
        super();
    }

    /**
     * gets the hardware command mapped to the action in input for example:
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
                LOG.log(Level.SEVERE, "Doesn''t exists a valid hardware command associated to action ''{0}'' of object ''{1}"
                        + "''. \n"
                        + "This are the available mappings between action -> command for object ''{2}'': {3}",
                        new Object[]{action, pojo.getName(), pojo.getName(), commandsMapping.toString()});

                return null;
            }
        } else {
            LOG.log(Level.SEVERE, "The action ''{0}'' is not valid in object ''{1}''",
                    new Object[]{action, pojo.getName()});

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
        HashMap<String, String> result = pojo.getExposedProperties();
        return result;
    }

    /**
     *
     * @return
     */
    @RequiresPermissions("objects:read")
    public Map<String, String> getExposedBehaviors() {
        Map<String, String> result = new HashMap<String, String>();
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
        newName = newName.trim();
        LOG.log(Level.WARNING, "Renaming object ''{0}'' in ''{1}''", new Object[]{oldName, newName});
        //change the object name
        this.getPojo().setName(newName);

        //change trigger references to this object
        for (Trigger t : TriggerPersistence.getTriggers()) {
            renameValuesInTrigger(t, oldName, newName);
        }

        //change commands references to this object
        for (Command c : CommandPersistence.getUserCommands()) {
            renameValuesInCommand(c, oldName, newName);
        }

        //rebuild reactions description
        for (Reaction r : ReactionPersistence.getReactions()) {
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
            throw new IllegalArgumentException("behavior name and trigger cannot be null");
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

        pojo.getTriggers().setProperty(trigger.getName(),
                behaviorName);
        LOG.log(Level.CONFIG, "Trigger mapping in object {0}: behavior ''{1}'' is now associated to trigger named ''{2}''",
                new Object[]{this.getPojo().getName(), behaviorName, trigger.getName()});
    }

    /**
     *
     * @param t
     * @return
     */
    @RequiresPermissions("objects:read")
    public String getAction(String t) {
        return getPojo().getTriggers().getProperty(t);
    }

    /**
     *
     * @param value
     */
    @RequiresPermissions("objects:update")
    public synchronized void setChanged(boolean value) {
        if (value == true) {
            this.changed = true;

            ObjectHasChangedBehavior objectEvent = new ObjectHasChangedBehavior(this, this);
            //send multicast because an event must be received by all triggers registred on the destination channel
            LOG.log(Level.FINE, "Object {0} changes something in its status (eg: a behavior value)",
                    this.getPojo().getName());
            Freedomotic.sendEvent(objectEvent);
        } else {
            changed = false;
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
        if (getBehavior(b.getName()) != null) {
            behaviors.remove(b.getName());
            LOG.warning("Re-registering existing behavior " + b.getName() + " in object " + this.getPojo().getName());
            //throw new IllegalArgumentException("Impossible to register behavior " + b.getName() + " in object "
            //        + this.getPojo().getName() + " because it is already registed");
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
        return behaviors.get(name);
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
        commandsMapping = new HashMap<String, Command>();
        cacheDeveloperLevelCommand();
        // assign object to an environment
        this.setEnvironment(environmentRepository.findOne(pojo.getEnvironmentID()));
        // update topology information
        updateTopology();
    }

    @Deprecated
    @RequiresPermissions("objects:read")
    private boolean isChanged() {
        return changed;
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
        // if (pojo.getUUID() == null  || auth.isPermitted("objects:read:" + pojo.getUUID().substring(0, 5))
        //         ) {
        return pojo;
        //  }
        //  return null;
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

        if ((this.pojo != other.pojo) && ((this.pojo == null) || !this.pojo.equals(other.pojo))) {
            return false;
        }

        return true;
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
        int randomX = 0 + (int) (Math.random() * environmentRepository.findAll().get(0).getPojo().getWidth());
        int randomY = 0 + (int) (Math.random() * environmentRepository.findAll().get(0).getPojo().getHeight());
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
        FreedomPolygon translatedObject
                = (FreedomPolygon) TopologyUtils.translate((FreedomPolygon) shape, xoffset, yoffset);

        //REGRESSION
        for (EnvironmentLogic locEnv : environmentRepository.findAll()) {
            for (ZoneLogic zone : locEnv.getZones()) {
                if (this.getEnvironment() == locEnv && TopologyUtils.intersects(translatedObject, zone.getPojo().getShape())) {
                    //DEBUG: System.out.println("object " + getPojo().getName() + " intersects zone " + zone.getPojo().getName());
                    //add to the zones this object belongs
                    zone.getPojo().getObjects().add(this.getPojo());
                    LOG.log(Level.FINE, "Object {0} is in zone {1}", new Object[]{getPojo().getName(), zone.getPojo().getName()});
                } else {
                    //remove from the zone
                    zone.getPojo().getObjects().remove(this.getPojo());
                    //DEBUG: System.out.println("object " + getPojo().getName() + " NOT intersects zone " + zone.getPojo().getName());
                }
            }
        }
    }

    /**
     * Changes a behavior value accordingly to the value property in the trigger
     * in input
     *
     * @param t an hardware level trigger
     * @return true if the values is applied successfully, false otherwise
     */
    public final boolean executeTrigger(Trigger t) {
        String behavior = getAction(t.getName());

        if (behavior == null) {
            //LOG.severe("Hardware trigger '" + t.getName() + "' is not bound to any action of object " + this.getPojo().getName());
            //check if the behavior name is written in the trigger

            behavior = t.getPayload().getStatements("behavior.name").isEmpty() ? "" : t.getPayload().getStatements("behavior.name").get(0).getValue();

            if (behavior.isEmpty()) {
                return false;
            }
        }

        Statement valueStatement = t.getPayload().getStatements("behaviorValue").get(0);

        if (valueStatement == null) {
            LOG.log(Level.WARNING,
                    "No value in hardware trigger ''{0}'' to apply to object action ''{1}'' of object {2}",
                    new Object[]{t.getName(), behavior, getPojo().getName()});

            return false;
        }

        LOG.log(Level.CONFIG,
                "Sensors notification ''{0}'' has changed ''{1}'' behavior ''{2}'' to {3}",
                new Object[]{t.getName(), getPojo().getName(), behavior, valueStatement.getValue()});

        Config params = new Config();
        params.setProperty("value", valueStatement.getValue());
        getBehavior(behavior).filterParams(params, false); //false means not fire commands, only change behavior value

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
        LOG.log(Level.FINE, "Executing action ''{0}'' of object ''{1}''", new Object[]{action, getPojo().getName()});

        if (getPojo().getActAs().equalsIgnoreCase("virtual")) {
            //it's a virtual object like a button, not needed real execution of a command
            LOG.log(Level.CONFIG,
                    "The object ''{0}'' act as virtual device, so its hardware commands are not executed.",
                    getPojo().getName());

            return true;
        }

        final Command command = getHardwareCommand(action.trim());

        if (command == null) {
            LOG.log(Level.WARNING,
                    "The hardware level command for action ''{0}'' in object ''{1}'' doesn''t exists or is not set",
                    new Object[]{action, pojo.getName()});

            return false; //command not executed
        }

        //resolves developer level command parameters like myObjectName = "@event.object.name" -> myObjectName = "Light 1"
        //in this case the parameter in the userLevelCommand are used as basis for the resolution process (the context)
        //along with the parameters getted from the relative behavior (if exists)
        LOG.log(Level.FINE, "Environment object ''{0}'' tries to ''{1}'' itself using hardware command ''{2}''",
                new Object[]{pojo.getName(), action, command.getName()});

        Resolver resolver = new Resolver();
        //adding a resolution context for object that owns this hardware level command. 'owner.' is the prefix of this context
        resolver.addContext("request.", params);
        resolver.addContext("owner.", getExposedProperties());
        resolver.addContext("owner.", getExposedBehaviors());

        try {
            final Command resolvedCommand = resolver.resolve(command); //eg: turn on an X10 device

            Command result;
            //mark the command as not executed if it is supposed to not return
            //an execution state value
            if (Boolean.valueOf(command.getProperty("send-and-forget")) == true) {
                LOG.config("Command '" + resolvedCommand.getName() + "' is 'send-and-forget' no execution result will be catched from plugin's reply");
                resolvedCommand.setReplyTimeout(-1); //disable reply request
                Freedomotic.sendCommand(resolvedCommand);
                return false;
            } else {
                //10 seconds is the default timeout if not already set
                if (resolvedCommand.getReplyTimeout() < 1) {
                    resolvedCommand.setReplyTimeout(10000); //enable reply request
                }
                result = Freedomotic.sendCommand(resolvedCommand); //blocking wait until timeout
            }

            if (result == null) {
                LOG.log(Level.WARNING, "Received null reply after sending hardware command " + resolvedCommand.getName());
            } else if (result.isExecuted()) {
                return true; //succesfully executed
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(EnvObjectLogic.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VariableResolutionException ex) {
            Logger.getLogger(EnvObjectLogic.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false; //command not executed
    }

    /**
     *
     */
    protected void createCommands() {
        //default empty implementation
    }

    /**
     *
     */
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
                LOG.log(Level.WARNING, "trigger name renamed to {0}", t.getName());
            }
            Iterator<Statement> it = t.getPayload().iterator();
            while (it.hasNext()) {
                Statement statement = it.next();
                if (statement.getValue().contains(oldName)) {
                    statement.setValue(statement.getValue().replace(oldName, newName));
                    LOG.log(Level.WARNING, "Trigger value in payload renamed to {0}", statement.getValue());
                }
            }
        }
    }

    @RequiresPermissions({"objects:read", "commands:update"})
    private void renameValuesInCommand(Command c, String oldName, String newName) {
        if (c.getName().contains(oldName)) {
            c.setName(c.getName().replace(oldName, newName));
            LOG.log(Level.WARNING, "Command name renamed to {0}", c.getName());
        }

        if (c.getProperty("object") != null) {
            if (c.getProperty("object").contains(oldName)) {
                c.setProperty("object",
                        c.getProperty("object").replace(oldName, newName));
                LOG.log(Level.WARNING, "Property ''object'' in command renamed to {0}", c.getProperty("object"));
            }
        }
    }

    private void cacheDeveloperLevelCommand() {
        if (commandsMapping == null) {
            commandsMapping = new HashMap<String, Command>();
        }

        for (String action : pojo.getActions().stringPropertyNames()) {
            String commandName = pojo.getActions().getProperty(action);
            Command command = CommandPersistence.getHardwareCommand(commandName);

            if (command != null) {
                LOG.log(Level.CONFIG,
                        "Caching the command ''{0}'' as related to action ''{1}'' ",
                        new Object[]{command.getName(), action});
                setAction(action, command);
            } else {
                LOG.log(Level.CONFIG,
                        "Don''t exist a command called ''{0}'' is not possible to bound this command to action ''{1}'' of {2}",
                        new Object[]{commandName, action, this.getPojo().getName()});
            }
        }
    }

    /**
     *
     * @param selEnv
     */
    @RequiresPermissions("objects:update")
    public void setEnvironment(EnvironmentLogic selEnv) {
        if (selEnv == null) {
            LOG.warning("Trying to assign a null environment to object " + this.getPojo().getName()
                    + ". It will be relocated to the fallback environment");
            selEnv = environmentRepository.findAll().get(0);
            if (selEnv == null) {
                throw new IllegalArgumentException("Fallback environment is null for object " + getPojo().getName());
            }
        }
        this.environment = selEnv;
        getPojo().setEnvironmentID(selEnv.getPojo().getUUID());
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
    private static final Logger LOG = Logger.getLogger(EnvObjectLogic.class.getName());
}
