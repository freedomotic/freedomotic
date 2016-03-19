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
package com.freedomotic.core;

import com.freedomotic.events.ProtocolRead;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author Enrico Nicoletti
 */
public class InjectorFeatures extends AbstractModule {

    @Override
    protected void configure() {

        bind(Autodiscovery.class).in(Singleton.class);
        bind(SynchManager.class).in(Singleton.class);
        bind(TopologyManager.class).in(Singleton.class);
        bind(JoinPlugin.class).in(Singleton.class);
        bind(TriggerCheck.class).in(Singleton.class);
        bind(BehaviorManager.class).in(Singleton.class);
        //TODO: bind(ResourcesManager.class).in(Singleton.class);

        // The ProcolRead event now needs the TriggerCheck class
        // It should be refactored in the future to avoid this dependency
        bind(ProtocolRead.class);

    }
}
