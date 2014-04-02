package com.freedomotic.clients.client.utils;

import com.freedomotic.model.geometry.FreedomPoint;
import com.freedomotic.model.geometry.FreedomPolygon;

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

}
