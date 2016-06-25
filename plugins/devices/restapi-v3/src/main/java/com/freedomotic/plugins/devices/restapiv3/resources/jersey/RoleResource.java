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
package com.freedomotic.plugins.devices.restapiv3.resources.jersey;

import com.freedomotic.plugins.devices.restapiv3.filters.ItemNotFoundException;
import com.freedomotic.plugins.devices.restapiv3.representations.RoleRepresentation;
import com.freedomotic.plugins.devices.restapiv3.utils.AbstractResource;
import com.freedomotic.security.User;
import com.wordnik.swagger.annotations.Api;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import org.apache.shiro.authz.SimpleRole;

/**
 *
 * @author Matteo Mazzoni
 */
@Path("system/roles")
@Api(value = "roles", description = "Operations on global roles")
public class RoleResource extends AbstractResource<RoleRepresentation> {

    @Override
    protected URI doCopy(String UUID) {
        SimpleRole sr = api.getAuth().getRole(UUID);
        if (sr != null) {
            SimpleRole s2 = new SimpleRole("CopyOf-" + sr.getName(), sr.getPermissions());
            api.getAuth().addRole(s2);
            return createUri(s2.getName());
        }
        return null;
    }

    @Override
    protected URI doCreate(RoleRepresentation o) throws URISyntaxException {
        if (api.getAuth().getRole(o.getName()) == null) {
            api.getAuth().addRole(o.asSimpleRole());
            return createUri(o.getName());
        } else {
            return null;
        }
    }

    @Override
    protected boolean doDelete(String UUID) {
        api.getAuth().deleteRole(UUID);
        return true;
    }

    @Override
    protected RoleRepresentation doUpdate(String uuid, RoleRepresentation o) {
        o.setName(uuid);
        SimpleRole sr = api.getAuth().getRole(o.getName());
        if (sr != null) {
            List<User> users = new ArrayList<User>();
            for (User u : api.getAuth().getUsers().values()) {
                if (u.getRoles().contains(o.getName())) {
                    users.add(u);
                }
            }
            api.getAuth().deleteRole(o.getName());
            api.getAuth().addRole(o.asSimpleRole());
            for (User u : users) {
                u.addRole(o.getName());
            }
            return o;
        }
        return null;
    }

    @Override
    protected List<RoleRepresentation> prepareList() {
        List<RoleRepresentation> rr = new ArrayList<RoleRepresentation>();
        for (SimpleRole r : api.getAuth().getRoles().values()) {
            rr.add(new RoleRepresentation(r));
        }
        return rr;
    }

    @Override
    protected RoleRepresentation prepareSingle(String uuid) {
        SimpleRole sr = api.getAuth().getRole(uuid);
        if (sr != null) {
            return new RoleRepresentation(sr);
        }
        return null;
    }

}
