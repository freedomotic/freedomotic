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
package com.freedomotic.rules;

import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import java.util.Map;

/**
 *
 * @author Enrico Nicoletti
 */
public interface Resolver {

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Config aContext);

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Map<String, String> aContext);

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Payload aContext);

    /**
     * Creates a resolved clone of the reaction in input. All commands in the
     * reaction are resolved according to the context given in the contructor.
     *
     * @param r
     * @return a clone of the resolver reaction
     */
    Reaction resolve(Reaction r);

    /**
     * Creates a resolved clone of the command in input according to the current
     * context given in input to the constructor.
     *
     * @param c
     * @return
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    Command resolve(Command c) throws CloneNotSupportedException, VariableResolutionException;

    /**
     * Creates a resolved clone of the trigger in input according to the current
     * context given in input to the constructor.
     *
     * @param trigger
     * @return
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    Trigger resolve(Trigger t) throws VariableResolutionException;
    
}
