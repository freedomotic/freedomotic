/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.testutils;

import com.freedomotic.api.API;
import com.freedomotic.api.APIStandardImpl;
import com.freedomotic.core.JoinPlugin;
import com.freedomotic.core.TriggerCheck;
import com.freedomotic.environment.EnvironmentDAO;
import com.freedomotic.environment.EnvironmentDAOFactory;
import com.freedomotic.environment.EnvironmentDAOXstream;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.ClientStorageInMemory;
import com.freedomotic.plugins.filesystem.PluginsManager;
import com.freedomotic.plugins.filesystem.PluginsManagerImpl;

import org.junit.Ignore;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 * @author enrico
 */
@Ignore
public class FreedomoticTestsInjector
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
    }
}
