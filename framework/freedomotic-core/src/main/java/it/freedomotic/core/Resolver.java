/* Copyright 2009 Enrico Nicoletti
 * eMail: enrico.nicoletti84@gmail.com
 *
 * This file is part of Freedomotic.
 *
 * Freedomotic is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * any later version.
 *
 * Freedomotic is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.freedomotic.core;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.ds.Config;
import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Payload;
import it.freedomotic.reactions.Reaction;
import it.freedomotic.reactions.Statement;
import it.freedomotic.reactions.Trigger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * Resolves command values using an event as the context of resolution eg: param
 * sensorPlugin = "
 *
 * @event.sender" becomes sensorPlugin="TemperatureSensorPlugin"
 *
 * <p> This class takes a list of properties in form key=value and propagates
 * this list to all commands in a given reaction. After that this list (called
 * context) is used to resolve the references to external values in the command.
 * For example Commmand: turn on this x10 device x10-object =
 * @event.object.name x10-address =
 * @event.object.address this are resolved according to the parameters in the
 * event that has fired the reaction containing the command 'turn on this x10
 * device' in this case the event can be something like 'object receive click on
 * the GUI' with paramenter object = Light 1 click = SINGLE_CLICK </p>
 *
 * @author Enrico Nicoletti (enrico.nicoletti84@gmail.com)
 */
public final class Resolver {

    // private static final String REFERENCE_DELIMITER = "@";
    private ArrayList<String> prefixes = new ArrayList<String>();
    private Payload context;
    private Reaction reaction;
    private Command command;
    private Trigger trigger;

    /**
     * Creates an empty resolution context
     */
    public Resolver() {
        this.context = new Payload();
    }

    /**
     * Creates a resolved clone of the reaction in input. All commands in the
     * reaction are resolved according to the context given in the contructor.
     *
     * @param r
     * @return a clone of the resolver reaction
     */
    public Reaction resolve(Reaction r) {
        this.reaction = r;
        if ((context != null) && (reaction != null)) {
            Reaction clone = new Reaction(
                    reaction.getTrigger(),
                    performSubstitutionInCommands(reaction.getCommands()));
            return clone;
        }
        return null;
    }

    /**
     * Creates a resolved clone of the command in input according to the current
     * context given in input to the constructor.
     *
     * @param c
     * @return
     */
    public Command resolve(Command c) throws CloneNotSupportedException {
        this.command = c;
        if ((context != null) && (command != null)) {
            Command clone = command.clone();
            mergeContextParamsIntoCommand(clone);
            performSubstitutionInCommand(clone);
            return clone;
        }
        return null;
    }

    /**
     * Creates a resolved clone of the trigger in input according to the current
     * context given in input to the constructor.
     *
     * @param trigger
     * @return
     */
    public Trigger resolve(Trigger t) {
        this.trigger = t;
        if ((context != null) && (trigger != null)) {
            Trigger clone = trigger.clone();
            mergeContextParamsIntoTrigger(clone);
            performSubstitutionInTrigger(clone);
            return clone;
        }
        return null;
    }

    /*
     * makes all commands in a reaction inherits the event parameters. after
     * that resolves the properties of the command eg: if a command properties
     * is: device = @event.device it becomes: device = P01 translating
     * @event.device to the name of the device which has generated the event
     */
    private ArrayList<Command> performSubstitutionInCommands(ArrayList<Command> commands) {
        //clone reaction to not affect the original commands with temporary values
        //construct a cloned reaction
        ArrayList<Command> tmp = new ArrayList<Command>();
        try {
            for (Command originalCommand : commands) {
                //resolving values using context data
                Command clonedCmd = resolve(originalCommand);
                tmp.add(clonedCmd);
            }
        } catch (Exception e) {
            Freedomotic.logger.warning(Freedomotic.getStackTraceInfo(e));
        }
        return tmp;
    }

