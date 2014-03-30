package com.freedomotic.clients.client.widgets;


import com.freedomotic.clients.client.Freedomotic;
import com.freedomotic.clients.client.api.EnvironmentsController;
import com.freedomotic.clients.client.utils.*;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;

public class FloorPlanWidget {

    Canvas canvas;
    Context2d ctx;
    Canvas ghostCanvas;
    Context2d gctx;
    Path2D envPath = new Path2D.Double(); // the enviroment path that is going
    // to be draw.
//	WebGraphics mPaint;
//	WebGraphics environmentPaint;
//	WebGraphics environmentPaint2;
//	WebGraphics environmentShadowPaint;
    //GPT private static EnvironmentResourceProxy environmentResource;		
    public static Environment environment = null;
    ArrayList<DrawableRoom> drawingRooms = new ArrayList<DrawableRoom>();
    ArrayList<DrawableObject> drawingObjects = new ArrayList<DrawableObject>();
    ArrayList<DrawableObject> objectsShowingBehaviors = new ArrayList<DrawableObject>();
    // Map of objects that are drawed
    HashMap<Integer, DrawableElement> objectsIndex = new HashMap<Integer, DrawableElement>();
    //TODO: add get /set
    boolean dataInitialized = false;
    //timer refresh rate, in milliseconds
    static final int refreshRate = 25;
    // canvas size, in px
    //static final int height = 1500;
    //static final int width = 1324;
    private static final double MARGIN = 50;
    private static int BORDER_X = 10; //the empty space around the map
    private static int BORDER_Y = 10; //the empty space around the map
    private static int CANVAS_WIDTH = 1300 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 900 + (BORDER_X * 2);
    private double ENVIRONMENT_WIDTH = CANVAS_WIDTH;
    private double ENVIRONMENT_HEIGHT = CANVAS_HEIGHT;
    private double mScaleFactor = 1;
    boolean transformed = false;
    private double mPosX;
    private double mPosY;
    private DockLayoutPanel parent;

