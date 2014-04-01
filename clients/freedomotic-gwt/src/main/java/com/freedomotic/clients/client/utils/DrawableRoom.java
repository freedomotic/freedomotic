package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.Freedomotic;
import com.freedomotic.model.environment.Zone;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;

public class DrawableRoom extends DrawableElement {

    private Zone roomObject;

    String textureName;
    private Rectangle2D roomBounds;


    //TODO: create global CONSTANTS for all restapi paths
    public static final String TEXTURE_PATH = Freedomotic.RESOURCES_URL;
    CanvasPattern cp;
    //If true the room is renderer and filled with the pattern
    private boolean fillRoom = false;

    public DrawableRoom(Zone roomObject) {
        super();
        this.roomObject = roomObject;

        //Filling image the load of the image is async. So we must first load it.
        textureName = TEXTURE_PATH + roomObject.getTexture();
        ImageUtils.queueImage(textureName);

        setDrawingPath(DrawingUtils.freedomPolygonToPath(roomObject.getShape()));

    }

    public Zone getRoomObject() {
        return roomObject;
    }

    public void setRoomObject(Zone roomObject) {
        this.roomObject = roomObject;
    }

    public void drawFill(Context2d context, WebGraphics g) {
        //we only fill the room when the filling image is ready.
        if (ImageUtils.CachedImages.containsKey(textureName)) {
            FillStrokeStyle fst = context.getFillStyle();
            if (cp == null) {
                Image im = ImageUtils.CachedImages.get(textureName);
                ImageElement ie = ImageElement.as(im.getElement());
                cp = context.createPattern(ie, Repetition.REPEAT);
            }
            context.setFillStyle(cp);
            g.fill(elementBounds);
            context.setFillStyle(fst);
        }
    }

    public void setDrawingPath(Path2D drawingPath) {
        elementBounds = drawingPath;
        roomBounds = this.elementBounds.getBounds2D();
    }

    @Override
    public void updateElement()
    {}

    @Override
    public void paint(Context2d context, Context2d indexContext) {

        WebGraphics g = new WebGraphics(context);

        //draw the fill
        if (isFillRoom()) {
            drawFill(context, g);
        }
        //draw the border
        context.setLineWidth(2); // 7 pixel line width.
        g.setColor(Color.DARK_GRAY);
        g.draw(elementBounds);
        //draw the text
        g.setColor(Color.BLACK);
        context.fillText(roomObject.getName(), roomBounds.getMinX() + 22, roomBounds.getMinY() + 22);

        //move to a method on the canvas
        paintIndex(indexContext);

    }

    @Override
    public String getName() {

        return roomObject.getName();
    }

    boolean isFillRoom() {
        return fillRoom;
    }


}