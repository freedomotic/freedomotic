/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.api;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;


/**
 *
 * @author enrico
 */
public class InjectorApi extends AbstractModule {


    @Override
    protected void configure() {
        
        bind(API.class).to(APIStandardImpl.class).in(Singleton.class);
        
    }
}
