package com.freedomotic.clients.client.utils;

import com.google.gwt.canvas.dom.client.Context2d;
import com.levigo.util.gwtawt.client.WebGraphics;
import com.google.gwt.canvas.client.Canvas;

import java.awt.Shape;
import java.awt.Color;

public abstract class DrawableElement {

    int indexColor;
    protected double mScaleFactor = 1;
    protected double mPosX = 0;
    protected double mPosY = 0;
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
    public void OnDoubleClick(Canvas canvas)
    {
    }
    //end region


    public void beforeDraw(Context2d context, Context2d indexContext){
    }

    public void draw(Context2d context, Context2d indexContext)
    {
        beforeDraw(context, indexContext);

        context.save();
        indexContext.save();

        context.scale(mScaleFactor, mScaleFactor);
        //context.translate(BORDER_X, BORDER_Y);
        indexContext.scale(mScaleFactor, mScaleFactor);
        //indexContext.translate(BORDER_X, BORDER_Y);
        context.translate(mPosX,mPosY);
        indexContext.translate(mPosX,mPosY);

        paint(context, indexContext);

        indexContext.restore();
        context.restore();

    }

    public abstract void paint(Context2d context, Context2d indexContext);

    public void paintIndex(Context2d indexContext)
    {
        //Draw over the ghost
        Color c = new Color(getIndexColor());
        //I think this doesn't eed to be here
        WebGraphics gIndex = new WebGraphics(indexContext);
        gIndex.setFillColor(c);
        gIndex.fill(elementBounds);

    }

    public abstract void updateElement();

    public void setScaleFactor(double mScaleFactor) {
        this.mScaleFactor = mScaleFactor;
    }


    public ExtendedCanvas getParentCanvas() {
        return parentCanvas;
    }

    public void setParentCanvas(ExtendedCanvas parentCanvas) {
        this.parentCanvas = parentCanvas;
    }
    public void setPosition(double posX, double posY)
    {
        setPosX(posX);
        setPosY(posY);
    }


    public double getPosX() {
        return mPosX;
    }

    public void setPosX(double posX) {
        this.mPosX = posX;
    }

    public double getPosY() {
        return mPosY;
    }

    public void setPosY(double posY) {
        this.mPosY = posY;
    }
}
