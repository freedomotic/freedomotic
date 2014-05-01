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

import com.freedomotic.model.object.EnvObject;
import com.freedomotic.objects.EnvObjectLogic;
import com.freedomotic.objects.EnvObjectPersistence;
import com.freedomotic.plugins.devices.japi.utils.AbstractResource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Path;

/**
 *
 * @author matteo
 */
@Path("objects")
public class ObjectResource extends AbstractResource<EnvObject> {

    @Override
    protected List<EnvObject> prepareList() {
        List<EnvObject> objects = new ArrayList<EnvObject>();
        for (EnvObjectLogic objLogic : EnvObjectPersistence.getObjectList()) {
            objects.add(objLogic.getPojo());
        }
        return objects;
    }

    @Override
    protected EnvObject prepareSingle(String uuid) {
        return EnvObjectPersistence.getObjectByUUID(uuid).getPojo();
    }

    @Override
    protected boolean doDelete(String UUID) {
        EnvObjectLogic obj = EnvObjectPersistence.getObjectByUUID(UUID);
        if (obj != null) {
            EnvObjectPersistence.remove(EnvObjectPersistence.getObjectByUUID(UUID));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected EnvObject doUpdate(EnvObject eo) {

        EnvObjectLogic el = new EnvObjectLogic();
            // set POJO!!!

        EnvObjectPersistence.add(el, false);
        return eo;

    }

    @Override
    protected URI doCreate(EnvObject eo) throws URISyntaxException {
        return new URI(eo.getUUID());
    }
}
