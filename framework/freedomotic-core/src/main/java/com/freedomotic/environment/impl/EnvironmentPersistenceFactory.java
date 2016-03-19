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
package com.freedomotic.environment.impl;

import java.io.File;

/**
 * A Guice factory to bind the {@link EnvironmentPersistence} class to its
 * implementation while allowing to pass a parameter to the implementation
 * constructior (see Guice assisted injection)
 *
 * @author Enrico Nicoletti
 */
interface EnvironmentPersistenceFactory {

    public EnvironmentPersistence create(File directory);
}
