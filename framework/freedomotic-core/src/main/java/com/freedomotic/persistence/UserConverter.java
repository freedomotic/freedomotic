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
package com.freedomotic.persistence;

import com.freedomotic.app.Freedomotic;
import com.freedomotic.security.Auth;
import com.freedomotic.security.User;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import org.apache.shiro.codec.Base64;
import org.apache.shiro.util.ByteSource;
import org.slf4j.LoggerFactory;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;

/**
 *
 * @author Matteo Mazzoni
 */
class UserConverter implements Converter {

    private static final Logger LOG = LoggerFactory.getLogger(UserConverter.class.getName());

    /**
     *
     * @param o
     * @param writer
     * @param mc
     */
    @Override
    public void marshal(Object o, HierarchicalStreamWriter writer, MarshallingContext mc) {

        User user = (User) o;
        writer.startNode("principals");
        for (String realm : user.getPrincipals().getRealmNames()) {
            for (Object p : user.getPrincipals().fromRealm(realm)) {
                writer.startNode("principal");
                writer.addAttribute("realm", realm);
                writer.addAttribute("primary", (p.toString().equals(user.getPrincipals().getPrimaryPrincipal().toString()) ? "true" : "false"));
                writer.setValue(p.toString());
                writer.endNode();
            }
        }
        writer.endNode();
        writer.startNode("credentials");
        writer.setValue(user.getCredentials().toString());
        writer.endNode();
        writer.startNode("salt");
        writer.setValue(user.getCredentialsSalt().toBase64());
        writer.endNode();
        writer.startNode("roles");
        for (String r : user.getRoles()) {
            writer.startNode("role");
            writer.addAttribute("name", r);
            writer.endNode();
        }
        writer.endNode();
        writer.startNode("properties");
        mc.convertAnother(user.getProperties());
        writer.endNode();

    }

    /**
     *
     * @param reader
     * @param uc
     * @return
     */
    @Override
    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext uc) {
        SimplePrincipalCollection pc = new SimplePrincipalCollection();
        User user = null;
        reader.moveDown(); //principals
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            String realm = reader.getAttribute("realm");
            pc.add(reader.getValue(), realm);
            // reader.getAttribute("primary"); // ???
            reader.moveUp();
        }
        reader.moveUp(); //end principals
        reader.moveDown(); // credentials
        user = new User(pc, reader.getValue(), Freedomotic.INJECTOR.getInstance(Auth.class));
        reader.moveUp(); // end credentials
        reader.moveDown(); //salt
        user.setCredentialsSalt(ByteSource.Util.bytes(Base64.decode(reader.getValue())));
        reader.moveUp();
        reader.moveDown();
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            user.addRole(reader.getAttribute("name"));
            reader.moveUp();
        }
        reader.moveUp();
        reader.moveDown(); //properties
        while (reader.hasMoreChildren()) {
            reader.moveDown();
            user.setProperty(reader.getAttribute("name"), reader.getAttribute("value"));
            reader.moveUp();
        }
        reader.moveUp();
        return user;
    }

    /**
     *
     * @param type
     * @return
     */
    @Override
    public boolean canConvert(Class type) {
        return (type == User.class);
    }
}
