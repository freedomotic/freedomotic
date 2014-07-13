/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.bus;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;


/**
 *
 * @author enrico
 */
public class InjectorBus extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(BusService.class).to(BusServiceImpl.class).in(Singleton.class);
        
    }
}
