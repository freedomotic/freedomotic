/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.app;

import com.freedomotic.api.API;
import com.freedomotic.api.APIStandardImpl;
import com.freedomotic.bus.BusService;
import com.freedomotic.bus.impl.BusServiceImpl;
import com.freedomotic.core.JoinPlugin;
import com.freedomotic.core.TriggerCheck;
import com.freedomotic.environment.EnvironmentDAO;
import com.freedomotic.environment.EnvironmentDAOFactory;
import com.freedomotic.environment.EnvironmentDAOXstream;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ClientStorageInMemory;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.plugins.filesystem.PluginsManagerImpl;
import com.freedomotic.reactions.CommandPersistence;
import com.freedomotic.reactions.ReactionPersistence;
import com.freedomotic.reactions.TriggerPersistence;
import com.freedomotic.security.Auth;
import com.freedomotic.security.AuthImpl;
import com.freedomotic.util.I18n.I18n;
import com.freedomotic.util.I18n.I18nImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 * @author enrico
 */
public class DependenciesInjector
        extends AbstractModule {

    /**
     *
     */
    @Override
    protected void configure() {
        bind(ClientStorage.class).to(ClientStorageInMemory.class).in(Singleton.class);
        bind(PluginsManager.class).to(PluginsManagerImpl.class).in(Singleton.class);
        //bind(JoinDevice.class).in(Singleton.class);
        bind(JoinPlugin.class).in(Singleton.class);
        bind(TriggerCheck.class).in(Singleton.class);
        install(new FactoryModuleBuilder().implement(EnvironmentDAO.class, EnvironmentDAOXstream.class)
                .build(EnvironmentDAOFactory.class));
        bind(API.class).to(APIStandardImpl.class).in(Singleton.class);
        bind(ProtocolRead.class);

        bind(AppConfig.class).to(AppConfigImpl.class).in(Singleton.class);

        bind(Auth.class).to(AuthImpl.class).in(Singleton.class);

        bind(I18n.class).to(I18nImpl.class).in(Singleton.class);

        bind(BusService.class).to(BusServiceImpl.class).in(Singleton.class);

        bind(EnvironmentPersistence.class).in(Singleton.class);
        bind(TriggerPersistence.class).in(Singleton.class);
        bind(EnvObjectPersistence.class).in(Singleton.class);
        bind(CommandPersistence.class).in(Singleton.class);
        bind(ReactionPersistence.class).in(Singleton.class);

    }
}
