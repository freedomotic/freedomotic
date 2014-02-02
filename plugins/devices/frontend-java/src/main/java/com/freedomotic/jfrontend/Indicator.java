package com.freedomotic.jfrontend;

import java.awt.Color;
import java.awt.Shape;

/**
 * Created with IntelliJ IDEA.
 * User: Bastiaan Visser
 * Date: 7/2/13
 * Time: 10:54 AM
 */
public class Indicator
{
    private Shape shape;
    private Color color = new Color(0, 0, 255, 50);

    /**
     *
     * @param shape
     */
    public Indicator(Shape shape) {
        this.shape = shape;
    }

    /**
     *
     * @param shape
     * @param color
     */
    public Indicator(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    /**
     *
     * @return
     */
    public Shape getShape() {
        return shape;
    }

    /**
     *
     * @return
     */
    public Color getColor() {
        return color;
    }
}
