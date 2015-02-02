/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.app;

import com.freedomotic.api.InjectorApi;
import com.freedomotic.bus.InjectorBus;
import com.freedomotic.core.InjectorFeatures;
import com.freedomotic.environment.impl.InjectorEnvironment;
import com.freedomotic.plugins.impl.InjectorPlugins;
import com.freedomotic.i18n.InjectorI18n;
import com.freedomotic.nlp.InjectorNlp;
import com.freedomotic.things.impl.InjectorThings;
import com.freedomotic.persistence.InjectorPersistence;
import com.freedomotic.reactions.InjectorAutomations;
import com.freedomotic.security.InjectorSecurity;
import com.freedomotic.settings.InjectorSettings;
import com.google.inject.AbstractModule;

/**
 *
 * @author enrico
 */
public class FreedomoticInjector extends AbstractModule {

    @Override
    protected void configure() {
        install(new InjectorBus());
        install(new InjectorPlugins());
        install(new InjectorApi());
        install(new InjectorEnvironment());
        install(new InjectorThings());
        install(new InjectorFeatures());
        install(new InjectorPersistence());
        install(new InjectorNlp());
        install(new InjectorAutomations());
        install(new InjectorI18n());
        install(new InjectorSecurity());
        install(new InjectorSettings());
        
    }
}
