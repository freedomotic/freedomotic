package it.freedomotic.jfrontend;

import java.awt.*;

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


    public Indicator(Shape shape) {
        this.shape = shape;
    }

    public Indicator(Shape shape, Color color) {
        this.shape = shape;
        this.color = color;
    }

    public Shape getShape() {
        return shape;
    }

    public Color getColor() {
        return color;
    }
}
