/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.things.impl;

import com.freedomotic.things.ThingFactory;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.things.impl.ThingRepositoryImpl;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;


/**
 *
 * @author enrico
 */
public class InjectorThings extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(ThingRepository.class).to(ThingRepositoryImpl.class).in(Singleton.class);
        bind(ThingFactory.class);
        
    }
}
