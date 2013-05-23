package it.freedomotic.gwtclient.client.utils;

import com.google.gwt.canvas.dom.client.Context2d;

public abstract class DrawableElement {

    int indexColor;

    public DrawableElement() {
        setIndexColor();
    }

    public abstract String getName();

    public void setIndexColor() {
        this.indexColor = DrawingUtils.generateNextValidColor();
    }

    public int getIndexColor() {
        return indexColor;
    }

    public abstract void draw(Context2d context);

    public abstract void drawGhost(Context2d context);
}
