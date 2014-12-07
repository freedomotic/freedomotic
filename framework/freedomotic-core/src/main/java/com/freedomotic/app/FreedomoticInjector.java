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
import com.freedomotic.security.Auth;
import com.freedomotic.security.AuthImpl2;
import com.freedomotic.i18n.I18n;
import com.freedomotic.i18n.I18nImpl;
import com.freedomotic.things.impl.InjectorThings;
import com.freedomotic.persistence.InjectorPersistence;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

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

        bind(AppConfig.class).to(AppConfigImpl.class).in(Singleton.class);

        bind(Auth.class).to(AuthImpl2.class).in(Singleton.class);

        bind(I18n.class).to(I18nImpl.class).in(Singleton.class);
    }
}
