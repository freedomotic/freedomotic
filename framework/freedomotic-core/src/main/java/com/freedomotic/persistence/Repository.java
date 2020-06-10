/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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

import java.util.List;

/**
 * Generic repository of freedomotics objects.
 * @author Matteo Mazzoni
 * @param <T> class the repository is dedicated to
 */
public interface Repository<T> {

    /**
     * Finds all the entries of <T>
     * @return list of all found objects
     */
    List<T> findAll();

    /**
     * Find all objects having the name provided.
     * @param name value to match
     * @return list of all found objects TODO: it's supposed name is a unique identifier, should return a single object
     */
    List<T> findByName(String name);

    /**
     * Find an object from its uuid.
     * @param uuid value to match
     * @return the found objects if present
     */
    T findOne(String uuid);

    
    /**
     * Save the provided object.
     * @param item object to save
     * @return true if OK, false instead
     * TODO: refactor in T save(T item)
     */
    boolean create(T item);

    /**
     * Delete the provided object.
     * @param item object to delete
     * @return true if OK, false instead
     */
    boolean delete(T item);

    /**
     * Delete the object identified by an uuid.
     * @param uuid reference of the object to delete
     * @return true if done, false instead
     */
    boolean delete(String uuid);

    /**
     * Replace with other data, the content of the object referenced by an uuid.
     * @param uuid reference of the object to update
     * @param data data to save
     * @return true if OK, false instead
     */
    T modify(String uuid, T data);

    /**
     * Copy a <T> object.
     * @param data information to copy
     * @return the copied data
     */
    T copy(T data);

    /**
     * Delete all the <T> objects
     */
    void deleteAll();

    //TODO: add a long count() method
    //TODO: add a boolean exists(String uuid) method
    //TODO: add a void flush() method
    //TODO: add a T saveAndFlush(T arg0) method
}
