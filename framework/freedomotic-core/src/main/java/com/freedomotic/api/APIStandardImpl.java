/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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
package com.freedomotic.api;

import com.freedomotic.settings.AppConfig;
import com.freedomotic.bus.BusService;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.security.Auth;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * Implements the standard Freedomotic APIs available to plugins. This class
 * returns only unmodifiable collections, so the returned collections are just a
 * read-only view of current underlying data. This data are not immutable
 * themselves, but they are immutable through the references retrieved from the
 * methods of this class.
 *
 * @author Enrico Nicoletti
 */
class APIStandardImpl implements API {

    private final EnvironmentRepository environments;
    private final ThingRepository things;
    private final ClientStorage clientStorage;
    private final AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    private final PluginsManager plugManager;
    private TriggerRepository triggers;
    private CommandRepository commands;
    private ReactionRepository reactions;
    private final ThingFactory thingsFactory;
    private NlpCommand nlpCommands;
    private final BusService busService;

    /**
     *
     * @param environment
     * @param things
     * @param clientStorage
     * @param config
     * @param auth
     * @param i18n
     * @param plugManager
     * @param triggerPersistence
     * @param commands
     */
    @Inject
    public APIStandardImpl(
            BusService busService,
            EnvironmentRepository environment,
            ThingRepository things,
            ThingFactory thingsFactory,
            ClientStorage clientStorage,
            AppConfig config,
            Auth auth,
            I18n i18n,
            PluginsManager plugManager,
            TriggerRepository triggerPersistence,
            CommandRepository commands,
            ReactionRepository reactions,
            NlpCommand nlpCommands) {
        this.environments = environment;
        this.things = things;
        this.clientStorage = clientStorage;
        this.config = config;
        this.auth = auth;
        this.i18n = i18n;
        this.plugManager = plugManager;
        this.triggers = triggerPersistence;
        this.commands = commands;
        this.reactions = reactions;
        this.thingsFactory = thingsFactory;
        this.nlpCommands = nlpCommands;
        this.busService = busService;
    }

    /**
     *
     * @return
     */
    @Override
    public AppConfig getConfig() {
        return config;
    }

    /**
     *
     * @param filter
     * @return
     */
    @Override
    public Collection<Client> getClients(String filter) {
        return clientStorage.getClients(filter);
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<Client> getClients() {
        return clientStorage.getClients();
    }

    @Override
    public BufferedImage getResource(String resourceIdentifier) {
        return ResourcesManager.getResource(resourceIdentifier);
    }

    /**
     *
     * @return
     */
    @Override
    public ClientStorage getClientStorage() {
        return clientStorage;
    }

    /**
     *
     * @return
     */
    @Override
    public Auth getAuth() {
        return auth;
    }

    /**
     *
     * @return
     */
    @Override
    public I18n getI18n() {
        return i18n;
    }

    /**
     *
     * @return
     */
    @Override
    public PluginsManager getPluginManager() {
        return plugManager;
    }

    @Override
    public EnvironmentRepository environments() {
        return environments;
    }

    @Override
    public TriggerRepository triggers() {
        return triggers;
    }

    @Override
    public ThingRepository things() {
        return things;
    }

    @Override
    public CommandRepository commands() {
        return commands;
    }

    @Override
    public ReactionRepository reactions() {
        return reactions;
    }

    @Override
    public ThingFactory thingsFactory() {
        return thingsFactory;
    }

    @Override
    public NlpCommand nlpCommands() {
        return nlpCommands;
    }

    @Override
    public BusService bus() {
        return busService;
    }

}
