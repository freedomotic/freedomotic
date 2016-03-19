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
package com.freedomotic.environment;

import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.persistence.Repository;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author Enrico Nicoletti
 */
public interface EnvironmentRepository extends Repository<EnvironmentLogic> {

    //TODO: remove it it's just temporary to ease the refactoring
    public void saveEnvironmentsToFolder(File folder) throws RepositoryException;

    //public boolean loadEnvironmentsFromDir(File folder, boolean makeUnique) throws RepositoryException;

    public EnvironmentLogic loadEnvironmentFromFile(File file) throws RepositoryException;

    public void saveAs(EnvironmentLogic env, File folder) throws IOException;

    public void init(File folder) throws RepositoryException;

    public void initFromDefaultFolder() throws RepositoryException;

}
