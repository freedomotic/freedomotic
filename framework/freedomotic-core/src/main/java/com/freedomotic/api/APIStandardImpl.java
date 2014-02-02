/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.freedomotic.app.AppConfig;

import com.freedomotic.core.ResourcesManager;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;

import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;

import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.security.Auth;
import com.freedomotic.util.I18n.I18n;

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

    private final EnvironmentPersistence environment;
    private final EnvObjectPersistence object;
    private final ClientStorage clientStorage;
    private final AppConfig config;
    private final Auth auth;
    private final I18n i18n;
    private final PluginsManager plugManager;

    @Inject
    public APIStandardImpl(
            EnvironmentPersistence environment,
            EnvObjectPersistence object,
            ClientStorage clientStorage,
            AppConfig config,
            Auth auth,
            I18n i18n,
            PluginsManager plugManager) {
        this.environment = environment;
        this.object = object;
        this.clientStorage = clientStorage;
        this.config = config;
        this.auth = auth;
        this.i18n = i18n;
        this.plugManager = plugManager;
        System.out.println("auth in apiimpl is " + this.auth);
    }

    @Override
    public AppConfig getConfig() {
        return config;
    }

    @Override
    public EnvObjectLogic addObject(EnvObjectLogic obj, boolean MAKE_UNIQUE) {
        return object.add(obj, MAKE_UNIQUE);
    }

    @Override
    public Collection<EnvObjectLogic> getObjectList() {
        return /*Collections.unmodifiableList(*/ object.getObjectList(); /*);*/
    }

    @Override
    public EnvObjectLogic getObjectByName(String name) {
        return object.getObjectByName(name);
    }

    @Override
    public EnvObjectLogic getObjectByUUID(String uuid) {
        return object.getObjectByUUID(uuid);
    }

    @Override
    public Collection<EnvObjectLogic> getObjectByAddress(String protocol, String address) {
        return object.getObjectByAddress(protocol, address);
    }

    @Override
    public Collection<EnvObjectLogic> getObjectByProtocol(String protocol) {
        return object.getObjectByProtocol(protocol);
    }

    @Override
    public Collection<EnvObjectLogic> getObjectByEnvironment(String uuid) {
        return object.getObjectByEnvironment(uuid);
    }

    @Override
    public void removeObject(EnvObjectLogic input) {
        object.remove(input);
    }

    @Override
    public EnvironmentLogic addEnvironment(EnvironmentLogic obj, boolean MAKE_UNIQUE) {
        return environment.add(obj, MAKE_UNIQUE);
    }

    @Override
    public List<EnvironmentLogic> getEnvironments() {
        return environment.getEnvironments();
    }

    @Override
    public EnvironmentLogic getEnvByUUID(String UUID) {
        return environment.getEnvByUUID(UUID);
    }

    @Override
    public void removeEnvironment(EnvironmentLogic input) {
        environment.remove(input);
    }

    @Override
    public Collection<Client> getClients(String filter) {
        return clientStorage.getClients(filter);
    }
    
    @Override
    public Collection<Client> getClients() {
        return clientStorage.getClients();
    }

    public BufferedImage getResource(String resourceIdentifier) {
        return ResourcesManager.getResource(resourceIdentifier);
    }

    @Override
    public ClientStorage getClientStorage() {
        return clientStorage;
    }
    
    @Override
    public Auth getAuth(){
        return auth;
    }

    @Override
    public I18n getI18n() {
        return i18n;
    }

    @Override
    public PluginsManager getPluginManager() {
        return plugManager;
    }

    @Override
    public Collection<EnvObjectLogic> getObjectByTag(String tag) {
        return object.getObjectByTags(tag);
    }
}
