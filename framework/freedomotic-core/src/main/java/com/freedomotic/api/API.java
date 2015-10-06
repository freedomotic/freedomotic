/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.freedomotic.bus.BusService;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.things.ThingFactory;
import java.awt.image.BufferedImage;
import java.util.Collection;

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
    TriggerRepository triggers();
    ThingRepository things();
    CommandRepository commands();
    ReactionPersistence reactions();
    NlpCommand nlpCommands();
    
    ThingFactory thingsFactory();
    
    BusService bus();
}
