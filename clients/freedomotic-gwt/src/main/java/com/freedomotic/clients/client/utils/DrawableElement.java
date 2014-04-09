package com.freedomotic.clients.client.utils;

import com.google.gwt.canvas.dom.client.Context2d;
import com.levigo.util.gwtawt.client.WebGraphics;
import com.google.gwt.canvas.client.Canvas;

import java.awt.Shape;
import java.awt.Color;

public abstract class DrawableElement {

    int indexColor;
    protected Shape elementBounds;
    protected ExtendedCanvas parentCanvas;

    public DrawableElement() {

    }


    public abstract String getName();

    public void setIndexColor(int indexColor) {
        this.indexColor = indexColor;
    }

    public int getIndexColor() {
        return indexColor;
    }

    //region mouseEvents
    public void OnMouseOver(Canvas canvas)
    {}

    public void OnMouseLeft(Canvas canvas)
    {}

    public void OnClick(Canvas canvas)
    {
    }
    public boolean OnDoubleClick(Canvas canvas)
    {
        return false;
    }
    //end region


    public void beforeDraw(Context2d context, Context2d indexContext){
    }

    public void draw(Context2d context, Context2d indexContext)
    {
        beforeDraw(context, indexContext);
        paint(context, indexContext);

    }

    public abstract void paint(Context2d context, Context2d indexContext);

    public void paintIndex(Context2d indexContext)
    {
        //Draw over the ghost
        Color c = new Color(getIndexColor());
        //I think this doesn't eed to be here
        WebGraphics gIndex = new WebGraphics(indexContext);

        gIndex.setFillColor(c);
        gIndex.setColor(c);
        gIndex.draw(elementBounds);
        gIndex.fill(elementBounds);

    }

    public abstract void updateElement();


    public ExtendedCanvas getParentCanvas() {
        return parentCanvas;
    }

    public void setParentCanvas(ExtendedCanvas parentCanvas) {
        this.parentCanvas = parentCanvas;
    }

}
