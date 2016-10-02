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
package com.freedomotic.core;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.reactions.Command;
import com.freedomotic.rules.Payload;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
 * <p>
 * This class takes a list of properties in form key=value and propagates this
 * list to all commands in a given reaction. After that this list (called
 * context) is used to resolve the references to external values in the command.
 * For example Commmand: turn on this x10 device x10-object =
 * @event.object.name x10-address =
 * @event.object.address this are resolved according to the parameters in the
 * event that has fired the reaction containing the command 'turn on this x10
 * device' in this case the event can be something like 'object receive click on
 * the GUI' with paramenter object = Light 1 click = SINGLE_CLICK </p>
 *
 * @author Enrico Nicoletti
 */
public final class Resolver {

    // private static final String REFERENCE_DELIMITER = "@";
    private static final Logger LOG = LoggerFactory.getLogger(Resolver.class.getName());
    private List<String> namespaces = new ArrayList<String>();
    private Payload context;

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
     * @param reaction
     * @return a clone of the resolver reaction
     */
    public Reaction resolve(Reaction reaction) {

        if ((context != null) && (reaction != null)) {
            Reaction clone
                    = new Reaction(reaction.getTrigger(),
                            performSubstitutionInCommands(reaction.getCommands()));

            return clone;
        }

        return null;
    }

