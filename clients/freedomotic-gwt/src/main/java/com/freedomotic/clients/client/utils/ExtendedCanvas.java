package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.widgets.EnvObjectProperties;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by gpt on 31/03/14.
 */
public class ExtendedCanvas {

    Canvas canvas;
    Context2d ctx;
    Canvas ghostCanvas;
    Context2d gctx;

    // Map of objects that are drawed
    LinkedHashMap<Integer, DrawableElement> objectsIndex = new LinkedHashMap<Integer, DrawableElement>();
    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private static int CANVAS_WIDTH = 1300 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 900 + (BORDER_X * 2);

    private double mScaleFactor = 1;
    private double mPosX;
    private double mPosY;
    //private DockLayoutPanel parent;



    public ExtendedCanvas() {
        canvas = Canvas.createIfSupported();
        ctx = canvas.getContext2d();
        ghostCanvas = Canvas.createIfSupported();
        gctx = ghostCanvas.getContext2d();
        objectsIndex.clear();


    }
    void initCanvas()
    {
        objectsIndex.clear();
    }
    void setSize(int width, int height)
    {
        CANVAS_WIDTH = width;
        CANVAS_HEIGHT = height;
        canvas.setWidth(width + "px");
        canvas.setHeight(height + "px");
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);

        ghostCanvas.setWidth(width + "px");
        ghostCanvas.setHeight(height + "px");
        ghostCanvas.setCoordinateSpaceWidth(width);
        ghostCanvas.setCoordinateSpaceHeight(height);

    }
    void addDrawingElement(DrawableElement de)
    {
        de.setIndexColor(generateNextValidColor());
        objectsIndex.put(de.getIndexColor(), de);
    }

    DrawableElement elementUnderMouse;
    void registerHandlers()
    {
        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final DrawableElement de = getElementUnderCoordinates(event.getX(), event.getY());
                if (de != null ){
                    de.OnClick(canvas);
                }

            }
        });

        canvas.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
                final DrawableElement de =  getElementUnderCoordinates(event.getX(), event.getY());
                if ((de == null && elementUnderMouse!= null) || (de != null && elementUnderMouse!= null && de != elementUnderMouse))
                    elementUnderMouse.OnMouseLeft(canvas);
                if (de != null)
                {
                    de.OnMouseOver(canvas);
                    elementUnderMouse = de;
                }
            }
        });

    }


   void draw() {
        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);

        for (DrawableElement de : objectsIndex.values()) {
            de.draw(this.getContext(), this.getIndexContext());
    }


    }
    void updateElements()
    {
        for (DrawableElement de : objectsIndex.values()) {
            de.setScaleFactor(mScaleFactor);
            de.updateElement();
        }
    }

    public DrawableElement getElementUnderCoordinates(int x, int y) {
        if (gctx != null) {
            int posX = 0;
            int posY = 0;
            //retrieve the color under the click on the ghost canvas
            ImageData id = gctx.getImageData(x, y, 1, 1);
            Color c = new Color(id.getRedAt(posX, posY), id.getGreenAt(posX, posY), id.getBlueAt(posX, posY), id.getAlphaAt(posX, posY));
            //GWT.log(Integer.toString(c.getRGB()));
            if (objectsIndex.containsKey(c.getRGB()));
            {
                if (objectsIndex.get(c.getRGB()) != null) {
                    DrawableElement de = objectsIndex.get(c.getRGB());
                    return de;
                }
            }
        }
        return null;
    }

    public Canvas getCanvas() {
        return canvas;
    }


    public Context2d getContext()
    {
        return ctx;
    }

    public Context2d getIndexContext()
    {
        return gctx;
    }


    //Adapt the "original coordinates" from freedomotic to the canvas size
    public void fitToScreen(double width, double height) {

        double xSize = CANVAS_WIDTH - BORDER_X;
        double ySize = CANVAS_HEIGHT - BORDER_Y - 200;

        double xPathSize = width;
        double yPathSize = height;

        double xScale = xSize / xPathSize;
        double yScale = ySize / yPathSize;
        if (xScale < yScale) {
            mScaleFactor = xScale;
        } else {
            mScaleFactor = yScale;
        }
        mPosX = 0;//MARGIN;
        mPosY = 0;//MARGIN;
        updateElements();

    }

    //Handle the objects that are being drawn in the canvas

    private int redValue = 0;
    private int greenValue = 0;
    private int blueValue = 0;
    private int alphaValue = 255;

    public int generateNextValidColor() {
        int step = 1;
        redValue += step;
        if (redValue >= 256) {
            greenValue += step;
            redValue = 0;
            if (greenValue >= 256) {
                blueValue += step;
                greenValue = 0;
                if (blueValue >= 256) {
                    System.out.println("We have reached the limit of the number of objects!! 255*255*255!!!");
                }
            }

        }
        Color c = new Color(redValue, greenValue, blueValue, alphaValue);
        return (c.getRGB());

    }


    public double getmScaleFactor() {
        return mScaleFactor;
    }
}
