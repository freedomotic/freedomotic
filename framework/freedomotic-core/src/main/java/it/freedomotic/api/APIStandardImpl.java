/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.api;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import it.freedomotic.core.ResourcesManager;

import it.freedomotic.environment.EnvironmentLogic;
import it.freedomotic.environment.EnvironmentPersistence;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;

import it.freedomotic.plugins.ClientStorage;

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

    @Inject
    public APIStandardImpl(EnvironmentPersistence environment, EnvObjectPersistence object, ClientStorage clientStorage) {
        this.environment = environment;
        this.object = object;
        this.clientStorage = clientStorage;
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

    public BufferedImage getResource(String resourceIdentifier) {
        return ResourcesManager.getResource(resourceIdentifier);
    }

    @Override
    public ClientStorage getClientStorage() {
        return clientStorage;
    }
}
