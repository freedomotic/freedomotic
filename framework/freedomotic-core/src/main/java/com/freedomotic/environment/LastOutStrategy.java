/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.environment;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 *
 * @author Enrico
 */
public class LastOutStrategy
        implements Ownership,
        Serializable {

    private static final long serialVersionUID = -4839776027684778640L;

	@Override
    public boolean canTriggerReactionsOnEnter(ZoneLogic z) {
        return true;
    }

    @Override
    public boolean canTriggerReactionsOnExit(ZoneLogic z) {
        if (z.howManyInside() <= 1) { //the last person in the zone is exiting from it

            return true;
        } else {
            return false;
        }
    }
    private static final Logger LOG = Logger.getLogger(LastOutStrategy.class.getName());
}