    public FloorPlanWidget(DockLayoutPanel parent, Environment env) {
        this.parent = parent;
        environment = env;
        initCanvas();

        // setup timer
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (dataInitialized) {
                    draw();
                } else {
                    initializeData();
                    fitToScreen();
                }
            }
        };
        timer.scheduleRepeating(refreshRate);

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(ResizeEvent event) {
                // TODO Auto-generated method stub
                GWT.log("resized");
                GWT.log("parentHeight: " + canvas.getParent().getOffsetHeight());
                GWT.log("parentWidth: " + canvas.getParent().getOffsetWidth());
            }
        });


    }

    void initCanvas() {
        canvas = Canvas.createIfSupported();
        canvas.setWidth(CANVAS_WIDTH + "px");
        canvas.setHeight(CANVAS_HEIGHT + "px");
        canvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        canvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        ctx = canvas.getContext2d();


        canvas.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                final DrawableObject dobj = getObjectUnderCoordinates(event.getX(), event.getY());
                if (dobj != null) {
                    final EnvObjectProperties eop = new EnvObjectProperties(dobj.getEnvObject());
                    eop.setPopupPositionAndShow(new EnvObjectProperties.PositionCallback() {
                        @Override
                        public void setPosition(int offsetWidth, int offsetHeight) {
                            int left = event.getX() + canvas.getAbsoluteLeft() + dobj.getCurrentWidth() / 4;
                            int top = event.getY() + canvas.getAbsoluteTop() - dobj.getCurrentHeight() / 2;
                            eop.setPopupPosition(left, top);

                        }
                    });
                }

            }
        });

        canvas.addMouseMoveHandler(new MouseMoveHandler() {
            //TODO: maybe there is a better way making DrawableObjects implement MouseListener and transforming it on widgets
            @Override
            public void onMouseMove(final MouseMoveEvent event) {
                GWT.log("On mouse over");
                final DrawableObject dobj = getObjectUnderCoordinates(event.getX(), event.getY());
                if (dobj != null) {
                    if (!dobj.isShowingBehavioursPanel()) //to avoid blinking 
                    {
                        int left = canvas.getAbsoluteLeft();
                        int top = canvas.getAbsoluteTop();
                        dobj.showBehavioursPanel(left, top, mScaleFactor);
                        objectsShowingBehaviors.add(dobj);

                    }
                } else {
                    for (DrawableObject dobj2 : objectsShowingBehaviors) {
                        dobj2.hideBehavioursPanel();
                    }
                    objectsShowingBehaviors.clear();

                }
            }
        });

        ghostCanvas = Canvas.createIfSupported();
        ghostCanvas.setWidth(CANVAS_WIDTH + "px");
        ghostCanvas.setHeight(CANVAS_HEIGHT + "px");
        ghostCanvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        ghostCanvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        gctx = ghostCanvas.getContext2d();
    }

    void initializeData() {
        if (EnvironmentsController.getInstance().HasData()) {
            if (environment == null) {
                environment = EnvironmentsController.getInstance().getEnvironments().get(0);
            }

            FreedomPolygon poly = environment.getShape();
            envPath = DrawingUtils.freedomPolygonToPath(poly);
            ENVIRONMENT_WIDTH = environment.getWidth();
            ENVIRONMENT_HEIGHT = environment.getHeight();

            drawingRooms.clear();
            drawingObjects.clear();
            objectsIndex.clear();
            ImageUtils.queueImage(Freedomotic.RESOURCES_URL + environment.getBackgroundImage());
            // create all drawingrooms
            for (Zone r : environment.getZones()) {
                if (r.isRoom()) {
                    DrawableRoom dr = new DrawableRoom(r);
                    drawingRooms.add(dr);
                    //GWT.log("index: "+((Integer)dr.getIndexColor()).toString());
                    objectsIndex.put(dr.getIndexColor(), dr);
                    // TODO: Take care of the objects not in room
                    for (EnvObject obj : r.getObjects()) {
                        GWT.log("object in a room " + obj.getName());
                        DrawableObject dobj = new DrawableObject(obj);
                        drawingObjects.add(dobj);
                        objectsIndex.put(dobj.getIndexColor(), dobj);

                    }
                }
            }
            //this.parent.addNorth(new EnvListBox(this), 4);
            dataInitialized = true;
        }


    }

    void adjustAfterCanvasResize(int newParentWidth, int newParentHeight)
    {
        CANVAS_WIDTH = newParentWidth;
        CANVAS_HEIGHT = newParentHeight;
        canvas.setWidth(CANVAS_WIDTH + "px");
        canvas.setHeight(CANVAS_HEIGHT + "px");
        canvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        canvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);

        ghostCanvas.setWidth(CANVAS_WIDTH + "px");
        ghostCanvas.setHeight(CANVAS_HEIGHT + "px");
        ghostCanvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        ghostCanvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);

        fitToScreen();


    }

    void draw() {

        int parentWidth = canvas.getParent().getOffsetWidth();
        int parentHeight = canvas.getParent().getOffsetHeight();
        if (CANVAS_WIDTH != parentWidth || CANVAS_HEIGHT != parentHeight) //the size of the canvas has changed, adjust the map
        {
            adjustAfterCanvasResize(parentWidth, parentHeight);
        }


        ctx.save();
        gctx.save();
        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        ctx.scale(mScaleFactor, mScaleFactor);
        ctx.translate(BORDER_X, BORDER_Y);
        gctx.scale(mScaleFactor, mScaleFactor);
        gctx.translate(BORDER_X, BORDER_Y);

        prepareBackground();
        //TODO: If we render the environment, the border moves the shape, so we must move the background also
        //renderEnvironment(ctx);
        renderRooms();
        renderObjects();
        gctx.restore();
        ctx.restore();
        //ImageUtils.newData = false;		

    }

    public void prepareBackground() {
        if (ImageUtils.CachedImages.containsKey(Freedomotic.RESOURCES_URL + environment.getBackgroundImage())) {
            Image im = ImageUtils.CachedImages.get(Freedomotic.RESOURCES_URL + environment.getBackgroundImage());
            ImageElement ie = ImageElement.as(im.getElement());
            //ghostBitmap =Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(),Config.ARGB_8888);
            //im.setVisible(true);								
            ctx.drawImage(ie, 0, 0, ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT);
        }


    }

    public DrawableObject getObjectUnderCoordinates(int x, int y) {
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
                    if (de instanceof DrawableObject) {
                        return (DrawableObject) de;
                    }
                }
            }
        }
        return null;
    }

    public void renderEnvironment(Context2d ctx) {

        WebGraphics g = new WebGraphics(ctx);
        // Draw it on the Canvas
        ctx.setLineWidth(33);
        g.setColor(Color.WHITE);
        g.draw(envPath);
        ctx.setLineWidth(30);
        g.setColor(Color.LIGHT_GRAY);
        g.draw(envPath);
        ctx.setLineWidth(7);
        g.setColor(Color.DARK_GRAY);
        g.draw(envPath);
    }

    public void renderRooms() {
        for (DrawableRoom dr : drawingRooms) {
            dr.draw(ctx);
            dr.drawGhost(gctx);
        }
    }

    public void renderObjects() {
        for (DrawableObject dobj : drawingObjects) {
            dobj.draw(ctx);
            dobj.drawGhost(gctx);
        }

    }

    public Canvas getCanvas() {
        return canvas;
    }

    //Adapt the "original coordinates" from freedomotic to the canvas size		  
    public void fitToScreen() {

        double xSize = CANVAS_WIDTH - BORDER_X;
        double ySize = CANVAS_HEIGHT - BORDER_Y - 200;

        double xPathSize = ENVIRONMENT_WIDTH;
        double yPathSize = ENVIRONMENT_HEIGHT;

        double xScale = xSize / xPathSize;
        double yScale = ySize / yPathSize;
        if (xScale < yScale) {
            mScaleFactor = xScale;
        } else {
            mScaleFactor = yScale;
        }
        mPosX = 0;//MARGIN;
        mPosY = 0;//MARGIN;

    }

    public void setEnvironment(String envUUID) {
        for (Environment env : EnvironmentsController.getInstance().getEnvironments()) {
            if (env.getUUID().equals(envUUID)) {
                environment = env;
                this.dataInitialized = false;
                break;
            }
        }
    }
}
