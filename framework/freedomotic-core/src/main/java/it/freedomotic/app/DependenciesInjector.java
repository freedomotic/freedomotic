/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.app;

import it.freedomotic.api.API;
import it.freedomotic.api.APIStandardImpl;
import it.freedomotic.bus.BusService;
import it.freedomotic.bus.impl.BusServiceImpl;
import it.freedomotic.core.JoinPlugin;
import it.freedomotic.core.TriggerCheck;
import it.freedomotic.environment.EnvironmentDAO;
import it.freedomotic.environment.EnvironmentDAOFactory;
import it.freedomotic.environment.EnvironmentDAOXstream;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.plugins.ClientStorage;
import it.freedomotic.plugins.ClientStorageInMemory;
import it.freedomotic.plugins.filesystem.PluginsManager;
import it.freedomotic.plugins.filesystem.PluginsManagerImpl;
import it.freedomotic.security.Auth;
import it.freedomotic.security.AuthImpl;
import it.freedomotic.util.I18n.I18n;
import it.freedomotic.util.I18n.I18nImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;


/**
 *
 * @author enrico
 */
public class DependenciesInjector
        extends AbstractModule {

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
        //requestStaticInjection(I18n.class);
        
        bind(BusService.class).to(BusServiceImpl.class).in(Singleton.class);
        
    }
}
