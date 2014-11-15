/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.FactoryModuleBuilder;

/**
 *
 * @author enrico
 */
public class InjectorEnvironment extends AbstractModule {

    @Override
    protected void configure() {

        bind(EnvironmentRepository.class).to(EnvironmentPersistence.class).in(Singleton.class);

        install(new FactoryModuleBuilder().implement(EnvironmentLoader.class, EnvironmentLoaderImpl.class)
                .build(EnvironmentLoaderFactory.class));
    }
}
