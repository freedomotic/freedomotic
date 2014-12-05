/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.things.impl;

import com.freedomotic.things.ThingsFactory;
import com.freedomotic.things.ThingsRepository;
import com.freedomotic.things.impl.ThingsRepositoryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;


/**
 *
 * @author enrico
 */
public class InjectorThings extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(ThingsRepository.class).to(ThingsRepositoryImpl.class).in(Singleton.class);
        bind(ThingsFactory.class);
        
    }
}
