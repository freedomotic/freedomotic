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
package com.freedomotic.persistence;

import com.freedomotic.exceptions.DataUpgradeException;

/**
 * Upgrades the data making them compatible with the current version
 *
 * @author Enrico Nicoletti
 * @param <T> The type of data to upgrade
 */
public interface DataUpgradeService<T> {

    /**
     * Returns an upgraded object
     *
     * @param classType Pass a the dataObject class to check if a suitable
     * upgrader can be instantiated
     * @param dataObject The object to upgrade
     * @param fromVersion The source version of the data to upgrade
     * @return
     * @throws DataUpgradeException
     */
    T upgrade(Class classType, T dataObject, String fromVersion) throws DataUpgradeException;

}
