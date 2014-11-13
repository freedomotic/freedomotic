/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.restapiv3.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.freedomotic.api.API;
import com.freedomotic.app.Freedomotic;

/**
 *
 * @author matteo
 */
public abstract class AbstractWSResource {
    protected static final API api;
    protected static final ObjectMapper om;
    static {
        api = Freedomotic.INJECTOR.getInstance(API.class);
        om = new ObjectMapper();
    }
    
}
