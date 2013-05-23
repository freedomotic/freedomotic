package it.freedomotic.gwtclient.client.utils;

import it.freedomotic.model.geometry.FreedomPoint;
import it.freedomotic.model.geometry.FreedomPolygon;

import java.awt.Color;
import java.awt.geom.Path2D;

public class DrawingUtils {

    //Helper class to transform from a FreedomPolygon to a Path
    public static Path2D freedomPolygonToPath(FreedomPolygon fp) {
        Path2D mP = new Path2D.Double();
        for (int j = 0; j < fp.getPoints().size(); j++) {
            FreedomPoint point = fp.getPoints().get(j);
            if (j == 0) {
                mP.moveTo(point.getX(), point.getY());
            } else {
                mP.lineTo(point.getX(), point.getY());
            }
        }
        //closing the path
        mP.closePath();

        return mP;
    }
    public static String nextValidColor = "FF000001";
    private static int redValue = 0;
    private static int greenValue = 0;
    private static int blueValue = 0;
    private static int alphaValue = 255;

    public static int generateNextValidColor() {
        int step = 1;
        redValue += step;
        if (redValue == 256) {
            greenValue += step;
            redValue = 0;
            if (greenValue == 256) {
                blueValue += step;
                greenValue = 0;
                if (blueValue == 256) {
                    System.out.println("We have reached the limit of the number of objects!! 255*255*255!!!");
                }
            }

        }
        Color c = new Color(redValue, greenValue, blueValue, alphaValue);
        return (c.getRGB());

    }
}
