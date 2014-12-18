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

import com.freedomotic.api.Client;
import com.freedomotic.bus.BusConsumer;
import com.freedomotic.bus.BusMessagesListener;
import com.freedomotic.bus.BusService;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.things.EnvObjectLogic;
import com.google.inject.Inject;
import java.io.File;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

/**
 *
 * @author enrico
 */
public final class Autodiscovery implements BusConsumer {

    private static final String MESSAGING_CHANNEL = "app.objects.create";

    private static BusMessagesListener listener;
    private static final Logger LOG = Logger.getLogger(Autodiscovery.class.getName());

    // Dependencies
    private final ClientStorage clientStorage;
    private final BusService busService;
    private final ThingRepository thingsRepository;

    @Inject
    Autodiscovery(ClientStorage clientStorage, ThingRepository thingsRepository, BusService busService) {
        this.clientStorage = clientStorage;
        this.thingsRepository = thingsRepository;
        this.busService = busService;
        register();
    }

    static String getMessagingChannel() {
        return MESSAGING_CHANNEL;
    }

    /**
     * Register one or more channels to listen to
     */
    private void register() {
        listener = new BusMessagesListener(this, busService);
        listener.consumeCommandFrom(getMessagingChannel());
    }

    @Override
    public void onMessage(ObjectMessage message) {
        try {
            Object jmsObject = message.getObject();

            if (jmsObject instanceof Command) {
                Command command = (Command) jmsObject;
                String name = command.getProperty("object.name");
                String protocol = command.getProperty("object.protocol");
                String address = command.getProperty("object.address");
                String clazz = command.getProperty("object.class");

                if (thingsRepository.findByAddress(protocol, address).isEmpty()) {
                    join(clazz, name, protocol, address);
                }
            }
        } catch (JMSException ex) {
            Logger.getLogger(Autodiscovery.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param clazz
     * @param name
     * @param protocol
     * @param address
     * @return
     */
    protected EnvObjectLogic join(String clazz, String name, String protocol, String address) {
        EnvObjectLogic loaded = null;
        ObjectPluginPlaceholder objectPlugin = (ObjectPluginPlaceholder) clientStorage.get(clazz);

        if (objectPlugin == null) {
            LOG.log(Level.WARNING, "Autodiscovery error: doesn''t exist an object class called {0}", clazz);
            return null;
        }
        
        LOG.log(Level.WARNING, "Autodiscovery request for an object called ''{0}'' of type ''{1}''", new Object[]{name, clazz});

        File templateFile = objectPlugin.getTemplate();
        try {
            loaded = thingsRepository.load(templateFile);
        } catch (RepositoryException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
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

        //set the PREFERRED MAPPING of the protocol plugin (if any is defined in its manifest)
        Client addon = clientStorage.getClientByProtocol(protocol);

        if (addon != null) {
            for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
                Map tuple = addon.getConfiguration().getTuples().getTuple(i);
                String regex = (String) tuple.get("object.class");

                if ((regex != null) && clazz.matches(regex)) {
                    //map object behaviors to hardware triggers
                    for (Behavior behavior : loaded.getPojo().getBehaviors()) {
                        String triggerName = (String) tuple.get(behavior.getName());
                        if (triggerName != null) {
                            loaded.addTriggerMapping(TriggerPersistence.getTrigger(triggerName),
                                    behavior.getName());
                        }
                    }

                    for (String action : loaded.getPojo().getActions().stringPropertyNames()) {
                        String commandName = (String) tuple.get(action);

                        if (commandName != null) {
                            loaded.setAction(action,
                                    CommandPersistence.getHardwareCommand(commandName));
                        }
                    }
                }
            }
        }
        LOG.log(Level.INFO, "Autodiscovery adds a thing called ''{0}'' of type ''{1}''", 
                new Object[]{loaded.getPojo().getName(), clazz});
        return loaded;
    }
}
