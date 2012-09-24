package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.geometry.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

/**
 *
 * @author Enrico
 */
public class AWTConverter {

    private AWTConverter() {
    }

    /**
     * Converts a Freedomotic Shape into an AWT Shape Remember that modifiers
     * like rotation and offset are not applyed
     *
     * @param input
     * @return
     */
    public static Shape convertToAWT(FreedomShape input) {
        if (input instanceof FreedomPolygon) {
            return convertToAWT((FreedomPolygon) input);
        } else {
            if (input instanceof FreedomEllipse) {
                return convertToAWT((FreedomEllipse) input);
            } else {
                throw new IllegalArgumentException("The kind of shape in input is unknown");
            }
        }
    }

    public static Shape convertToAWT(FreedomShape input, double xScale, double yScale) {
        if (input instanceof FreedomPolygon) {
            Shape shape = convertToAWT((FreedomPolygon) input);
            AffineTransform transform = new AffineTransform();
            transform.scale(xScale, yScale);
            Shape transformed = transform.createTransformedShape(shape);
            return transformed;
        } else {
            if (input instanceof FreedomEllipse) {
                Shape shape = convertToAWT((FreedomEllipse) input);
                AffineTransform transform = new AffineTransform();
                transform.scale(xScale, yScale);
                Shape transformed = transform.createTransformedShape(shape);
                return transformed;
            } else {
                throw new IllegalArgumentException("The kind of shape in input is unknown");
            }
        }
    }

    public static Color convertColorToAWT(FreedomColor color) {
        Color awtColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
        return awtColor;
    }

    private static Point convertToAWT(FreedomPoint fPoint) {
        return new Point(fPoint.getX(), fPoint.getY());
    }

    private static Ellipse2D convertToAWT(FreedomEllipse fEllipse) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private static Polygon convertToAWT(FreedomPolygon input) {
        FreedomPolygon polygon = (FreedomPolygon) input;
        Polygon output = new Polygon();
        for (FreedomPoint point : polygon.getPoints()) {
            output.addPoint(point.getX(), point.getY());
        }
        return output;
    }

    public static FreedomShape translate(FreedomShape input, int xoffset, int yoffset) {
        if (input instanceof FreedomPolygon) {
            //a low CPU translation system
            return (translate((FreedomPolygon) input, xoffset, yoffset));
        } else {
            //use awt translation here
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }

    private static FreedomPolygon translate(FreedomPolygon input, int xoffset, int yoffset) {
        FreedomPolygon output = new FreedomPolygon();
        for (FreedomPoint point : input.getPoints()) {
            output.append(point.getX() + xoffset, point.getY() + yoffset);
        }
        return output;
    }

    public static FreedomShape rotate(FreedomShape input, int rotation) {
        //use awt rotation
        throw new UnsupportedOperationException("Not yet implemented");
    }

//    public static boolean circleContainsPoint(int destX, int destY, int RADIUS, FreedomPoint currentLocation) {
//        Circle dest = new Circle((destX - (RADIUS / 2)), (destY - (RADIUS / 2)), RADIUS, RADIUS);
//        return dest.contains(convertToAWT(currentLocation));
//    }
    public static boolean intersects(FreedomShape fShape, FreedomShape fPolygon) {
        Shape shape = AWTConverter.convertToAWT(fShape);
        Shape polygon = AWTConverter.convertToAWT(fPolygon);

        return shape.intersects(polygon.getBounds2D());
    }

    public static boolean contains(FreedomShape fShape, FreedomPoint fPoint) {
        Shape shape = convertToAWT(fShape);
        Point point = convertToAWT(fPoint);
        return shape.contains(point);
    }
}
