/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.app.AppConfig;
import com.freedomotic.core.ResourcesManager;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.util.I18n.I18n;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

/**
 * Implements the standard freedomotic APIs available to plugins. This class
 * returns only unmodifiable collections, so the returned collections are just a
 * read-only view of current underlying data. This data are not immutable
 * themselves, but they are immutable trough the references retrieved from the
 * methods of this class.
 *
 * @author enrico
 */
@Singleton
public class APIStandardImpl
        implements API {

    private final EnvironmentPersistence environments;
    private final EnvObjectPersistence objects;
    private final ClientStorage clientStorage;
    private final AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    private final PluginsManager plugManager;
    private TriggerPersistence triggers;
    private CommandPersistence commands;
    private ReactionPersistence reactions;

    /**
     *
     * @param environment
     * @param object
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
            EnvironmentPersistence environment,
            EnvObjectPersistence object,
            ClientStorage clientStorage,
            AppConfig config,
            Auth auth,
            I18n i18n,
            PluginsManager plugManager,
            TriggerPersistence triggerPersistence,
            CommandPersistence commands,
            ReactionPersistence reactions) {
        this.environments = environment;
        this.objects = object;
        this.clientStorage = clientStorage;
        this.config = config;
        this.auth = auth;
        this.i18n = i18n;
        this.plugManager = plugManager;
        this.triggers = triggerPersistence;
        this.commands = commands;
        this.reactions = reactions;
        System.out.println("auth in apiimpl is " + this.auth);
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
     * @param obj
     * @param MAKE_UNIQUE
     * @return
     */
    @Override
    public EnvObjectLogic addObject(EnvObjectLogic obj, boolean MAKE_UNIQUE) {
        return objects.add(obj, MAKE_UNIQUE);
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<EnvObjectLogic> getObjectList() {
        return /*Collections.unmodifiableList(*/ objects.getObjectList(); /*);*/

    }

    /**
     *
     * @param name
     * @return
     */
    @Override
    public EnvObjectLogic getObjectByName(String name) {
        return objects.getObjectByName(name);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Override
    public EnvObjectLogic getObjectByUUID(String uuid) {
        return objects.getObjectByUUID(uuid);
    }

    /**
     *
     * @param protocol
     * @param address
     * @return
     */
    @Override
    public Collection<EnvObjectLogic> getObjectByAddress(String protocol, String address) {
        return objects.getObjectByAddress(protocol, address);
    }

    /**
     *
     * @param protocol
     * @return
     */
    @Override
    public Collection<EnvObjectLogic> getObjectByProtocol(String protocol) {
        return objects.getObjectByProtocol(protocol);
    }

    /**
     *
     * @param uuid
     * @return
     */
    @Override
    public Collection<EnvObjectLogic> getObjectByEnvironment(String uuid) {
        return objects.getObjectByEnvironment(uuid);
    }

    /**
     *
     * @param input
     */
    @Override
    public void removeObject(EnvObjectLogic input) {
        objects.remove(input);
    }

    /**
     *
     * @param obj
     * @param MAKE_UNIQUE
     * @return
     */
    @Override
    public EnvironmentLogic addEnvironment(EnvironmentLogic obj, boolean MAKE_UNIQUE) {
        return environments.add(obj, MAKE_UNIQUE);
    }

    /**
     *
     * @return
     */
    @Override
    public List<EnvironmentLogic> getEnvironments() {
        return environments.getEnvironments();
    }

    /**
     *
     * @param UUID
     * @return
     */
    @Override
    public EnvironmentLogic getEnvByUUID(String UUID) {
        return environments.getEnvByUUID(UUID);
    }

    /**
     *
     * @param input
     */
    @Override
    public void removeEnvironment(EnvironmentLogic input) {
        environments.remove(input);
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

    /**
     *
     * @param tag
     * @return
     */
    @Override
    public Collection<EnvObjectLogic> getObjectByTag(String tag) {
        return objects.getObjectByTags(tag);
    }

    @Override
    public EnvironmentPersistence environments() {
        return environments;
    }

    @Override
    public TriggerPersistence triggers() {
        return triggers;
    }

    @Override
    public EnvObjectPersistence objects() {
        return objects;
    }

    @Override
    public CommandPersistence commands() {
        return commands;
    }

    @Override
    public ReactionPersistence reactions() {
        return reactions;
    }

    
}
