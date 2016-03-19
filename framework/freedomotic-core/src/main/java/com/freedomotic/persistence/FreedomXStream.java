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

import com.freedomotic.core.Condition;
import com.freedomotic.environment.Room;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomEllipse;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.geometry.FreedomShape;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.freedomotic.reactions.Command;
import com.freedomotic.rules.Payload;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.rules.Statement;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.security.User;
import com.thoughtworks.xstream.XStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import org.apache.commons.io.IOUtils;
import org.apache.shiro.authz.SimpleRole;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class FreedomXStream {

    private static XStream xstream = null;
    private static final Logger LOG = LoggerFactory.getLogger(FreedomXStream.class.getName());

    @Inject
    private static ReactionConverter reactionConverter;

    /**
     * Creates a new fully configured serialization engine object which can be
     * used to convert java instances into text (xml, json [not yet supported])
     *
     * @return
     */
    public static XStream getXstream() {
        if (xstream == null) {
            // Generic configuration
            xstream = new XStream();
            xstream.setMode(XStream.NO_REFERENCES);
            xstream.autodetectAnnotations(true);

            // Things
            xstream.omitField(EnvObject.class, "LOG");

            // Geometry
            xstream.alias("polygon", FreedomPolygon.class);
            xstream.addImplicitCollection(FreedomPolygon.class, "points", "point", FreedomPoint.class);
            xstream.alias("ellipse", FreedomEllipse.class);
            xstream.alias("point", FreedomPoint.class);
            xstream.useAttributeFor(FreedomPoint.class, "x");
            xstream.useAttributeFor(FreedomPoint.class, "y");
            xstream.alias("shape", FreedomShape.class);
            xstream.alias("view", Representation.class);

            // Commands
            xstream.omitField(Config.class, "xmlFile");
            xstream.registerLocalConverter(Config.class, "tuples", new TupleConverter());

            // Zones and topology
            xstream.alias("object", EnvObject.class);
            xstream.alias("environment", Environment.class);
            xstream.alias("zone", Zone.class);
            xstream.omitField(Zone.class, "occupiers");
            xstream.omitField(Room.class, "gates");
            xstream.omitField(Room.class, "reachable");
            xstream.omitField(Environment.class, "occupiers");
            xstream.omitField(Zone.class, "objects");

            // Triggers and commands
            xstream.alias("trigger", Trigger.class);
            xstream.alias("statement", Statement.class);
            xstream.alias("command", Command.class);
            xstream.alias("reaction", Reaction.class);
            xstream.alias("condition", Condition.class);
            xstream.omitField(Trigger.class, "suspensionStart");
            xstream.omitField(Trigger.class, "listener");
            xstream.omitField(Trigger.class, "checker");
            xstream.omitField(Trigger.class, "busService");
            xstream.alias("payload", Payload.class);

            // Register custom converters
            xstream.registerConverter(reactionConverter);
            xstream.registerConverter(new PayloadConverter());
            xstream.registerConverter(new PropertiesConverter());
            xstream.registerConverter(new TupleConverter());
            xstream.alias("user", User.class);
            xstream.alias("users", User[].class);
            xstream.registerConverter(new UserConverter());
            xstream.alias("role", SimpleRole.class);
            xstream.alias("roles", SimpleRole[].class);
            xstream.registerConverter(new RoleConverter());
        }

        return xstream;
    }

    public static boolean toXML(Object object, File file) {
        XStream serializer = getXstream();
        OutputStream outputStream = null;
        Writer writer = null;

        try {
            outputStream = new FileOutputStream(file);
            writer = new OutputStreamWriter(outputStream, Charset.forName("UTF-8"));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            serializer.toXML(object, writer);
        } catch (Exception exp) {
            LOG.error("Error while serializing instance to disk", exp);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(outputStream);
        }

        return true;
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static XStream getEnviromentXstream() {
        return getXstream();
    }

    private FreedomXStream() {
        //disable instantiation
    }
}
