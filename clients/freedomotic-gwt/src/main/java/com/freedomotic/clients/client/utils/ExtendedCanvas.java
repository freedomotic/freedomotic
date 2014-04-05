package com.freedomotic.clients.client.utils;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by gpt on 31/03/14.
 */
public class ExtendedCanvas {

    Canvas canvas;
    Context2d ctx;

    LinkedHashMap<String, Layer> layers = new LinkedHashMap<>();
    //we only accept one selected layer at a time.
    //TODO: Change to a structure that traverse the visible layers from top to bottom
    Layer selectedLayer = null;

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
    }

    public static int getCANVAS_WIDTH() {
        return CANVAS_WIDTH;
    }

    public static int getCANVAS_HEIGHT() {
        return CANVAS_HEIGHT;
    }

    void initCanvas()
    {
        layers.clear();
    }

    void setSize(int width, int height)
    {
        CANVAS_WIDTH = width;
        CANVAS_HEIGHT = height;
        canvas.setWidth(width + "px");
        canvas.setHeight(height + "px");
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        for(Layer layer: layers.values())
        {
            layer.setSize(width, height);
        }

    }

    void addDrawingElement(DrawableElement de, Layer layer)
    {
        //TODO: search for the layer and add the element
        layer.addObjectToLayer(de);
    }

    DrawableElement elementUnderMouse;
    void registerHandlers()
    {
        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                if (selectedLayer!= null) {
                    final DrawableElement de = selectedLayer.getElementUnderCoordinates(event.getX(), event.getY());
                    if (de != null) {
                        de.OnClick(canvas);
                    }
                }

            }
        });

        canvas.addMouseMoveHandler(new MouseMoveHandler() {
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
                if (selectedLayer!= null) {
                    final DrawableElement de = getElementUnderCoordinates(event.getX(), event.getY());
                    if ((de == null && elementUnderMouse != null) || (de != null && elementUnderMouse != null && de != elementUnderMouse))
                        elementUnderMouse.OnMouseLeft(canvas);
                    if (de != null) {
                        de.OnMouseOver(canvas);
                        elementUnderMouse = de;
                    }
                }
            }
        });

    }


    void draw() {
        ctx.clearRect(0, 0, getCANVAS_WIDTH(), getCANVAS_HEIGHT());
        for(Layer layer: layers.values())
        {
            layer.draw();
        }

    }

    void updateElements()
    {
        for(Layer layer: layers.values())
        {
            layer.updateElements();
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }


    public Context2d getContext()
    {
        return ctx;
    }


    //Adapt the "original coordinates" from freedomotic to the canvas size
    public void fitToScreen(double width, double height) {

        double xSize = getCANVAS_WIDTH() - BORDER_X;
        double ySize = getCANVAS_HEIGHT() - BORDER_Y - 200;

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

    public double getScaleFactor() {
        return mScaleFactor;
    }


    //region Layers Management

    public Layer addLayer(String objectUUID)
    {
        Layer newLayer = new Layer(this, objectUUID);
        layers.put(objectUUID, newLayer);
        return newLayer;

    }

    public void changeLayerVisibility(String objectUUID, boolean visibility)
    {
        Layer layer = layers.get(objectUUID);
        if (visibility == true)
            selectedLayer = layer;
        layer.setVisible(visibility);
    }

    public DrawableElement getElementUnderCoordinates(int x, int y)
    {
        //TODO: this method should be changed to search from top to bottom in the visible layers
        //This is the schema, we only need to generate ordererLayers
//        for(Layer layer: orderedLayers)
//        {
//            if (layer.isVisible())
//            {
//                DrawableElement de = layer.getElementUnderCoordinates(x,y);
//                if (de!= null)
//                {
//                    return de;
//                }
//
//            }
//
//        }
//        return de;
        return selectedLayer.getElementUnderCoordinates(x, y);

    }

    public List<LayerPojo> getLayers() {
        ArrayList<LayerPojo> layersPojos = new ArrayList<>();
        for (Layer layer : layers.values()) {
            layersPojos.add(layer.getLayer());

        }
        return layersPojos;
    }

    //endregion
}
