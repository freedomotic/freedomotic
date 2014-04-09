package com.freedomotic.clients.client.utils;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.user.client.Window;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Created by gpt on 31/03/14.
 */
public class ExtendedCanvas  implements MouseWheelHandler , MouseDownHandler, MouseMoveHandler, MouseUpHandler{

    Canvas canvas;
    Canvas backbuffer;
    Context2d ctx;
    Context2d backBufferContext;

    LinkedHashMap<String, Layer> layers = new LinkedHashMap<>();
    //we only accept one selected layer at a time.
    //TODO: Change to a structure that traverse the visible layers from top to bottom
    Layer selectedLayer = null;

    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map


    private double mScaleFactor = 1;
    private double mPosX = 0;
    private double mPosY = 0;

    boolean mouseDown = false;
    double mouseDownXPos = 0;
    double mouseDownYPos = 0;

    double zoom = 1;
    double offsetX = 0;
    double offsetY = 0;

    //private DockLayoutPanel parent;

    public ExtendedCanvas() {
        canvas = Canvas.createIfSupported();
        backbuffer = Canvas.createIfSupported();
        ctx = canvas.getContext2d();
        backBufferContext = backbuffer.getContext2d();

        canvas.addMouseWheelHandler(this);
        canvas.addMouseMoveHandler(this);
        canvas.addMouseDownHandler(this);
        canvas.addMouseUpHandler(this);

    }

    public int getCanvasWitdh()
    {
        return canvas.getParent().getOffsetWidth();// - BORDER_X - 200;
    }
    public int getCanvasHeight()
    {
        return canvas.getParent().getOffsetHeight();// - BORDER_Y - 200;
    }


    void initCanvas()
    {
        layers.clear();
    }

    void setSize()
    {
        int width = getCanvasWitdh();
        int height = getCanvasHeight();

        canvas.setWidth(width + "px");
        canvas.setHeight(height + "px");
        canvas.setCoordinateSpaceWidth(width);
        canvas.setCoordinateSpaceHeight(height);
        backbuffer.setCoordinateSpaceWidth(width);
        backbuffer.setCoordinateSpaceHeight(height);
        for(Layer layer: layers.values())
        {
            layer.setSize();
        }

    }

