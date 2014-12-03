/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment.impl;

import com.freedomotic.environment.EnvironmentRepository;
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

        bind(EnvironmentRepository.class).to(EnvironmentRepositoryImpl.class).in(Singleton.class);
        install(new FactoryModuleBuilder().implement(EnvironmentPersistence.class, EnvironmentPersistenceImpl.class).build(EnvironmentPersistenceFactory.class));
    }
}
