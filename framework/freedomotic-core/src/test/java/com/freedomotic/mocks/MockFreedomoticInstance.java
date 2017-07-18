package com.freedomotic.mocks;

import com.freedomotic.api.InjectorApi;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.app.FreedomoticInjector;
import com.freedomotic.bus.InjectorBus;
import com.freedomotic.core.InjectorFeatures;
import com.freedomotic.environment.impl.InjectorEnvironment;
import com.freedomotic.i18n.InjectorI18n;
import com.freedomotic.nlp.InjectorNlp;
import com.freedomotic.persistence.InjectorPersistence;
import com.freedomotic.plugins.impl.InjectorPlugins;
import com.freedomotic.reactions.InjectorAutomations;
import com.freedomotic.security.InjectorSecurity;
import com.freedomotic.settings.InjectorSettings;
import com.freedomotic.things.impl.InjectorThings;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/**
 * Static container for a global Freedomotic INJECTOR to be used for Mocking classes for JUnit tests
 * @author P3trur0 https://flatmap.it
 *
 */
public abstract class MockFreedomoticInstance {

	 static {
		 Freedomotic.INJECTOR = Guice.createInjector(new FreedomoticMockInjector());
	 }
	
}

class FreedomoticMockInjector extends AbstractModule {

    @Override
    protected void configure() {
        install(new MockInjectorBus());
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

