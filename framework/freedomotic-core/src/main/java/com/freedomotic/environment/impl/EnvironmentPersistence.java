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

import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.model.environment.Environment;
import java.util.Collection;

/**
 * This interface is intended to be used by only by the related
 * {@link EnvironmentRepository} It will manage the lower layer details about
 * persistence (eg: filesystem load/save)
 *
 * @author Enrico Nicoletti
 */
interface EnvironmentPersistence {

    /**
     * Dumps an Environment to the persistence layer (eg: filesystem or
     * database)
     *
     * @param environment
     * @throws RepositoryException
     */
    void persist(Environment environment) throws RepositoryException;

    /**
     * Completely removes an Environment from the persistence layer (eg:
     * filesystem or database)
     *
     * @param environment
     * @throws RepositoryException
     */
    void delete(Environment environment) throws RepositoryException;

    /**
     * Bootstaps all the resources reading them from the persistence layer
     *
     * @return
     * @throws RepositoryException
     */
    Collection<Environment> loadAll() throws RepositoryException;
}