    /**
     * search in a command attribute for a pattern
     *
     * @event.VARIABLE_NAME and replace it with the real value from event
     * Payload p
     *
     * @param command
     */
    private void performSubstitutionInCommand(Command command) {
        Iterator<Entry<Object, Object>> it = command.getProperties().entrySet().iterator();
        while (it.hasNext()) {
            Entry<Object, Object> aProperty =  it.next();
            String key = (String) aProperty.getKey();
            String propertyValue = (String) aProperty.getValue();
            for (final String PREFIX : prefixes) {
                String regex = "@" + PREFIX + "[.A-Za-z0-9_-]*\\b(#)?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(propertyValue);
                while (matcher.find()) {
                    String occurrence = matcher.group();
                    //re-read the property each loop because is possible the previous loop has already replaced something
                    propertyValue = (String) aProperty.getValue();
                    //System.out.println("La proprieta '" + key + " = " + propertyValue + "' contiene: '" + occurrence + "'");
                    String referenceToResolve = occurrence;
                    if (occurrence.endsWith("#")) {
                        referenceToResolve = referenceToResolve.substring(0, referenceToResolve.length() - 1);  //cutting out the optional last '#'
                    }
                    referenceToResolve = referenceToResolve.substring(1, referenceToResolve.length()); //cutting out the first char '@'
                    String replacer = command.getProperty(referenceToResolve);

                    //Freedomotic.logger.severe("Event variable name '" + eventVarName + "' is substituted with its value '" + replacer + "'");
                    if ((replacer != null && !replacer.isEmpty())) {
                        String propertyValueResolved = propertyValue.replaceFirst(occurrence, replacer);
                        aProperty.setValue(propertyValueResolved);
                    } else {
                        Freedomotic.logger.severe("Variable '" + referenceToResolve
                                + "' cannot be resolved in command '" + command.getName() + "'.\n"
                                + "Availabe tokens are: " + context.toString());
                    }
                }
            }
            //all references are replaced with real values in the current property, now perform scripting
            String possibleScript = (String) aProperty.getValue();
            boolean success = false;
            if (possibleScript.startsWith("=")) {
                //this is a javascript
                try {
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine js = mgr.getEngineByName("JavaScript");
                    String script = possibleScript.substring(1); //removing equal sign on the head
                    if (js == null) {
                        Freedomotic.logger.severe("Cannot instatiate a JavaScript engine");
                    }
//                    ScriptContext cont = js.getContext();
//                    cont.setAttribute("name", "JavaScript",
//                            ScriptContext.ENGINE_SCOPE);
                    try {
                        js.eval(script);
                    } catch (ScriptException scriptException) {
                        Freedomotic.logger.severe(scriptException.getMessage());
                    }
//                    Freedomotic.logger.warning("EXPERIMENTAL: Apply javascript evaluation to: '" + script + "' the complete value is '" + possibleScript);
//                    Freedomotic.logger.warning("EXPERIMENTAL: " + key + " is: '" + js.get(key) + "'");
                    if (js.get(key) == null) {
                        Freedomotic.logger.severe("Script evaluation has returned a null value, maybe the key '" + key + "' is not evaluated properly.");
                    }
                    aProperty.setValue(js.get(key).toString());
                    success = true;
                } catch (Exception ex) {
                    success = false;
                    Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(ex));
                }
            }
            if (!success) {
                aProperty.setValue(possibleScript);
            }
        }
    }

    /**
     * search in a trigger attribute for a pattern
     *
     * @event.VARIABLE_NAME and replace it with the real value from event
     * Payload p
     *
     * @param trigger
     */
    private void performSubstitutionInTrigger(Trigger trigger) {
        Iterator<Statement> it = trigger.getPayload().iterator();
        while (it.hasNext()) {
            Statement statement = it.next();
            String key = (String) statement.getAttribute();
            String propertyValue = (String) statement.getValue();
            for (final String PREFIX : prefixes) {
                String regex = "@" + PREFIX + "[.A-Za-z0-9_-]*\\b(#)?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(propertyValue);
                while (matcher.find()) {
                    String occurrence = matcher.group();
                    propertyValue = (String) statement.getValue();
                    String referenceToResolve = occurrence;
                    if (occurrence.endsWith("#")) {
                        referenceToResolve = referenceToResolve.substring(0, referenceToResolve.length() - 1);  //cutting out the optional last '#'
                    }
                    referenceToResolve = referenceToResolve.substring(1, referenceToResolve.length()); //cutting out the first char '@'
                    for (Statement st : trigger.getPayload().getStatements(referenceToResolve)) {
                        String replacer = st.getValue();
                        if ((replacer == null ? "" != null : !replacer.isEmpty())) {
                            //first occurrence is replaced with values and then the system try to resolve math operations
                            String propertyValueResolved = propertyValue.replaceFirst(occurrence, replacer);
                            statement.setValue(propertyValueResolved);
                        } else {
                            Freedomotic.logger.severe("Variable '" + referenceToResolve
                                    + "' cannot be resolved in trigger '" + trigger.getName() + "'.\n"
                                    + "Availabe tokens are: " + context.toString());
                        }
                    }
                }
            }
            //all references are replaced with real values in the current statement, now perform scripting
            String possibleScript = (String) statement.getValue().trim();
            boolean success = false;
            if (possibleScript.startsWith("=")) {
                //this is a javascript
                try {
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine js = mgr.getEngineByName("JavaScript");
                    String script = possibleScript.substring(1); //removing equal sign on the head
                    if (js == null) {
                        Freedomotic.logger.severe("Cannot instatiate a JavaScript engine");
                    }
                    try {
                        js.eval(script);
                    } catch (ScriptException scriptException) {
                        Freedomotic.logger.severe(scriptException.getMessage());
                    }
                    //Freedomotic.logger.warning("EXPERIMENTAL: Apply javascript evaluation to trigger value: '" + script + "' the complete value is '" + possibleScript);
                    //Freedomotic.logger.warning("EXPERIMENTAL: " + key + " is: '" + js.get(key) + "'");
                    if (js.get(key) == null) {
                        Freedomotic.logger.severe("Script evaluation in trigger '" + trigger.getName() + "' has returned a null value, maybe the key '" + key + "' is not evaluated properly.");
                    }
                    statement.setValue(js.get(key).toString());
                    success = true;
                } catch (Exception ex) {
                    success = false;
                    Freedomotic.logger.severe(ex.getMessage());
                }
            }
            if (!success) {
                statement.setValue(possibleScript); //fall back to the value before scripting evaluation
            }
        }
    }

    private void mergeContextParamsIntoCommand(Command c) {
        //adding  parameters to command parameters with a  prefix
        Iterator<Statement> it = context.iterator();
        while (it.hasNext()) {
            Statement statement = it.next();
            c.setProperty(statement.getAttribute(), statement.getValue());
        }
    }

    private void mergeContextParamsIntoTrigger(Trigger t) {
        //adding  parameters to command parameters with a  prefix
        t.getPayload().merge(context);
    }

    public void addContext(final String PREFIX, final Config aContext) {
        if (context == null) {
            context = new Payload();
        }
        //registering the new prefix
        if (!prefixes.contains(PREFIX)) {
            prefixes.add(PREFIX);
        }
        Set<Entry<Object,Object>> entries = aContext.getProperties().entrySet();
        Iterator<Entry<Object,Object>> it = entries.iterator();
        while (it.hasNext()) {
            String key;
            Entry<Object,Object> entry = it.next();
            //removing the prefix of the properties if already exists
            //to avoid dublicate prefixes like @event.event.object.name
            if (entry.getKey().toString().startsWith(PREFIX)) {
                key = entry.getKey().toString().substring(PREFIX.length());
            } else {
                key = entry.getKey().toString();
            }
            //System.out.println("    statement " + PREFIX + key + "=" + entry.getValue().toString());
            context.addStatement(PREFIX + key, entry.getValue().toString());
        }
    }

    public void addContext(final String PREFIX, final Map<String,String> aContext) {
        if (context == null) {
            context = new Payload();
        }
        //registering the new prefix
        if (!prefixes.contains(PREFIX)) {
            prefixes.add(PREFIX);
        }
        Iterator<Entry<String,String>> it = aContext.entrySet().iterator();
        while (it.hasNext()) {
            String key;
            Entry<String,String> entry = it.next();
            //removing the prefix of the properties if already exists
            //to avoid duplicate prefixes like @event.event.object.name
            if (entry.getKey().toString().startsWith(PREFIX)) {
                key = entry.getKey().toString().substring(PREFIX.length());
            } else {
                key = entry.getKey().toString();
            }
            //System.out.println("    statement " + PREFIX + key + "=" + entry.getValue().toString());
            context.addStatement(PREFIX + key, entry.getValue().toString());
        }
    }

    public void addContext(final String PREFIX, final Payload aContext) {
        if (context == null) {
            context = new Payload();
        }
        //registering the new prefix
        if (!prefixes.contains(PREFIX)) {
            prefixes.add(PREFIX);
        }
        Iterator<Statement> it = aContext.iterator();
        while (it.hasNext()) {
            String key;
            Statement statement = it.next();
            //removing the prefix of the properties if already exists
            //to avoid dublicate prefixes like @event.event.object.name
            if (statement.getAttribute().startsWith(PREFIX)) {
                key = statement.getAttribute().substring(PREFIX.length());
            } else {
                key = statement.getAttribute().toString();
            }
            context.addStatement(PREFIX + key, statement.getValue());
        }
    }

    void clear() {
        prefixes.clear();
        context.clear();
        reaction = null;
        command = null;
        trigger = null;
    }
}
