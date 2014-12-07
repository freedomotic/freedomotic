/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author enrico
 */
public class InjectorPersistence extends AbstractModule {

    @Override
    protected void configure() {
        bind(DataUpgradeService.class).to(DataUpgradeServiceImpl.class).in(Singleton.class);
    }

}
