/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.security;

import com.freedomotic.i18n.*;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author enrico
 */
public class InjectorSecurity extends AbstractModule {

    @Override
    protected void configure() {
        bind(Auth.class).to(AuthImpl2.class).in(Singleton.class);
    }

}
