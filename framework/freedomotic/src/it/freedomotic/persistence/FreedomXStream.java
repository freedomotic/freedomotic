package it.freedomotic.persistence;

import it.freedomotic.persistence.converters.TupleConverter;
import com.thoughtworks.xstream.XStream;
import it.freedomotic.environment.Room;
import it.freedomotic.model.environment.Environment;
import it.freedomotic.model.environment.Zone;
import it.freedomotic.model.ds.Config;
import it.freedomotic.model.geometry.FreedomEllipse;
import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.model.geometry.FreedomShape;
import it.freedomotic.model.object.Representation;
import it.freedomotic.persistence.converters.PayloadConverter;
import it.freedomotic.persistence.converters.PropertiesConverter;
import it.freedomotic.persistence.converters.ReactionConverter;
import it.freedomotic.reactions.Payload;
import it.freedomotic.reactions.Trigger;

/**
 *
 * @author gpt
 */
public final class FreedomXStream {

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
            xstream.registerLocalConverter(Config.class, "tuples", new TupleConverter());

            xstream.omitField(Zone.class, "occupiers");
            xstream.omitField(Room.class, "gates");
            xstream.omitField(Room.class, "reachable");

            /*
             * Initialization for Triggers
             */
            xstream.alias("trigger", Trigger.class);
            xstream.omitField(Trigger.class, "suspensionStart");
            xstream.omitField(Trigger.class, "busChannel");
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
        if (environmentXstream == null) {    //Enviroment serialization
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
