package it.freedomotic.objects;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.core.Resolver;
import it.freedomotic.events.ObjectHasChangedBehavior;
import it.freedomotic.exceptions.EnvObjectMappingException;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.Representation;
import it.freedomotic.persistence.CommandPersistence;
import it.freedomotic.persistence.ReactionPersistence;
import it.freedomotic.persistence.TriggerPersistence;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Statement;
import it.freedomotic.reactions.Trigger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public abstract class EnvObjectLogic {

    private EnvObject pojo;
    private boolean changed;
    private String message;
    private HashMap<String, Command> commandsMapping; //mapping between action name -> hardware command instance
    private ArrayList<BehaviorLogic> behaviors = new ArrayList<BehaviorLogic>();

    protected abstract void createCommands();

    protected abstract void createTriggers();

    /**
     * gets the hardware command mapped to the action in input for example:
     * Action -> Hardware Command Turn on -> Turn on light with X10 Actuator
     * Turn off -> Turn off light with X10 Actuator
     *
     * @param action
     * @return a Command or null if action doesen't exist or the mapping is not
     * valid
     */
    public Command getHardwareCommand(String action) {
        if ((action != null) && (action.trim() != "")) {
            Command commandToSearch = commandsMapping.get(action.trim().toLowerCase());
            if (commandToSearch != null) {
                return commandToSearch;
            } else {
                Freedomotic.logger.severe("Doesn't exists a valid hardware command associated to action '" + action + "' of object '" + pojo.getName() + "'. \n"
                        + "This are the available mappings between action -> command for object '" + pojo.getName() + "': " + commandsMapping.toString());
                return null;
            }
        } else {
            Freedomotic.logger.severe("The action '" + action + "' is not valid in object '" + pojo.getName() + "'");
            return null;
        }
    }

    /**
     * Create an HashMap with all object properties useful in an event
     *
     * @return a set of key/values of object properties
     */
    public HashMap getExposedProperties() {
        HashMap result = pojo.getExposedProperties();
        for (BehaviorLogic behavior : behaviors) {
            result.put("object." + behavior.getName().toLowerCase().trim(), behavior.getValueAsString());
        }
        return result;
    }

    public void rename(String newName) {
        String oldName = this.getPojo().getName();
        newName = newName.trim();
        Freedomotic.logger.warning("Renaming object '" + oldName + "' in '" + newName + "'");
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

    private void renameValuesInTrigger(Trigger t, String oldName, String newName) {
        if (!t.isHardwareLevel()) {
            if (t.getName().contains(oldName)) {
                t.setName(t.getName().replace(oldName, newName));
                Freedomotic.logger.warning("trigger name renamed to " + t.getName());
            }
            Iterator it = t.getPayload().iterator();
            while (it.hasNext()) {
                Statement statement = (Statement) it.next();
                if (statement.getValue().contains(oldName)) {
                    statement.setValue(statement.getValue().replace(oldName, newName));
                    Freedomotic.logger.warning("Trigger value in payload renamed to " + statement.getValue());
                }
            }
        }
    }

    private void renameValuesInCommand(Command c, String oldName, String newName) {
        if (c.getName().contains(oldName)) {
            c.setName(c.getName().replace(oldName, newName));
            Freedomotic.logger.warning("Command name renamed to " + c.getName());
        }
        if (c.getProperty("object") != null) {
            if (c.getProperty("object").contains(oldName)) {
                c.setProperty("object", c.getProperty("object").replace(oldName, newName));
                Freedomotic.logger.warning("Property 'object' in command renamed to " + c.getProperty("object"));
            }
        }
    }

    public void fillDefault() {
        pojo.setName("name");
        pojo.setDescription("description");
        pojo.setType("EnvObject");
        pojo.setPhisicalAddress("address");
        pojo.setDescription("description");
        Representation representation = new Representation();
        pojo.getRepresentations().add(representation);
        pojo.getRepresentations().get(0).setTangible(true);
        pojo.getRepresentations().get(0).setBorderColor("#000000");
        pojo.getRepresentations().get(0).setTextColor("#000000");
        pojo.getRepresentations().get(0).setFillColor("#000000");
        pojo.getRepresentations().get(0).setIcon("accesa.png");
        pojo.getRepresentations().get(0).setOffset(new FreedomPoint(20, 20));
        pojo.getRepresentations().get(0).setRotation(0.0);
        FreedomPolygon p = new FreedomPolygon();
        p.append(0, 0);
        p.append(200, 0);
        p.append(200, 100);
        p.append(0, 100);
        pojo.getRepresentations().get(0).setShape(p);
    }

    private void cacheDeveloperLevelCommand() throws EnvObjectMappingException {
        if (commandsMapping == null) {
            commandsMapping = new HashMap<String, Command>();
        }
        for (String action : pojo.getActions().stringPropertyNames()) {
            String commandName = pojo.getActions().getProperty(action);
            Command command = CommandPersistence.getHardwareCommand(commandName);
            if (command != null) {
                Freedomotic.logger.config("Caching the command '" + command.getName() + "' as related to action '" + action + "' ");
                setAction(action, command);
            } else {
                Freedomotic.logger.warning("Don't exist a command called '" + commandName + "' is not possible to related this command to action '" + action + "' ");
                throw new EnvObjectMappingException();
            }
        }
    }

    public void setAction(String action, Command command) {
        if (action != null && !action.isEmpty() && command != null) {
            commandsMapping.put(action.trim(), command);
            pojo.getActions().setProperty(action.trim(), command.getName());
        }
    }

    public void addTriggerMapping(Trigger trigger, String behaviorName) {
        //checking input parameters
        if (behaviorName == null || behaviorName.isEmpty() || trigger == null) {
            return;
        }
        //parameters in input are ok, continue...
        Iterator it = pojo.getTriggers().entrySet().iterator();
        //remove old references if any
        while (it.hasNext()) {
            Entry e = (Entry) it.next();
            if (e.getValue().equals(behaviorName)) {
                it.remove(); //remove the old value that had to be updated
            }
        }
        pojo.getTriggers().setProperty(trigger.getName(), behaviorName);
        Freedomotic.logger.config("Trigger mapping in object " + this.getPojo().getName()
                + ": behavior '" + behaviorName + "' is now associated to trigger named '" + trigger.getName() + "'");
    }

    public String getAction(String t) {
        return getPojo().getTriggers().getProperty(t);
    }

    public synchronized void setChanged(boolean value) {
        if (value == true) {
            this.changed = true;
            ObjectHasChangedBehavior objectEvent = new ObjectHasChangedBehavior(this, pojo);
            //send multicast because an event must be received by all triggers registred on the destination channel
            Freedomotic.logger.config("Object " + this.getPojo().getName() + " changes something in its status (eg: a behavior value)");
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
    public void registerBehavior(BehaviorLogic b) {
        if (getBehavior(b.getName()) == null) {
            behaviors.add(b);
        } else {
            Freedomotic.logger.severe("Impossible to register behavior " + b.getName() + " in object " + this.getPojo().getName() + " because it is already registed");
        }
    }

    /**
     * Finds a behavior using its name (case sensitive)
     *
     * @param name
     * @return the reference to the behavior or null if it doesen't exists
     */
    public BehaviorLogic getBehavior(String name) {
        for (BehaviorLogic behaviorLogic : behaviors) {
            if (behaviorLogic.getName().equals(name)) {
                return behaviorLogic;
            }
        }
        return null;
    }

    /**
     * Caches developers level commands and creates user level commands as
     * specified in the createCommands() method of its subclasses
     */
    public void init() throws EnvObjectMappingException {
        createCommands();
        createTriggers();
        commandsMapping = new HashMap<String, Command>();
        cacheDeveloperLevelCommand();
        for (Entry e : pojo.getTriggers().entrySet()) {
            Freedomotic.logger.info("Trigger '" + e.getKey() + "' of object '" + pojo.getName() + "' is mapped to its '" + e.getValue() + "' behavior");
        }
    }

    public boolean executeTrigger(Trigger t) {
        String behavior = getAction(t.getName());
        if (behavior == null) {
            //Freedomotic.logger.severe("Hardware trigger '" + t.getName() + "' is not bound to any action of object " + this.getPojo().getName());
            return false;
        }

        Statement valueStatement = t.getPayload().getStatements("behaviorValue").get(0);
        if (valueStatement == null) {
            Freedomotic.logger.warning("No value in hardware trigger '" + t.getName() + "' to apply to object action '" + behavior + "' of object " + getPojo().getName());
            return false;
        }
        Freedomotic.logger.config("Sensors notification '" + t.getName() + "' has changed '" + getPojo().getName() + "' behavior '" + behavior + "' to " + valueStatement.getValue());
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
    public boolean executeCommand(final String action, final Config params) {
        Freedomotic.logger.config("Executing action '" + action + "' of object '" + getPojo().getName() + "'");
        if (getPojo().getActAs().equalsIgnoreCase("virtual")) {
            //it's a virtual object like a button, not needed real execution of a command
            Freedomotic.logger.info("The object '" + getPojo().getName() + "' act as virtual device, so its hardware commands are not executed.");
            return true;
        }
        final Command command = getHardwareCommand(action.trim());
        if (command == null) {
            Freedomotic.logger.warning("The hardware level command for action '" + action + "' in object '" + pojo.getName() + "' doesn't exists or is not setted");
            return false; //command not executed
        }
        //resolves developer level command parameters like myObjectName = "@event.object.name" -> myObjectName = "Light 1"
        //in this case the parameter in the userLevelCommand are used as basis for the resolution process (the context)
        //along with the parameters getted from the relative behavior (if exists)
        Freedomotic.logger.config("Environment object '" + pojo.getName() + "' tries to '" + action + "' itself using hardware command '" + command.getName() + "'");
        Resolver resolver = new Resolver();
        //adding a resolution context for object that owns this hardware level command. 'owner.' is the prefix of this context
        resolver.addContext("owner.", getExposedProperties());
        //adding a resolution context for event parameters. 'event.' is the prefix of this context
        //resolver.addContext("event.", params);
        try {
            final Command resolvedCommand = resolver.resolve(command); //eg: turn on an X10 device
            Command result = Freedomotic.sendCommand(resolvedCommand); //blocking wait until timeout
            if (result != null && result.isExecuted()) {
                return true; //succesfully executed
            }
        } catch (CloneNotSupportedException ex) {
            Logger.getLogger(EnvObjectLogic.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false; //command not executed
    }

    public boolean isChanged() {
        return changed;
    }

    public void setPojo(EnvObject pojo) {
        this.pojo = pojo;
    }

    public EnvObject getPojo() {
        return pojo;
    }

    public void destroy() {
        pojo = null;
        commandsMapping.clear();
        commandsMapping = null;
        behaviors.clear();
        behaviors = null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final EnvObjectLogic other = (EnvObjectLogic) obj;
        if (this.pojo != other.pojo && (this.pojo == null || !this.pojo.equals(other.pojo))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.pojo != null ? this.pojo.hashCode() : 0);
        return hash;
    }

    public Iterable<BehaviorLogic> getBehaviors() {
        return behaviors;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        if (message == null) {
            return "";
        } else {
            return message;
        }
    }

    public void setRandomLocation() {
        int randomX = 0 + (int) (Math.random() * Freedomotic.environment.getPojo().getWidth());
        int randomY = 0 + (int) (Math.random() * Freedomotic.environment.getPojo().getHeight());
        for (Representation rep : getPojo().getRepresentations()) {
            rep.setOffset(randomX, randomY);
        }
    }

    public void setLocation(int x, int y) {
        for (Representation rep : getPojo().getRepresentations()) {
            rep.setOffset(x, y);
        }
    }
}
