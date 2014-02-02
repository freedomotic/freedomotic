/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.app.AppConfig;
import com.freedomotic.core.ResourcesManager;

import com.freedomotic.environment.EnvironmentLogic;

import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.security.Auth;
import com.freedomotic.util.I18n.I18n;

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
    
    Auth getAuth();
    
    I18n getI18n();

    //Object API
    //create
    EnvObjectLogic addObject(final EnvObjectLogic obj, final boolean MAKE_UNIQUE);

    ClientStorage getClientStorage();
    //read

    Collection<EnvObjectLogic> getObjectList();

    EnvObjectLogic getObjectByName(String name);

    EnvObjectLogic getObjectByUUID(String uuid);
    Collection<EnvObjectLogic> getObjectByTag(String tag);
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
    
    PluginsManager getPluginManager();

    //Resources API
    /**
     *
     * @param resourceIdentifier
     * @return
     */
    BufferedImage getResource(String resourceIdentifier);
}
