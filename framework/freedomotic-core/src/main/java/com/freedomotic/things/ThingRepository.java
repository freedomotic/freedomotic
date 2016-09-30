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
package com.freedomotic.things;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.persistence.Repository;
import java.io.File;
import java.util.List;

/**
 *
 * @author Enrico Nicoletti
 */
public interface ThingRepository extends Repository<EnvObjectLogic> {

    public List<EnvObjectLogic> findByEnvironment(EnvironmentLogic env);

    public List<EnvObjectLogic> findByEnvironment(String uuid);

    public List<EnvObjectLogic> findByProtocol(String protocolName);

    public EnvObjectLogic findByAddress(String protocol, String address);

    //TODO: temporary for refactoring, should be removed
    public EnvObjectLogic load(File file) throws RepositoryException;

    public List<EnvObjectLogic> loadAll(File folder) throws RepositoryException;

    public void saveAll(File folder) throws RepositoryException;
}
