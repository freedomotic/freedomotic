/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.app;

import com.freedomotic.api.InjectorApi;
import com.freedomotic.bus.InjectorBus;
import com.freedomotic.core.InjectorFeatures;
import com.freedomotic.environment.impl.InjectorEnvironment;
import com.freedomotic.plugins.impl.InjectorPlugins;
import com.freedomotic.i18n.InjectorI18n;
import com.freedomotic.nlp.InjectorNlp;
import com.freedomotic.things.impl.InjectorThings;
import com.freedomotic.persistence.InjectorPersistence;
import com.freedomotic.reactions.InjectorAutomations;
import com.freedomotic.security.InjectorSecurity;
import com.freedomotic.settings.InjectorSettings;
import com.google.inject.AbstractModule;

/**
 *
 * @author Enrico Nicoletti
 */
public class FreedomoticInjector extends AbstractModule {

    @Override
    protected void configure() {
        install(new InjectorBus());
        install(new InjectorPlugins());
        install(new InjectorApi());
        install(new InjectorEnvironment());
        install(new InjectorThings());
        install(new InjectorFeatures());
        install(new InjectorPersistence());
        install(new InjectorNlp());
        install(new InjectorAutomations());
        install(new InjectorI18n());
        install(new InjectorSecurity());
        install(new InjectorSettings());

    }
}
