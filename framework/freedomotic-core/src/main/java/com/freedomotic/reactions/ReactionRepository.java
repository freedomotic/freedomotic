/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
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
package com.freedomotic.reactions;

import com.freedomotic.persistence.Repository;
import java.io.File;
import java.util.List;

/**
 *
 * @author enrico
 */
public interface ReactionRepository extends Repository<Reaction> {

   
    //TODO: remove it, here just for refactoring

    /**
     *
     * @param folder
     */
    
    public void loadReactions(File folder);

    /**
     *
     * @param folder
     */
    public void saveReactions(File folder);
}
