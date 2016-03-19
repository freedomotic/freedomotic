/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
 * http://freedomotic.com
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
package com.freedomotic.api;

import com.freedomotic.bus.BusService;
import com.freedomotic.settings.AppConfig;
import com.freedomotic.environment.EnvironmentRepository;
import com.freedomotic.things.ThingRepository;
import com.freedomotic.plugins.ClientStorage;
import com.freedomotic.plugins.PluginsManager;
import com.freedomotic.reactions.ReactionRepository;
import com.freedomotic.security.Auth;
import com.freedomotic.i18n.I18n;
import com.freedomotic.nlp.NlpCommand;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.reactions.TriggerRepository;
import com.freedomotic.things.ThingFactory;
import java.awt.image.BufferedImage;
import java.util.Collection;

/**
 *
 * @author Enrico Nicoletti
 */
public interface API {
    //Configuration APi

    /**
     *
     * @return
     */
    AppConfig getConfig();

    /**
     *
     * @return
     */
    Auth getAuth();

    /**
     *
     * @return
     */
    I18n getI18n();

    /**
     *
     * @return
     */
    ClientStorage getClientStorage();
    //read

    /**
     *
     * @return
     */
    Collection<Client> getClients();

    /**
     *
     * @param filter
     * @return
     */
    Collection<Client> getClients(String filter);

    /**
     *
     * @return
     */
    PluginsManager getPluginManager();

    //Resources API
    /**
     *
     * @param resourceIdentifier
     * @return
     */
    BufferedImage getResource(String resourceIdentifier);

    EnvironmentRepository environments();

    TriggerRepository triggers();

    ThingRepository things();

    CommandRepository commands();

    ReactionRepository reactions();

    NlpCommand nlpCommands();

    ThingFactory thingsFactory();

    BusService bus();
}
