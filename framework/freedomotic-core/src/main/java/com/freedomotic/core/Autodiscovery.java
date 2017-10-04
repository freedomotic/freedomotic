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
package com.freedomotic.core;

import java.io.File;
import java.util.List;
import java.util.Map;
import com.freedomotic.reactions.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomotic.api.AbstractConsumer;
import com.freedomotic.api.Client;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.bus.BusService;
import com.freedomotic.exceptions.FreedomoticRuntimeException;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.object.Behavior;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ObjectPluginPlaceholder;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;

/**
 * Creates Things on the fly using a template as a starting point. It can be
 * created from a Command or by calling the
 * {@link #join(String clazz, String name, String protocol, String address, boolean allowClones)}
 * method. This method spawns a new instance overriding template properties like
 * name, protocol and address. Moreover, it can load the hardware triggers and
 * commands mapping from the tuples defined in the plugin manifest.
 *
 * @author Enrico Nicoletti
 */
public final class Autodiscovery extends AbstractConsumer {

	/**
	 * Message queue channel name
	 */
	private static final String MESSAGING_CHANNEL = "app.objects.create";

    /**
     * Class logger
     */
	private static final Logger LOG = LoggerFactory.getLogger(Autodiscovery.class.getName());

	// Dependencies
	private final ClientStorage clientStorage;
	private final ThingRepository thingsRepository;
	private final TriggerRepository triggerRepository;
	private final CommandRepository commandRepository;

	@Inject
	Autodiscovery(ClientStorage clientStorage, ThingRepository thingsRepository, TriggerRepository triggerRepository,
			CommandRepository commandRepository, BusService busService) {
		super(busService);
		this.clientStorage = clientStorage;
		this.thingsRepository = thingsRepository;
		this.triggerRepository = triggerRepository;
		this.commandRepository = commandRepository;
	}

	/**
	 * Gets {@link #MESSAGING_CHANNEL} string constant
	 * @return messaging channel string constant
	 */
	@Override
	public String getMessagingChannel() {
		return MESSAGING_CHANNEL;
	}

    /**
     * Gets properties from command parameter and calls {@link #join}
     * @param command contains needed properties for parameters to call <code>join</code>
     *                method
     */
	@Override
	protected void onCommand(Command command) {
		String name = command.getProperty("object.name");
		String protocol = command.getProperty("object.protocol");
		String address = command.getProperty("object.address");
		String clazz = command.getProperty("object.class");
		// Creates a Thing also if one with the same name exists, true by
		// default
		boolean allowClones = command.getBooleanProperty("autodiscovery.allow-clones", true);

		try {
			join(clazz, name, protocol, address, allowClones);
		} catch (RepositoryException ex) {
			LOG.error(Freedomotic.getStackTraceInfo(ex));
		}
	}

	/**
	 * Creates an {@link EnvObjectLogic} with the specification in input. If an
	 * {@link EnvObjectLogic} with the same protocol and address already exists
	 * it will exit with no changes.
	 *
	 * @param clazz
	 *            the name of the thing template to load (eg: Light)
	 * @param name
	 *            the name of the thing
	 * @param protocol
	 *            the protocol which drives the thing
	 * @param address
	 *            the address to uniquely identify the thing
	 * @param allowClones
	 *            determines if an existent template can be cloned by adding an
	 *            ordinal number to its name, true by default
	 * @return the thing created
	 * @throws com.freedomotic.exceptions.RepositoryException
	 *             if it is not possible to retrieve the requested information
	 */
	protected EnvObjectLogic join(String clazz, String name, String protocol, String address, boolean allowClones)
			throws RepositoryException {
		// Check if autodiscovery can be applied
		if (thingAlreadyExists(protocol, address)) {
			LOG.info(
					"A thing with protocol \"{}\" and address \"{}\" already exists in the environment. Autodiscovery exits without changes",
					protocol, address);
			return null;
		}

		// If not allowed to clone an Thing
		if (!allowClones && thingAlreadyExists(name)) {
			LOG.info(
					"A thing with name \"{}\" already exists in the environment. Autodiscovery exits without changes because property 'autodiscovery.allow-clones' property is ''{}'' for the received command",
					name, false);
			return null;
		}

		// Check if the requested Thing template is loaded
		ObjectPluginPlaceholder thingTemplate = (ObjectPluginPlaceholder) clientStorage.get(clazz);
		if (thingTemplate == null) {
			LOG.warn("Autodiscovery error: doesn't exist an object class called \"{}\"", clazz);
			return null;
		}

		// Start the new Thing creation
		LOG.warn("Autodiscovery request for an object called \"{}\" of type \"{}\"", name, clazz);
		File templateFile = thingTemplate.getTemplate();
		EnvObjectLogic loaded = thingsRepository.load(templateFile);

		// changing the name and other properties invalidates related trigger
		// and commands
		// call init() again after this changes
		if ((name != null) && !name.isEmpty()) {
			loaded.getPojo().setName(name);
		} else {
			loaded.getPojo().setName(protocol);
		}

		loaded = thingsRepository.copy(loaded);
		loaded.getPojo().setProtocol(protocol);
		loaded.getPojo().setPhisicalAddress(address);
		// Remove the 'virtual' tag and any other actAs configuration.
		// TODO: it would be better to remove the actAs property and manage all
		// with tags
		loaded.getPojo().setActAs("");
		loaded.setRandomLocation();
		configureOptionalMapping(protocol, clazz, loaded);
		LOG.info("Autodiscovery adds a thing called \"{}\" of type \"{}\"", loaded.getPojo().getName(), clazz);
		return loaded;
	}

