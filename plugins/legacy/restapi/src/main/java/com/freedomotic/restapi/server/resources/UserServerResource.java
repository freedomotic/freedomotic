/**
 *
 * Copyright (c) 2009-2016 Freedomotic team
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

package com.freedomotic.restapi.server.resources;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ServerResource;

public class UserServerResource extends ServerResource {

    @Override
    public void doInit() {
        if (((String) getRequest().getAttributes().get("useraction")).equals("logout")) {
            getRequest().getClientInfo().setAuthenticated(false);
            getResponse().setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }
    }

    @Override
    public Representation get() {
        return null;
    }

    @Override
    public Representation post(Representation entity) {
        // Handle post
        // ...
        return null;
    }
}
