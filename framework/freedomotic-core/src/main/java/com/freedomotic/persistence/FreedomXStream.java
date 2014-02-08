/**
 *
 * Copyright (c) 2009-2013 Freedomotic team http://freedomotic.com
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

import com.freedomotic.environment.Room;
import com.freedomotic.model.ds.Config;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomEllipse;
import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.geometry.FreedomShape;
import com.freedomotic.model.object.Representation;
import com.freedomotic.reactions.Payload;
import com.freedomotic.reactions.ReactionConverter;
import com.freedomotic.reactions.Trigger;
import com.thoughtworks.xstream.XStream;

/**
 *
 * @author gpt
 */
public class FreedomXStream {

    private static XStream xstream = null;
    private static XStream environmentXstream = null;

    /**
     *
     * @return
     */
    public static XStream getXstream() {
        if (xstream == null) {
            // Generic configuration
            xstream = new XStream();
            xstream.setMode(XStream.NO_REFERENCES);
            xstream.autodetectAnnotations(true);

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
            xstream.omitField(Zone.class, "occupiers");
            xstream.omitField(Room.class, "gates");
            xstream.omitField(Room.class, "reachable");
            xstream.omitField(Environment.class, "occupiers");
            xstream.omitField(Zone.class, "objects");

            // Triggers and commands
            xstream.alias("trigger", Trigger.class);
            xstream.omitField(Trigger.class, "suspensionStart");
            xstream.omitField(Trigger.class, "listener");
            xstream.omitField(Trigger.class, "checker");
            xstream.alias("payload", Payload.class);

            // Register custom converters
            xstream.registerConverter(new ReactionConverter());
            xstream.registerConverter(new PropertiesConverter());
            xstream.registerConverter(new TupleConverter());
        }

        return xstream;
    }

    /**
     *
     * @return
     */
    @Deprecated
    public static XStream getEnviromentXstream() {
        if (environmentXstream == null) { //Enviroment serialization
            environmentXstream = new XStream();
            environmentXstream.setMode(XStream.NO_REFERENCES);
            environmentXstream.autodetectAnnotations(true);
            environmentXstream.omitField(Environment.class, "occupiers");
            environmentXstream.omitField(Zone.class, "objects");
            environmentXstream.alias("polygon", FreedomPolygon.class);
            environmentXstream.addImplicitCollection(FreedomPolygon.class, "points", "point", FreedomPoint.class);
            //adding also plain point xstream configuration
            environmentXstream.alias("point", FreedomPoint.class);
            environmentXstream.useAttributeFor(FreedomPoint.class, "x");
            environmentXstream.useAttributeFor(FreedomPoint.class, "y");
        }

        return environmentXstream;
    }

    private FreedomXStream() {
    }
}
