package it.freedomotic.util;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.model.geometry.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

/**
 *
 * @author Enrico
 */
public class TopologyUtils {

    private TopologyUtils() {
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

    public static FreedomPolygon translate(FreedomShape input, int xoffset, int yoffset) {
        if (input instanceof FreedomPolygon) {
            return (translate((FreedomPolygon) input, xoffset, yoffset));
        } else {
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

    public static FreedomPolygon rotate(FreedomPolygon input, int degrees) {
        FreedomPoint pivot = input.getPoints().get(0);//getRectangleCenter(getBoundingBox(input));
        FreedomPolygon output = new FreedomPolygon();
        for (FreedomPoint point : input.getPoints()) {
            output.append(rotatePoint(point, pivot, degrees));
        }
        return output;
    }

    private static FreedomPolygon getBoundingBox(FreedomPolygon input) {
        int minx = Integer.MAX_VALUE,
                miny = Integer.MAX_VALUE,
                maxx = Integer.MIN_VALUE,
                maxy = Integer.MIN_VALUE;
        for (FreedomPoint p : input.getPoints()) {
            minx = Math.min(minx, p.getX());
            miny = Math.min(miny, p.getY());
            maxx = Math.max(maxx, p.getX());
            maxy = Math.max(maxy, p.getY());
        }
        FreedomPolygon poly = new FreedomPolygon();
        poly.append(minx, miny);
        poly.append(maxx, miny);
        poly.append(maxx, maxy);
        poly.append(minx, maxy);
        return poly;
    }

    private static FreedomPoint getRectangleCenter(FreedomPolygon input) {
        FreedomPoint min = input.getPoints().get(0);
        FreedomPoint max = input.getPoints().get(2);
        int x = ((max.getX() - min.getX()) / 2) + min.getX();
        int y = ((max.getY() - min.getY() / 2) + min.getY());
        return new FreedomPoint(x, y);
    }

    /**
     * taken from
     * http://stackoverflow.com/questions/10533403/how-to-rotate-a-polygon-around-a-point-with-java
     * all credits to respective authors
     *
     *
     */
    private static FreedomPoint rotatePoint(FreedomPoint pt, FreedomPoint pivot, double degrees) {
        double radians = Math.toRadians(degrees);
        double cosAngle = Math.cos(radians);
        double sinAngle = Math.sin(radians);

        int x = (int) Math.round(pivot.getX() + (double) ((pt.getX() - pivot.getX()) * cosAngle - (pt.getY() - pivot.getY()) * sinAngle));
        int y = (int) Math.round(pivot.getY() + (double) ((pt.getX() - pivot.getX()) * sinAngle + (pt.getY() - pivot.getY()) * cosAngle));
        return new FreedomPoint(x, y);
    }

    /**
     * checks if some of the edge of source polygon is inside target polygon
     * area
     *
     * @param source
     * @param target
     * @return
     */
    public static boolean intersects(FreedomPolygon source, FreedomPolygon target) {
//        Shape shape = AWTConverter.convertToAWT(fShape);
//        Shape polygon = AWTConverter.convertToAWT(fPolygon);
//
//        return shape.intersects(polygon.getBounds2D());
//        for (int i = 1; i < fShape.getPoints().size(); i++) {
//            //get a segment from fShape
//            FreedomPoint p1 = fShape.getPoints().get(i - 1);
//            FreedomPoint p2 = fShape.getPoints().get(i);
//            //check this segment instersections with all segments on the other poly
//            for (int j = 1; j < fPolygon.getPoints().size(); j++) {
//                FreedomPoint p3 = fPolygon.getPoints().get(j - 1);
//                FreedomPoint p4 = fPolygon.getPoints().get(j);
//                Point intersection = intersection(
//                        p1.getX(), p1.getY(),
//                        p2.getX(), p2.getY(),
//                        p3.getX(), p3.getY(),
//                        p4.getX(), p4.getY());
//                if (intersection != null) {
//                    System.out.println("intersection in " +  intersection +" \n (segment " +i + "/"+j+")");
//                    return true;
//                }
//            }
//        }
//        return false;
        boolean sourceInside=false;
        boolean targetInside=false;
        for (FreedomPoint edge : source.getPoints()) {
            if (contains(target, edge)) {
                sourceInside = true;
            }
        }
        for (FreedomPoint edge : target.getPoints()) {
            if (contains(source, edge)) {
                targetInside = true;
            }
        }
        return (sourceInside || targetInside);
    }

//    /**
//     * Computes the intersection between two lines. The calculated point is
//     * approximate, since integers are used. If you need a more precise result,
//     * use doubles everywhere. (c) 2007 Alexander Hristov. Use Freely (LGPL
//     * license). http://www.ahristov.com
//     *
//     * @param x1 Point 1 of Line 1
//     * @param y1 Point 1 of Line 1
//     * @param x2 Point 2 of Line 1
//     * @param y2 Point 2 of Line 1
//     * @param x3 Point 1 of Line 2
//     * @param y3 Point 1 of Line 2
//     * @param x4 Point 2 of Line 2
//     * @param y4 Point 2 of Line 2
//     * @return Point where the segments intersect, or null if they don't
//     */
//    private static Point intersection(
//            int x1, int y1, int x2, int y2,
//            int x3, int y3, int x4, int y4) {
//        int d = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);
//        if (d == 0) {
//            return null;
//        }
//
//        int xi = ((x3 - x4) * (x1 * y2 - y1 * x2) - (x1 - x2) * (x3 * y4 - y3 * x4)) / d;
//        int yi = ((y3 - y4) * (x1 * y2 - y1 * x2) - (y1 - y2) * (x3 * y4 - y3 * x4)) / d;
//
//        return new Point(xi, yi);
//    }
    /**
     * Checks if a point is inside the polygon.
     * @param fShape
     * @param fPoint
     * @return true if inside, false if on border or outside
     */
    public static boolean contains(FreedomShape fShape, FreedomPoint fPoint) {
//        Shape shape = convertToAWT(fShape);
//        Point point = convertToAWT(fPoint);
//        return shape.contains(point);
        ArrayList<Float> lx = new ArrayList<Float>();
        ArrayList<Float> ly = new ArrayList<Float>();
        int verticesNum = 0;
        float px = fPoint.getX();
        float py = fPoint.getY();
        if (fShape instanceof FreedomPolygon) {
            FreedomPolygon poly = (FreedomPolygon) fShape;
            verticesNum = poly.getPoints().size();
            for (int i = 0; i < poly.getPoints().size(); i++) {
                lx.add((float) poly.getPoints().get(i).getX());
                ly.add((float) poly.getPoints().get(i).getY());
            }
        }

        //TODO: converting: change this code please
        float x[] = new float[lx.size()];
        for (int i = 0; i < lx.size(); i++) {
            Float f = lx.get(i);
            x[i] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }
        //TODO: converting: change this code please
        float y[] = new float[ly.size()];
        for (int i = 0; i < ly.size(); i++) {
            Float f = ly.get(i);
            y[i] = (f != null ? f : Float.NaN); // Or whatever default you want.
        }

        //algorithm starts
        if (verticesNum < 3) {
            return false;
        }

        boolean oddNodes = false;
        float x2 = x[verticesNum - 1];
        float y2 = y[verticesNum - 1];
        float x1, y1;
        for (int i = 0; i < verticesNum; x2 = x1, y2 = y1, ++i) {
            x1 = x[i];
            y1 = y[i];
            if (((y1 < py) && (y2 >= py))
                    || (y1 >= py) && (y2 < py)) {
                if ((py - y1) / (y2 - y1)
                        * (x2 - x1) < (px - x1)) {
                    oddNodes = !oddNodes;
                }
            }
        }
        return oddNodes;
    }
}
