/**
 *
 * Copyright (c) 2009-2014 Freedomotic team
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
package com.freedomotic.plugins.devices.japi.resources;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.environment.EnvironmentPersistence;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author matteo
 */
@Path("environments")
public class EnvironmentResource extends AbstractResource<Environment> {

    @Override
    protected List<Environment> prepareList() {
        List<Environment> environments = new ArrayList<Environment>();
        for (EnvironmentLogic log : EnvironmentPersistence.getEnvironments()) {
            environments.add(log.getPojo());
        }
        return environments;
    }

    @Override
    protected Environment prepareSingle(String uuid) {
        return EnvironmentPersistence.getEnvByUUID(uuid).getPojo();
    }

    @Override
    protected boolean doDelete(String UUID) {
        EnvironmentLogic env = EnvironmentPersistence.getEnvByUUID(UUID);
        if (env != null) {
            EnvironmentPersistence.remove(EnvironmentPersistence.getEnvByUUID(UUID));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected URI doCreate(Environment eo) throws URISyntaxException {
        EnvironmentLogic el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(eo);
        EnvironmentPersistence.add(el, false);
        return UriBuilder.fromResource(this.getClass()).path(el.getPojo().getUUID()).build();
    }

    @Override
    protected Environment doUpdate(Environment eo) {
        EnvironmentLogic el = Freedomotic.INJECTOR.getInstance(EnvironmentLogic.class);
        el.setPojo(eo);
        EnvironmentPersistence.remove(EnvironmentPersistence.getEnvByUUID(eo.getUUID()));
        EnvironmentPersistence.add(el, false);
        return eo;
    }

}
