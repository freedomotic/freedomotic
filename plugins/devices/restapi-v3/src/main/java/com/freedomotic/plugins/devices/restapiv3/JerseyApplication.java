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
package com.freedomotic.plugins.devices.restapiv3;

import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.JERSEY_RESOURCE_PKG;
import static com.freedomotic.plugins.devices.restapiv3.RestAPIv3.ATMOSPHRE_RESOURCE_PKG;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author matteo
 */
public class JerseyApplication extends ResourceConfig {
    
    public JerseyApplication(){
        packages(JERSEY_RESOURCE_PKG, ATMOSPHRE_RESOURCE_PKG)
        .register(JacksonFeature.class);
    }
}
