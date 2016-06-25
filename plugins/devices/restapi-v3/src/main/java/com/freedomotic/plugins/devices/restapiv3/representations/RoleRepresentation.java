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
package com.freedomotic.plugins.devices.restapiv3.representations;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.shiro.authz.Permission;
import org.apache.shiro.authz.SimpleRole;
import org.apache.shiro.authz.permission.WildcardPermission;

/**
 *
 * @author Matteo Mazzoni
 */
@XmlRootElement
public class RoleRepresentation {

    private String name;
    private final List<String> permissions = new ArrayList<String>();

    public RoleRepresentation() {
    }

    public RoleRepresentation(SimpleRole sr) {
        this.name = sr.getName();
        for (Permission p : sr.getPermissions()) {
            this.permissions.add(p.toString().replace("[", "").replace("]", "").replace(" ", ""));
        }
    }

    public String getName() {
        return name;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPermissions(List<String> perms) {
        this.permissions.clear();
        for (String s : perms) {
            this.permissions.add(s);
        }
    }

    public SimpleRole asSimpleRole() {
        SimpleRole sr = new SimpleRole(name);
        for (String s : permissions) {
            sr.add(new WildcardPermission(s));
        }
        return sr;
    }
}
