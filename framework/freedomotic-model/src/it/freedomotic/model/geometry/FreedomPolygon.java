package it.freedomotic.model.geometry;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Enrico
 */
public class FreedomPolygon implements FreedomShape, Serializable {

    private ArrayList<FreedomPoint> points = new ArrayList<FreedomPoint>();

    public FreedomPolygon() {
    }

    public void append(FreedomPoint point) {
        points.add(point);
    }

    public void append(int x, int y) {
        FreedomPoint point = new FreedomPoint(x, y);
        points.add(point);
    }

    public FreedomPoint insert(FreedomPoint nextTo) {
        int index = points.indexOf(nextTo);
        FreedomPoint currentPoint = null;
        FreedomPoint nextPoint = null;
        try {
            nextPoint = points.get((index + 1) % points.size());
            currentPoint = points.get(index);
        } catch (Exception e) {
        }
        if (currentPoint != null && nextPoint != null) {
            int x = (int) Math.abs((currentPoint.getX() - nextPoint.getX())) / 2;
            int y = (int) Math.abs((currentPoint.getY() - nextPoint.getY())) / 2;
            FreedomPoint newPoint = new FreedomPoint(x, y);
            points.add(index + 1, newPoint);
            return newPoint;
        }
        return null;
    }

    public void remove(FreedomPoint point) {
        points.remove(point);
    }

    public ArrayList<FreedomPoint> getPoints() {
        if (points != null) {
            return points;
        } else {
            return new ArrayList<FreedomPoint>();
        }
    }
    
    @Override
    public String toString(){
        StringBuilder buff = new StringBuilder();
        buff.append(points.size() + " points ");
        for (FreedomPoint p : points) {
            buff.append("(").append(p.getX()).append(",").append(p.getY()).append(")");
        }
        return buff.toString();
        
    }
}
