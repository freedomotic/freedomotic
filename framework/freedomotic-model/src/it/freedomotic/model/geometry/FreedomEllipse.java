package it.freedomotic.model.geometry;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class FreedomEllipse implements FreedomShape, Serializable {

    private FreedomPoint center;
    private int xRadius;
    private int yRadius;

    public FreedomEllipse(FreedomPoint center, int xRadius, int yRadius) {
        this.center = center;
        this.xRadius = xRadius;
        this.yRadius = yRadius;
    }

        public FreedomEllipse(int x, int y, int xRadius, int yRadius) {
        this.center = new FreedomPoint(x,y);
        this.xRadius = xRadius;
        this.yRadius = yRadius;
    }
}