    void addDrawingElement(DrawableElement de, Layer layer)
    {
        //TODO: search for the layer and add the element
        layer.addObjectToLayer(de);
        de.setParentCanvas(this);
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

        canvas.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                if (selectedLayer != null) {
                    final DrawableElement de = selectedLayer.getElementUnderCoordinates(event.getX(), event.getY());
                    if (de != null) {
                        if (de.OnDoubleClick(canvas))
                            return;
                    }
                    fitToScreen(getCanvasWitdh(),getCanvasHeight(), getCanvasWitdh()/2, getCanvasHeight() /2);
                }
            }
        });

    }
     //http://stackoverflow.com/questions/16284284/how-to-smoothly-zoom-a-canvas
    public void onMouseWheel(MouseWheelEvent event) {
        int move = event.getDeltaY();

        double xPos = (event.getRelativeX(canvas.getElement()));
        double yPos = (event.getRelativeY(canvas.getElement()));


        double scale = 1;
        if (move < 0) {
            zoom = 1.1;
        } else {
            zoom = 1 / 1.1;
        }

        double newX = (xPos - mPosX)/ mScaleFactor;
        double newY = (yPos - mPosY)/ mScaleFactor;

        double xPosition = (-newX * zoom) + newX;
        double yPosition = (-newY * zoom) + newY;

        centerAndScale(mPosX + xPosition* mScaleFactor * zoom, mPosY + yPosition * mScaleFactor * zoom, mScaleFactor*zoom, true);

    }

    public void onMouseDown(MouseDownEvent event) {
        mouseDownXPos = event.getRelativeX(canvas.getElement());
        mouseDownYPos = event.getRelativeY(canvas.getElement());
        this.mouseDown = true;

    }

    public void onMouseMove(MouseMoveEvent event) {
        if (mouseDown) {
            double xPos = event.getRelativeX(canvas.getElement());
            double yPos = event.getRelativeY(canvas.getElement());
            double deltaX =  mouseDownXPos -xPos;
            double deltaY =  mouseDownYPos -yPos;;
            mPosX -=deltaX;
            mPosY -=deltaY;
            paint(false);

            mouseDownXPos = xPos;
            mouseDownYPos = yPos;
        }
        else
        {
            if (selectedLayer != null) {
                final DrawableElement de = getElementUnderCoordinates(event.getX(), event.getY());
                if ((de == null && elementUnderMouse != null) || (de != null && elementUnderMouse != null && de != elementUnderMouse))
                    elementUnderMouse.OnMouseLeft(canvas);
                if (de != null) {
                    de.OnMouseOver(canvas);
                    elementUnderMouse = de;
                }
            }


        }
    }

    public void onMouseUp(MouseUpEvent event) {
        this.mouseDown = false;
    }


    boolean invalid = true;
    void draw()
    {
        draw(true);

    }

    void draw(boolean redraw) {
        if (invalid) {
            paint(redraw);

        }
    }
    void paint(boolean redraw)
    {
        backBufferContext.clearRect(0, 0, getCanvasWitdh(), getCanvasHeight());

        for (Layer layer : layers.values()) {
            layer.draw(redraw);
        }
        ctx.clearRect(0, 0, getCanvasWitdh(), getCanvasHeight());
        ctx.drawImage(backBufferContext.getCanvas(), 0, 0);

    }

    public void Invalidate()
    {
        invalid = true;

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
        return backBufferContext;
    }

    public void centerToRectangle(Rectangle2D elementBounds)
    {
        double xScale = getCanvasWitdh() / elementBounds.getWidth();
        double yScale = getCanvasHeight() / elementBounds.getHeight();
        double scale = 1;
        if (xScale < yScale) {
            scale = xScale;
        }
        else
            scale = yScale;
        double dX = (elementBounds.getMaxX() - elementBounds.getMinX())/2 *scale - getCanvasWitdh() / 2;//elementBounds.getMinX() *scale;
        double dY = (elementBounds.getMaxY() - elementBounds.getMinY())/2 * scale - getCanvasHeight()/2;//elementBounds.getMinY()*scale;
        centerAndScale(-dX - elementBounds.getMinX() *scale,- dY -elementBounds.getMinY() *scale, scale, true);
    }

    //Adapt the "original coordinates" from freedomotic to the canvas size
    public void fitToScreen(double width, double height, double posX, double posY) {
        //TODO: check for a maximum scale
        double xSize = getCanvasWitdh();
        double ySize = getCanvasHeight();

        double xScale = xSize / width;
        double yScale = ySize / height;
        double centerCanvasX = (getCanvasWitdh() );
        double centerCanvasY = (getCanvasHeight());

        double scale;
        if (xScale < yScale) {
            scale = xScale;
        }
        else
            scale = yScale;

        double centerX =  centerCanvasX - posX;
        double centerY = centerCanvasY - posY;
        centerAndScale(-posX / scale, -posY /scale, scale, true);

    }
    public void centerAndScale(double posX, double posY, double scale, boolean animation)
    {
        MoveWithAnimation moveAnimation = new MoveWithAnimation(mPosX, mPosY, posX, posY, mScaleFactor, scale);
        moveAnimation.run(500);

    }


    public double getScaleFactor() {
        return mScaleFactor;
    }
    public double getPosX() {
        return mPosX;
    }

    public double getPosY() {
        return mPosY;
    }


    //region Layers Management

    public Layer addLayer(String objectUUID, String name)
    {
        Layer newLayer = new Layer(this, objectUUID);
        newLayer.setName(name);
        layers.put(objectUUID, newLayer);
        //TODO: rename setSize for a more appropiate name
        newLayer.setSize();
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
        /*ListIterator<Layer> iter = (ListIterator<Layer>) layers.values().iterator();
        while(iter.hasPrevious())
        {
            Layer layer = iter.previous();
            if (layer.isVisible()) {
                DrawableElement de = layer.getElementUnderCoordinates(x, y);
                if (de != null) ;
                {
                    return de;

                }
            }
        }
        return null;*/
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

    //region Animations
    public class MoveWithAnimation  extends Animation {
        // initial position
        private double startX;
        private double startY;
        private double endX;
        private double endY;
        private double startScale;
        private double endScale;
        private boolean animateX;
        private boolean animateY;
        private boolean animateScale;

        public MoveWithAnimation(double startX, double startY, double endX, double endY, double startScale, double endScale) {
            this.startX = startX;
            this.startY = startY;
            this.endX = endX;
            this.endY = endY;
            this.startScale = startScale;
            this.endScale = endScale;
            animateX = (startX != endX);
            animateY = (startY != endY);
            animateScale = (startScale != endScale);



        }
        @Override
        protected void onUpdate(double progress) {
            if (animateX) mPosX = extractProportionalValue(progress, startX, endX);
            if (animateY) mPosY = extractProportionalValue(progress, startY, endY);
            if (animateScale) mScaleFactor = extractProportionalValue(progress, startScale, endScale);
            invalid = true;
        }

        private double extractProportionalValue(double progress, double start, double end) {
            double out = start - (start - end) * progress;
            return out;
        }

    }

    //endregion



}