    /**
     * Creates a resolved clone of the command in input according to the current
     * context given in input to the constructor.
     *
     * @param command
     * @return
     * @throws java.lang.CloneNotSupportedException
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    public Command resolve(Command command)
            throws CloneNotSupportedException, VariableResolutionException {

        if ((context != null) && (command != null)) {
            Command clone = command.clone();
            mergeContextParamsIntoCommand(clone);
            performSubstitutionInCommand(clone);
            this.clear();
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
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    public Trigger resolve(Trigger trigger) throws VariableResolutionException {

        if ((context != null) && (trigger != null)) {
            Trigger clone = trigger.clone();
            mergeContextParamsIntoTrigger(clone);
            performSubstitutionInTrigger(clone);
            this.clear();
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
    private List<Command> performSubstitutionInCommands(List<Command> commands) {
        //clone reaction to not affect the original commands with temporary values
        //construct a cloned reaction
        List<Command> tmp = new ArrayList<Command>();

        try {
            for (Command originalCommand : commands) {
                //resolving values using context data
                Command clonedCmd = resolve(originalCommand);
                tmp.add(clonedCmd);
            }
        } catch (Exception e) {
            LOG.error("Error while substituting variables", e);
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
    private void performSubstitutionInCommand(Command command) throws VariableResolutionException {
        for (Map.Entry aProperty : command.getProperties().entrySet()) {
            String key = (String) aProperty.getKey();
            String propertyValue = (String) aProperty.getValue();

            for (final String namespace : namespaces) {
                String regex = "@" + namespace + "[.A-Za-z0-9_-]*\\b(#)?";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(propertyValue);

                while (matcher.find()) {
                    String occurrence = matcher.group();
                    //re-read the property each loop because is possible the previous loop has already replaced something
                    propertyValue = (String) aProperty.getValue();

                    String referenceToResolve = occurrence;

                    if (occurrence.endsWith("#")) {
                        //cutting out the optional last '#'
                        referenceToResolve = referenceToResolve.substring(0, referenceToResolve.length() - 1);
                    }

                    //cutting out the first char '@'
                    referenceToResolve
                            = referenceToResolve.substring(1,
                                    referenceToResolve.length());

                    String replacer = command.getProperty(referenceToResolve);
                    if (((replacer != null) && !replacer.isEmpty())) {
                        String propertyValueResolved = propertyValue.replaceFirst(occurrence, replacer);
                        aProperty.setValue(propertyValueResolved);
                    } else {
                        throw new VariableResolutionException("Variable '" + referenceToResolve
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
                        LOG.error("Cannot instantiate a JavaScript engine");
                    }

                    try {
                        js.eval(script);
                    } catch (ScriptException scriptException) {
                        LOG.error(scriptException.getMessage());
                    }

                    if (js.get(key) == null) {
                        LOG.error(
                                "Script evaluation has returned a null value, maybe the key ''{}'' is not evaluated properly.",
                                key);
                    }

                    aProperty.setValue(js.get(key).toString());
                    success = true;
                } catch (Exception ex) {
                    success = false;
                    LOG.error("Error while evaluating script in command", ex);
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
    private void performSubstitutionInTrigger(Trigger trigger) throws VariableResolutionException {
        Iterator it = trigger.getPayload().iterator();

        while (it.hasNext()) {
            Statement statement = (Statement) it.next();
            String key = (String) statement.getAttribute();
            String propertyValue = (String) statement.getValue();

            for (final String PREFIX : namespaces) {
                Pattern pattern = Pattern.compile("@" + PREFIX + "[.A-Za-z0-9_-]*\\b(#)?"); //find any @token
                Matcher matcher = pattern.matcher(propertyValue);
                StringBuffer result = new StringBuffer();

                while (matcher.find()) {
                    matcher.appendReplacement(result, "");

                    String tokenKey = matcher.group();

                    if (tokenKey.endsWith("#")) {
                        tokenKey = tokenKey.substring(0, tokenKey.length() - 1); //cutting out the optional last '#'
                    }

                    tokenKey
                            = tokenKey.substring(1,
                                    tokenKey.length()); //cutting out the first char '@'

                    String tokenValue = trigger.getPayload().getStatementValue(tokenKey);

                    if (tokenValue == null) {
                        throw new VariableResolutionException("Variable '" + tokenValue + "' cannot be resolved in trigger '"
                                + trigger.getName() + "'.\n" + "Availabe tokens are: "
                                + context.toString());
                    }

                    //replace an @token.property with its real value
                    //System.out.println("Replace all " + tokenKey + " with " + tokenValue + " in " + propertyValue);
                    result.append(tokenValue);
                }

                matcher.appendTail(result);
                statement.setValue(result.toString());
            }

            //all references are replaced with real values in the current statement, now perform scripting
            String possibleScript = (String) statement.getValue().trim();
            boolean success = false;

            if (possibleScript.startsWith("=")) {
                //this is a javascript
                try {
                    ScriptEngineManager mgr = new ScriptEngineManager();
                    ScriptEngine js = mgr.getEngineByName("JavaScript");
                    //removing equal sign on the head
                    String script = possibleScript.substring(1);

                    if (js == null) {
                        LOG.error("Cannot instatiate a JavaScript engine");
                    }

                    try {
                        js.eval(script);
                    } catch (ScriptException scriptException) {
                        LOG.error(scriptException.getMessage());
                    }

                    if (js.get(key) == null) {
                        LOG.error(
                                "Script evaluation in trigger ''{}'' has returned a null value, maybe the key ''{}'' is not evaluated properly.",
                                new Object[]{trigger.getName(), key});
                    }

                    statement.setValue(js.get(key).toString());
                    success = true;
                } catch (Exception ex) {
                    success = false;
                    LOG.error(ex.getMessage());
                }
            }

            if (!success) {
                //fall back to the value before scripting evaluation
                statement.setValue(possibleScript);
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

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    public void addContext(final String PREFIX, final Config aContext) {
        if (context == null) {
            context = new Payload();
        }

        //registering the new prefix
        if (!namespaces.contains(PREFIX)) {
            namespaces.add(PREFIX);
        }

        Set entries = aContext.getProperties().entrySet();
        Iterator it = entries.iterator();

        while (it.hasNext()) {
            String key;
            Map.Entry entry = (Map.Entry) it.next();

            //removing the prefix of the properties if already exists
            //to avoid dublicate prefixes like @event.event.object.name
            if (entry.getKey().toString().startsWith(PREFIX)) {
                key = entry.getKey().toString().substring(PREFIX.length());
            } else {
                key = entry.getKey().toString();
            }

            //System.out.println("    statement " + PREFIX + key + "=" + entry.getValue().toString());
            context.addStatement(PREFIX + key,
                    entry.getValue().toString());
        }
    }

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    public void addContext(final String PREFIX, final Map<String, String> aContext) {
        if (context == null) {
            context = new Payload();
        }

        //registering the new prefix
        if (!namespaces.contains(PREFIX)) {
            namespaces.add(PREFIX);
        }

        Iterator it = aContext.entrySet().iterator();

        while (it.hasNext()) {
            String key;
            Entry entry = (Entry) it.next();

            //removing the prefix of the properties if already exists
            //to avoid duplicate prefixes like @event.event.object.name
            if (entry.getKey().toString().startsWith(PREFIX)) {
                key = entry.getKey().toString().substring(PREFIX.length());
            } else {
                key = entry.getKey().toString();
            }

            //System.out.println("    statement " + PREFIX + key + "=" + entry.getValue().toString());
            context.addStatement(PREFIX + key,
                    entry.getValue().toString());
        }
    }

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    public void addContext(final String PREFIX, final Payload aContext) {
        if (context == null) {
            context = new Payload();
        }

        //registering the new prefix
        if (!namespaces.contains(PREFIX)) {
            namespaces.add(PREFIX);
        }

        // get an hold on the statements list mutex to avoid others to use it
        synchronized (aContext.getStatements()) {
            Iterator it = aContext.iterator();

            while (it.hasNext()) {
                String key;
                Statement statement = (Statement) it.next();
                //removing the prefix of the properties if already exists
                //to avoid dublicate prefixes like @event.event.object.name
                if (statement.getAttribute().startsWith(PREFIX)) {
                    key = statement.getAttribute().substring(PREFIX.length());
                } else {
                    key = statement.getAttribute();
                }
                context.addStatement(PREFIX + key, statement.getValue());
            }
        }
    }

    void clear() {
        namespaces.clear();
        context.clear();
    }
}
