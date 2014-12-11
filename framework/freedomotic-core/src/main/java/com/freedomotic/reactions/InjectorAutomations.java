/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author enrico
 */
public class InjectorAutomations extends AbstractModule {

    @Override
    protected void configure() {
        bind(CommandRepository.class).to(CommandPersistence.class).in(Singleton.class);
    }

}
