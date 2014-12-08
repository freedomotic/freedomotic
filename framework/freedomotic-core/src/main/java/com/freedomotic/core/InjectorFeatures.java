/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.events.ProtocolRead;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author enrico
 */
public class InjectorFeatures extends AbstractModule {

    @Override
    protected void configure() {

        bind(Autodiscovery.class).in(Singleton.class);
        bind(SynchManager.class).in(Singleton.class);
        bind(TopologyManager.class).in(Singleton.class);
        bind(JoinPlugin.class).in(Singleton.class);
        bind(TriggerCheck.class).in(Singleton.class);
        bind(BehaviorManager.class).in(Singleton.class);
        //TODO: bind(ResourcesManager.class).in(Singleton.class);
        bind(FreeFormCommandsInterpreter.class).in(Singleton.class);

        // The ProcolRead event now needs the TriggerCheck class
        // It should be refactored in the future to avoid this dependency
        bind(ProtocolRead.class);

    }
}
