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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.api.AbstractConsumer;
import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.bus.BusService;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.things.EnvObjectLogic;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Create Things on the fly using a template as a starting point. It can be
 * created from a Command or by calling the
 * {@link #join(String clazz, String name, String protocol, String address, boolean allowClones)}
 * method. This module spawns a new instance overriding template properties like
 * name, protocol and address. Moreover it can load the hardware trigger and
 * commands mapping from the tuples defined plugin manifest
 *
 * @author enrico
 */
public final class Autodiscovery extends AbstractConsumer {

    private static final String MESSAGING_CHANNEL = "app.objects.create";
    private static final Logger LOG = Logger.getLogger(Autodiscovery.class.getName());

    // Dependencies
    private final ClientStorage clientStorage;
    private final ThingRepository thingsRepository;
    private final TriggerRepository triggerRepository;
    private final CommandRepository commandRepository;

    @Inject
    Autodiscovery(ClientStorage clientStorage,
            ThingRepository thingsRepository,
            TriggerRepository triggerRepository,
            CommandRepository commandRepository,
            BusService busService) {
        super(busService);
        this.clientStorage = clientStorage;
        this.thingsRepository = thingsRepository;
        this.triggerRepository = triggerRepository;
        this.commandRepository = commandRepository;
    }

    @Override
    public String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

    @Override
    protected void onCommand(Command command) throws IOException, UnableToExecuteException {
        String name = command.getProperty("object.name");
        String protocol = command.getProperty("object.protocol");
        String address = command.getProperty("object.address");
        String clazz = command.getProperty("object.class");
        // Creates a Thing also if one with the same name exists, true by default
        boolean allowClones = command.getBooleanProperty("autodiscovery.allow-clones", true);

        try {
            join(clazz, name, protocol, address, allowClones);
        } catch (RepositoryException ex) {
            Logger.getLogger(Autodiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Creates a {@link EnvObjectLogic} with the specification in input.If a
     * {@link EnvObjectLogic} with the same protocol and address already exists
     * it will exits with no changes.
     *
     * @param clazz The name of the Thing template to load (eg: Light)
     * @param name The name of the Thing
     * @param protocol The protocol which drives the Thing
     * @param address The address to uniquely identify the Thing
     * @param allowClones determines if can clone an existent template adding an
     * ordinal number to its name, true by default
     * @return The thing that is created
     * @throws com.freedomotic.exceptions.RepositoryException if it's not
     * possible to retrieve the requested Thing information
     */
    protected EnvObjectLogic join(String clazz, String name, String protocol, String address, boolean allowClones) throws RepositoryException {
        // Check if autodiscovery can be applied
        if (thingAlreadyExists(protocol, address)) {
            LOG.log(Level.INFO, "A thing with protocol ''{0}'' and address ''{1}'' "
                    + "already exists in the environment. "
                    + "Autodiscovery exists without changes", new Object[]{protocol, address});
            return null;
        }

        // If not allowed to clone an Thing
        if (!allowClones && thingAlreadyExists(name)) {
            LOG.log(Level.INFO, "A thing with name ''{0}'' already exists in the environment. "
                    + "Autodiscovery exists without changes because property "
                    + "'autodiscovery.allow-clones' property is "
                    + allowClones + " for the received command", name);
            return null;
        }

        // Check if the requested Thing template is loaded
        ObjectPluginPlaceholder thingTemplate = (ObjectPluginPlaceholder) clientStorage.get(clazz);
        if (thingTemplate == null) {
            LOG.log(Level.WARNING, "Autodiscovery error: doesn't exist an object class called ''{0}''", clazz);
            return null;
        }

        // Start the new Thing creation
        LOG.log(Level.WARNING, "Autodiscovery request for an object called ''{0}'' of type ''{1}''", new Object[]{name, clazz});
        File templateFile = thingTemplate.getTemplate();
        EnvObjectLogic loaded = thingsRepository.load(templateFile);

        //changing the name and other properties invalidates related trigger and commands
        //call init() again after this changes
        if ((name != null) && !name.isEmpty()) {
            loaded.getPojo().setName(name);
        } else {
            loaded.getPojo().setName(protocol);
        }

        loaded = thingsRepository.copy(loaded);
        loaded.getPojo().setProtocol(protocol);
        loaded.getPojo().setPhisicalAddress(address);
        // Remove the 'virtual' tag and any other actAs configuration. 
        //TODO: it would be better to remove the actAs property and manage all with tags
        loaded.getPojo().setActAs("");
        loaded.setRandomLocation();
        configureOptionalMapping(protocol, clazz, loaded);
        LOG.log(Level.INFO, "Autodiscovery adds a thing called ''{0}'' of type ''{1}''",
                new Object[]{loaded.getPojo().getName(), clazz});
        return loaded;
    }

    /**
     * Sets the PREFERRED MAPPING of the protocol plugin, if any is defined in
     * its manifest. The mapping information will be asked to the plugin which
     * implements the same protocol as the one specified in the arguments of
     * this method.
     *
     * The clazz argument is used to identify a tuple in the plugin manifest.
     * The configuration specified in this tuple will be applied to the target
     * Thing.
     *
     * @param protocol The Thing protocol, used to retrieve the corresponding
     * plugin
     * @param clazz The dot notation taxonomy used to identify the mapping that
     * should be applied (eg: EnvObject.ElectricDevice.Light)
     * @param loaded The target thing
     * @throws RuntimeException
     */
    private void configureOptionalMapping(String protocol, String clazz, EnvObjectLogic loaded) throws RuntimeException {
        Client addon = clientStorage.getClientByProtocol(protocol);

        if ((addon != null) && (addon.getConfiguration().getTuples() != null)) {
            for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
                Map tuple = addon.getConfiguration().getTuples().getTuple(i);
                String regex = (String) tuple.get("object.class");

                if ((regex != null) && clazz.matches(regex)) {
                    //map object behaviors to hardware triggers
                    for (Behavior behavior : loaded.getPojo().getBehaviors()) {
                        String triggerName = (String) tuple.get(behavior.getName());
                        if (triggerName != null) {
                            loaded.addTriggerMapping(triggerRepository.findByName(triggerName).get(0),
                                    behavior.getName());
                        }
                    }

                    for (String action : loaded.getPojo().getActions().stringPropertyNames()) {
                        String commandName = (String) tuple.get(action);

                        if (commandName != null) {
                            List<Command> list = commandRepository.findByName(commandName);
                            if (list.isEmpty()) {
                                loaded.setAction(action, list.get(0));
                            } else {
                                throw new RuntimeException("No commands found with name " + commandName);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean thingAlreadyExists(String protocol, String address) {
        if (protocol.trim().equalsIgnoreCase("unknown")) {
            return false; //Multiple object with protocol 'unknown' are allowed
        }
        return !thingsRepository.findByAddress(protocol, address).isEmpty();
    }

    private boolean thingAlreadyExists(String name) {
        return !thingsRepository.findByName(name).isEmpty();
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Autodiscovery module is not supposed to receive events");
    }
}
