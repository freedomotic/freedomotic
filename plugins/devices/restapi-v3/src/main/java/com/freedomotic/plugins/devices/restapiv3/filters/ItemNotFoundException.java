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
package com.freedomotic.plugins.devices.restapiv3.filters;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

/**
 *
 * @author Matteo Mazzoni
 */
public class ItemNotFoundException extends WebApplicationException {

    /**
     * Create a HTTP 404 (Not Found) exception.
     */
    public ItemNotFoundException() {
        super(Response.status(Response.Status.NOT_FOUND).entity("404: Item not found").type("text/plain").build());
    }

    /**
     * Create a HTTP 404 (Not Found) exception.
     *
     * @param message the String that is the entity of the 404 response.
     */
    public ItemNotFoundException(String message) {
        super(Response.status(Response.Status.NOT_FOUND)
                .entity(message).type("text/plain").build());
    }
}
