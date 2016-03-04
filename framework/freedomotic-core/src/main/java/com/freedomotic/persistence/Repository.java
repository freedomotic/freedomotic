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

import java.util.List;

/**
 *
 * @author matteo
 * @param <T>
 */
public interface Repository<T> {

    /**
     *
     * @return
     */
    public List<T> findAll();

    // TODO: it's supposed name is a unique identifier, should return a single object

    /**
     *
     * @param name
     * @return
     */
        public List<T> findByName(String name);

    /**
     *
     * @param uuid
     * @return
     */
    public T findOne(String uuid);

    /**
     *
     * @param item
     * @return
     */
    public boolean create(T item);  //TODO: refactor in T save(T item)

    /**
     *
     * @param item
     * @return
     */
    public boolean delete(T item);

    /**
     *
     * @param uuid
     * @return
     */
    public boolean delete(String uuid);

    /**
     *
     * @param uuid
     * @param data
     * @return
     */
    public T modify(String uuid, T data);
    
    /**
     *
     * @param data
     * @return
     */
    public T copy(T data);
    
    /**
     *
     */
    public void deleteAll();
    
    //TODO: public long count();
    //TODO: public boolean exists(String uuid);
    //TODO: public void flush();
    //TODO: T saveAndFlush(T arg0)

}
