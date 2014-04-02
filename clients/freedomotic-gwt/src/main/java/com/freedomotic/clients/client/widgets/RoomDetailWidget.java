package com.freedomotic.clients.client.widgets;


import com.freedomotic.clients.client.api.EnvironmentsController;
import com.freedomotic.clients.client.utils.DrawableObject;
import com.freedomotic.clients.client.utils.DrawableRoom;
import com.freedomotic.clients.client.utils.DrawingUtils;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.EnvObject;
import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Label;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

public class RoomDetailWidget {

    Label lbl = new Label();
    String id;
    //HashMap<Integer, DrawableElement> objectsIndex = new HashMap<Integer, DrawableElement>();
    ArrayList<DrawableObject> drawingObjects = new ArrayList<DrawableObject>();
    Canvas canvas;
    Context2d ctx;
    Canvas ghostCanvas;
    Context2d gctx;
    private DrawableRoom dr;
    boolean dataInitialized = false;
    private static int BORDER_X = 10; // the empty space around the map
    private static int BORDER_Y = 10; // the empty space around the map
    private static int CANVAS_WIDTH = 600 + (BORDER_X * 2);
    private static int CANVAS_HEIGHT = 400 + (BORDER_Y * 2);
    static final int refreshRate = 2500;
    //private DockLayoutPanel parent;
    Double xScale = 1.0;
    Double yScale = 1.0;
    Double xTraslate = 0.0;
    Double yTraslate = 0.0;

    public RoomDetailWidget(String roomID) {
        //this.parent = parent;
        id = roomID;

        canvas = Canvas.createIfSupported();
        canvas.setWidth(CANVAS_WIDTH + "px");
        canvas.setHeight(CANVAS_HEIGHT + "px");
        canvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        canvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        ctx = canvas.getContext2d();

        ghostCanvas = Canvas.createIfSupported();
        ghostCanvas.setWidth(CANVAS_WIDTH + "px");
        ghostCanvas.setHeight(CANVAS_HEIGHT + "px");
        ghostCanvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        ghostCanvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        gctx = ghostCanvas.getContext2d();

        // setup timer
        final Timer timer = new Timer() {
            @Override
            public void run() {
                if (dataInitialized) {
                    draw();
                } else {
                    init();
                }
            }
        };
        timer.scheduleRepeating(refreshRate);
        //draw();
    }

    public void init() {
        Environment env = EnvironmentsController.getInstance().getEnvironments().get(0);
        GWT.log("Zone id: " + id);

        for (Zone z : env.getZones()) {
            if (z.getName().equals(id)) {
                GWT.log("Found!");
                // draw zone
                dr = new DrawableRoom(z);
                //		GWT.log("index: "+((Integer)dr.getIndexColor()).toString());
                //objectsIndex.put(dr.getIndexColor(), dr);

                // TODO: Take care of the objects not in room
                for (EnvObject obj : z.getObjects()) {
                    // GWT.log("object in a room "+obj.getName());
                    DrawableObject dobj = new DrawableObject(obj);
                    drawingObjects.add(dobj);
                    //objectsIndex.put(dobj.getIndexColor(), dobj);
                }
                break;
            }
        }


        dataInitialized = true;
    }

    void draw() {
        fitArea();
        canvas.setWidth(CANVAS_WIDTH + "px");
        canvas.setHeight(CANVAS_HEIGHT + "px");
        canvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        canvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        //canvas.setWidth(canvas.getParent().getOffsetWidth() + "px");
        //canvas.setHeight(canvas.getParent().getOffsetHeight() + "px");
        //canvas.setCoordinateSpaceWidth(canvas.getParent().getOffsetWidth());
        //canvas.setCoordinateSpaceHeight(canvas.getParent().getOffsetHeight());
        ghostCanvas.setWidth(CANVAS_WIDTH + "px");
        ghostCanvas.setHeight(CANVAS_HEIGHT + "px");
        ghostCanvas.setCoordinateSpaceWidth(CANVAS_WIDTH);
        ghostCanvas.setCoordinateSpaceHeight(CANVAS_HEIGHT);
        //ghostCanvas.setWidth(canvas.getParent().getOffsetWidth() + "px");
        //ghostCanvas.setHeight(canvas.getParent().getOffsetHeight() + "px");
        //ghostCanvas.setCoordinateSpaceWidth(canvas.getParent().getOffsetWidth());
        //ghostCanvas.setCoordinateSpaceHeight(canvas.getParent().getOffsetHeight());



        ctx.save();
        gctx.save();
        ctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        gctx.clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        ctx.scale(xScale, yScale);
        gctx.scale(xScale, yScale);
        ctx.translate(BORDER_X - xTraslate, BORDER_Y - yTraslate);
        gctx.translate(BORDER_X - xTraslate, BORDER_Y - yTraslate);

        // TODO: If we render the environment, the border moves the shape, so we
        // must move the background also

        //dr.drawGhost(gctx);
        //dr.draw(ctx);

        renderObjects();

        gctx.restore();
        ctx.restore();
        // ImageUtils.newData = false;

        //hp.add(canvas);


    }

    public void renderObjects() {
        for (DrawableObject dobj : drawingObjects) {
           // dobj.drawGhost(gctx);
           // dobj.draw(ctx);

            //objectsIndex.put(dobj.getIndexColor(), dobj);	        		
        }
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void fitArea() {
        Zone room = dr.getRoomObject();
        FreedomPolygon poly = room.getShape();
        Path2D envPath = new Path2D.Double();
        envPath = DrawingUtils.freedomPolygonToPath(poly);
        Rectangle2D bounds = envPath.getBounds2D();
        xScale = (CANVAS_WIDTH - (BORDER_X * 4)) / bounds.getWidth();
        yScale = (CANVAS_HEIGHT - (BORDER_Y * 4)) / bounds.getHeight();
        if (xScale < yScale) {
            yScale = xScale;
        } else {
            xScale = yScale;
        }
        xTraslate = bounds.getMaxX() - bounds.getWidth();
        yTraslate = bounds.getMaxY() - bounds.getHeight();



    }
}
