/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
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
package it.freedomotic.persistence;

import it.freedomotic.environment.Room;

import it.freedomotic.model.ds.Config;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.model.geometry.FreedomEllipse;
import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.model.geometry.FreedomShape;
import it.freedomotic.model.object.Representation;

import it.freedomotic.reactions.Payload;
import it.freedomotic.reactions.ReactionConverter;
import it.freedomotic.reactions.Trigger;

import com.thoughtworks.xstream.XStream;

/**
 *
 * @author gpt
 */
public class FreedomXStream /*implements FrameTranslator*/ {

    private static XStream xstream = null;
    private static XStream environmentXstream = null;

    private FreedomXStream() {
    }

    public static XStream getXstream() {
        if (xstream == null) {
            xstream = new XStream();
            xstream.autodetectAnnotations(true);
            xstream.alias("polygon", FreedomPolygon.class);
            xstream.addImplicitCollection(FreedomPolygon.class, "points", "point", FreedomPoint.class);
            xstream.alias("ellipse", FreedomEllipse.class);
            xstream.alias("point", FreedomPoint.class);
            xstream.useAttributeFor(FreedomPoint.class, "x");
            xstream.useAttributeFor(FreedomPoint.class, "y");
            xstream.alias("shape", FreedomShape.class);

//            xstream.aliasPackage("Object", "it.freedomotic.objects.impl");
//            xstream.alias("freedomObject", EnvObject.class);
//            xstream.omitField(EnvObjectLogic.class,"changed");
//            xstream.omitField(EnvObjectLogic.class,"commandsMapping");
//            xstream.omitField(EnvObjectLogic.class, "lastSentCommand");

//            xstream.omitField(Behavior.class, "masterObject");
//
//            xstream.alias("BooleanBehavior", BooleanBehavior.class);
//            xstream.omitField(BooleanBehavior.class, "listener");

//            xstream.omitField(ExclusiveMultivalueBehavior.class, "listener");

//            xstream.alias("RangedIntBehavior", RangedIntBehavior.class);
//            xstream.omitField(RangedIntBehavior.class, "listener");
            xstream.alias("view", Representation.class);

//            xstream.alias("Object.ElectricDevice", ElectricDevice.class);
//            xstream.omitField(ElectricDevice.class, "powered");
//
//            xstream.alias("Object.Gate", Gate.class);
//            xstream.omitField(Gate.class, "from");
//            xstream.omitField(Gate.class, "to");
//            xstream.omitField(Gate.class, "openess");
//            xstream.omitField(Gate.class, "open");
//            xstream.omitField(Gate.class, "visited");
//
//            xstream.alias("Object.Light", Light.class);
//            xstream.omitField(Light.class, "brightness");

            //No Object directly
            xstream.omitField(Config.class, "xmlFile");
            xstream.registerLocalConverter(Config.class,
                    "tuples",
                    new TupleConverter());

            xstream.omitField(Zone.class, "occupiers");
            xstream.omitField(Room.class, "gates");
            xstream.omitField(Room.class, "reachable");

            /*
             * Initialization for Triggers
             */
            xstream.alias("trigger", Trigger.class);
            xstream.omitField(Trigger.class, "suspensionStart");
            xstream.omitField(Trigger.class, "busChannel");
            xstream.omitField(Trigger.class, "checker");
            //xstream.registerConverter(new PayloadConverter());
            xstream.alias("payload", Payload.class);

//            xstream.omitField(ObjectReceiveClick.class, "obj");
//            xstream.omitField(ObjectReceiveClick.class, "click");
            xstream.registerConverter(new ReactionConverter());
            xstream.registerConverter(new PropertiesConverter());
            xstream.registerConverter(new TupleConverter());
        }

        return xstream;
    }

    public static XStream getEnviromentXstream() {
        if (environmentXstream == null) { //Enviroment serialization
            environmentXstream = new XStream();
            environmentXstream.setMode(XStream.NO_REFERENCES);
            environmentXstream.autodetectAnnotations(true);
            environmentXstream.omitField(Environment.class, "occupiers");
            environmentXstream.alias("polygon", FreedomPolygon.class);
            environmentXstream.addImplicitCollection(FreedomPolygon.class, "points", "point", FreedomPoint.class);
            //adding also plain point xstream configuration
            environmentXstream.alias("point", FreedomPoint.class);
            environmentXstream.useAttributeFor(FreedomPoint.class, "x");
            environmentXstream.useAttributeFor(FreedomPoint.class, "y");
        }

        return environmentXstream;
    }
}
