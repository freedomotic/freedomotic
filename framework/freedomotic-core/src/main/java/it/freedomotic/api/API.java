/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.api;

import it.freedomotic.app.AppConfig;
import it.freedomotic.core.ResourcesManager;

import it.freedomotic.environment.EnvironmentLogic;

import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.plugins.ClientStorage;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface API {
    //Configuration APi

    AppConfig getConfig();

    //Object API
    //create
    EnvObjectLogic addObject(final EnvObjectLogic obj, final boolean MAKE_UNIQUE);

    ClientStorage getClientStorage();
    //read

    Collection<EnvObjectLogic> getObjectList();

    EnvObjectLogic getObjectByName(String name);

    EnvObjectLogic getObjectByUUID(String uuid);

    Collection<EnvObjectLogic> getObjectByAddress(String protocol, String address);

    Collection<EnvObjectLogic> getObjectByProtocol(String protocol);

    Collection<EnvObjectLogic> getObjectByEnvironment(String uuid);

    //delete
    void removeObject(EnvObjectLogic input);

    //Environment API
    //create
    EnvironmentLogic addEnvironment(final EnvironmentLogic obj, boolean MAKE_UNIQUE);

    //read
    List<EnvironmentLogic> getEnvironments();

    EnvironmentLogic getEnvByUUID(String UUID);

    //delete
    void removeEnvironment(EnvironmentLogic input);
    
    Collection<Client> getClients();

    Collection<Client> getClients(String filter);

    //Resources API
    /**
     *
     * @param resourceIdentifier
     * @return
     */
    BufferedImage getResource(String resourceIdentifier);
}
