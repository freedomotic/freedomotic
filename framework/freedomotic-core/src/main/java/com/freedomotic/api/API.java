/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.app.AppConfig;
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
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface API {
    //Configuration APi

    /**
     *
     * @return
     */
    
    AppConfig getConfig();

    /**
     *
     * @return
     */
    Auth getAuth();
    
    /**
     *
     * @return
     */
    I18n getI18n();

    //Object API
    //create

    /**
     *
     * @param obj
     * @param MAKE_UNIQUE
     * @return
     */
        EnvObjectLogic addObject(final EnvObjectLogic obj, final boolean MAKE_UNIQUE);

    /**
     *
     * @return
     */
    ClientStorage getClientStorage();
    //read

    /**
     *
     * @return
     */
    Collection<EnvObjectLogic> getObjectList();

    /**
     *
     * @param name
     * @return
     */
    EnvObjectLogic getObjectByName(String name);

    /**
     *
     * @param uuid
     * @return
     */
    EnvObjectLogic getObjectByUUID(String uuid);

    /**
     *
     * @param tag
     * @return
     */
    Collection<EnvObjectLogic> getObjectByTag(String tag);

    /**
     *
     * @param protocol
     * @param address
     * @return
     */
    Collection<EnvObjectLogic> getObjectByAddress(String protocol, String address);

    /**
     *
     * @param protocol
     * @return
     */
    Collection<EnvObjectLogic> getObjectByProtocol(String protocol);

    /**
     *
     * @param uuid
     * @return
     */
    Collection<EnvObjectLogic> getObjectByEnvironment(String uuid);

    //delete

    /**
     *
     * @param input
     */
        void removeObject(EnvObjectLogic input);

    //Environment API
    //create

    /**
     *
     * @param obj
     * @param MAKE_UNIQUE
     * @return
     */
        EnvironmentLogic addEnvironment(final EnvironmentLogic obj, boolean MAKE_UNIQUE);

    //read

    /**
     *
     * @return
     */
        List<EnvironmentLogic> getEnvironments();

    /**
     *
     * @param UUID
     * @return
     */
    EnvironmentLogic getEnvByUUID(String UUID);

    //delete

    /**
     *
     * @param input
     */
        void removeEnvironment(EnvironmentLogic input);

    /**
     *
     * @return
     */
    Collection<Client> getClients();

    /**
     *
     * @param filter
     * @return
     */
    Collection<Client> getClients(String filter);
    
    /**
     *
     * @return
     */
    PluginsManager getPluginManager();

    //Resources API
    /**
     *
     * @param resourceIdentifier
     * @return
     */
    BufferedImage getResource(String resourceIdentifier);
    
    EnvironmentPersistence environments();
    TriggerPersistence triggers();
    EnvObjectPersistence objects();
    CommandPersistence commands();
    ReactionPersistence reactions();
}
