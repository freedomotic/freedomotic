/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.objects.impl;

import com.freedomotic.objects.ThingsFactory;
import com.freedomotic.objects.ThingsRepository;
import com.freedomotic.objects.impl.ThingsRepositoryImpl;
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
