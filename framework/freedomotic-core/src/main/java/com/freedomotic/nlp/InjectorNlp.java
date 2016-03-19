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
package com.freedomotic.nlp;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

/**
 *
 * @author Enrico Nicoletti
 */
public class InjectorNlp extends AbstractModule {

    @Override
    protected void configure() {
        bind(NlpCommand.class).to(NlpCommandStringDistanceImpl.class).in(Singleton.class);
        bind(CommandsNlpService.class).in(Singleton.class);
    }

}
