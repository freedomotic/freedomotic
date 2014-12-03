/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.impl;

import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;


/**
 *
 * @author enrico
 */
public class InjectorPlugins extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(ClientStorage.class).to(ClientStorageInMemory.class).in(Singleton.class);
                bind(PluginsManager.class).to(PluginsManagerImpl.class).in(Singleton.class);
    }
}
