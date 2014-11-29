/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.app.AppConfig;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.ThingsRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.i18n.I18n;
import com.freedomotic.objects.ThingsFactory;
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
    
    EnvironmentRepository environments();
    TriggerPersistence triggers();
    ThingsRepository things();
    CommandPersistence commands();
    ReactionPersistence reactions();
    
    ThingsFactory thingsFactory();
}