	/**
	 * Sets the PREFERRED MAPPING of the protocol plugin, if any is defined in
	 * its manifest. The mapping information will be asked to the plugin which
	 * implements the same protocol as the one specified in the arguments of
	 * this method.
	 * <p>
	 * The clazz parameter is used to identify a tuple in the plugin manifest.
	 * The configuration specified in this tuple will be applied to the target
	 * Thing.
	 *
	 * @param protocol
	 *            the thing protocol, used to retrieve the corresponding plugin
	 * @param clazz
	 *            the dot notation taxonomy used to identify the mapping that
	 *            should be applied (eg: EnvObject.ElectricDevice.Light)
	 * @param loaded
	 *            the target things
	 */
	private void configureOptionalMapping(String protocol, String clazz, EnvObjectLogic loaded) {
		Client addon = clientStorage.getClientByProtocol(protocol);

		if ((addon != null) && (addon.getConfiguration().getTuples() != null)) {
			for (int i = 0; i < addon.getConfiguration().getTuples().size(); i++) {
				Map tuple = addon.getConfiguration().getTuples().getTuple(i);
				String regex = (String) tuple.get("object.class");

				if ((regex != null) && clazz.matches(regex)) {
					this.mapBehaviorsToHWTriggers(tuple, loaded);
					this.setActions(tuple, loaded);
				}
			}
		}
	}

    /**
     * Maps object behaviors to hardware triggers by calling {@link EnvObjectLogic#addTriggerMapping(Trigger, String)}
     * @param tuple Object with trigger name
     * @param environmentLogic Environment logic on which to map trigger and behavior
     */
	private void mapBehaviorsToHWTriggers(Map tuple, EnvObjectLogic environmentLogic) {
		for (Behavior behavior : environmentLogic.getPojo().getBehaviors()) {
			String triggerName = (String) tuple.get(behavior.getName());
			if (triggerName != null) {
				environmentLogic.addTriggerMapping(triggerRepository.findByName(triggerName).get(0), behavior.getName());
			}
		}
	}

    /**
     * Calls {@link EnvObjectLogic#setAction(String, Command)} with command name from <code>tuple</code>
     * @param tuple Object with action names
     * @param environmentLogic Environment logic on which to set actions
     */
	private void setActions(Map tuple, EnvObjectLogic environmentLogic) {
		for (String action : environmentLogic.getPojo().getActions().stringPropertyNames()) {
			String commandName = (String) tuple.get(action);
			if (commandName != null) {
				List<Command> list = commandRepository.findByName(commandName);
				if (!list.isEmpty()) {
					environmentLogic.setAction(action, list.get(0));
				} else {
					throw new FreedomoticRuntimeException("No commands found with name " + commandName);
				}
			}
		}
	}

    /**
     * Checks if object already exists in <code>ThingRepository</code>
     * @param protocol Object protocol search criteria
     * @param address Object address search criteria
     * @return true if object already exists in repository
     */
	private boolean thingAlreadyExists(String protocol, String address) {
		// Multiple object with protocol 'unknown' are allowed
		return !protocol.trim().equalsIgnoreCase("unknown") && (thingsRepository.findByAddress(protocol, address) != null);
	}

    /**
     * Checks if object already exists in <code>ThingRepository</code>
     * @param name Object name search criteria
     * @return true if object already exists in repository
     */
	private boolean thingAlreadyExists(String name) {
		return !thingsRepository.findByName(name).isEmpty();
	}

    /**
     * Guaranteed to throw an exception
     *
     * @throws UnsupportedOperationException always
     * @deprecated Unsupported operation.
     */
	@Override
	protected void onEvent(EventTemplate event) {
		throw new UnsupportedOperationException("Autodiscovery module is not supposed to receive events");
	}
}
