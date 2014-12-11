/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.app.AppConfig;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 * Implements the standard freedomotic APIs available to plugins. This class
 * returns only unmodifiable collections, so the returned collections are just a
 * read-only view of current underlying data. This data are not immutable
 * themselves, but they are immutable trough the references retrieved from the
 * methods of this class.
 *
 * @author enrico
 */
class APIStandardImpl implements API {

    private final EnvironmentRepository environments;
    private final ThingRepository things;
    private final ClientStorage clientStorage;
    private final AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    private final PluginsManager plugManager;
    private TriggerPersistence triggers;
    private CommandPersistence commands;
    private ReactionPersistence reactions;
    private final ThingFactory thingsFactory;
    private NlpCommand nlpCommands;

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
            EnvironmentRepository environment,
            ThingRepository things,
            ThingFactory thingsFactory,
            ClientStorage clientStorage,
            AppConfig config,
            Auth auth,
            I18n i18n,
            PluginsManager plugManager,
            TriggerPersistence triggerPersistence,
            CommandPersistence commands,
            ReactionPersistence reactions,
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
    public TriggerPersistence triggers() {
        return triggers;
    }

    @Override
    public ThingRepository things() {
        return things;
    }

    @Override
    public CommandPersistence commands() {
        return commands;
    }

    @Override
    public ReactionPersistence reactions() {
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

}
