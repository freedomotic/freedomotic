package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.Freedomotic;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;

import java.awt.*;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by gpt on 1/04/14.
 */
public class DrawableEnvironment extends DrawableElement {

    Environment environment;
    int ENVIRONMENT_WIDTH;
    int ENVIRONMENT_HEIGHT;
    ImageElement ie;
    Rectangle2D box;
    WebGraphics g;

    public DrawableEnvironment(Environment environment)
    {
        this.environment = environment;
        FreedomPolygon poly = environment.getShape();
        Path2D envPath = DrawingUtils.freedomPolygonToPath(poly);
        ENVIRONMENT_WIDTH = environment.getWidth();
        ENVIRONMENT_HEIGHT = environment.getHeight();

        ImageUtils.queueImage(Freedomotic.RESOURCES_URL + environment.getBackgroundImage());

    }

    @Override
    public String getName() {
        return environment.getName();
    }

    @Override
    public void draw(Context2d context) {

    }

    @Override
    public void drawGhost(Context2d context) {

    }

    @Override
    public void updateElement()
    {
        String file = Freedomotic.RESOURCES_URL + environment.getBackgroundImage();
        Path2D environmentPath = DrawingUtils.freedomPolygonToPath((FreedomPolygon) environment.getShape());
        box = environmentPath.getBounds2D();
        elementBounds = environmentPath.getBounds();
        if (ImageUtils.CachedImages.containsKey(file)) {
            Image im = ImageUtils.CachedImages.get(file);
            ie = ImageElement.as(im.getElement());
        }

    }
    @Override
    public void beforeDraw(Context2d context, Context2d indexContext)
    {
        if (ie == null)
            updateElement();
    }

    @Override
    public void paint(Context2d context, Context2d indexContext) {

        if (ie != null)
            context.drawImage(ie, 0, 0, ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT);

        paintIndex(indexContext);


    }
}
