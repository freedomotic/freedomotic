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
package com.freedomotic.security;

import java.util.UUID;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authz.permission.WildcardPermission;
import org.apache.shiro.realm.SimpleAccountRealm;

/**
 *
 * @author Matteo Mazzoni
 */
class PluginRealm extends SimpleAccountRealm {

    public final static String PLUGIN_REALM_NAME = "com.freedomotic.plugins.security";
    public final static String DEFAULT_PERMISSION = "*";

    public PluginRealm() {
        setName(PLUGIN_REALM_NAME);
    }

    public void addPlugin(String pluginName, String permissions) {
        SimpleAccount pluginUser = new SimpleAccount(pluginName, UUID.randomUUID().toString(), getName());
        pluginUser.addObjectPermission(new WildcardPermission(permissions));
        this.add(pluginUser);
    }

}
