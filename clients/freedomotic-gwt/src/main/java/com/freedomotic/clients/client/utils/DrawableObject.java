package com.freedomotic.clients.client.utils;

import com.freedomotic.clients.client.Freedomotic;
import com.freedomotic.clients.client.widgets.EnvObjectProperties;
import com.freedomotic.model.geometry.FreedomPolygon;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.model.object.Representation;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;
import com.google.gwt.canvas.client.Canvas;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Path2D;


public class DrawableObject extends DrawableElement {

    private EnvObject envObject;
//	private Bitmap ghostBitmap;
//	private Image ghostImage;
    double rotation = 0;
    double dx = 0;
    double dy = 0;
    ImageElement ie;
    Rectangle2D box;
    private Rectangle ghostPath;

    private Representation currentRepresentation;
    private int currentRepresentationIndex;

    public static final String OBJECT_PATH = Freedomotic.RESOURCES_URL;
    EnvObjectProperties eop;

    public DrawableObject(EnvObject envObject) {
        super();
        this.setEnvObject(envObject);
        //Cached of all images needed
        for (Representation rp : envObject.getRepresentations()) {
            if (rp.getIcon() != null) {
                ImageUtils.queueImage(OBJECT_PATH + rp.getIcon());
            }
        }
    }

    public EnvObject getEnvObject() {
        return envObject;
    }

    public void setEnvObject(EnvObject envObject) {
        this.envObject = envObject;
        updateElement();
    }

    @Override
    public String getName() {
        return getEnvObject().getName();
    }



    @Override
    public void updateElement()
    {
        currentRepresentation = envObject.getCurrentRepresentation();
        currentRepresentationIndex = envObject.getCurrentRepresentationIndex();
        String file = currentRepresentation.getIcon();
        Path2D objectPath = DrawingUtils.freedomPolygonToPath((FreedomPolygon)currentRepresentation.getShape());
        box = objectPath.getBounds2D();
        elementBounds = objectPath.getBounds();
        rotation = currentRepresentation.getRotation();
        dx = currentRepresentation.getOffset().getX();
        dy = currentRepresentation.getOffset().getY();
        if (ImageUtils.CachedImages.containsKey(OBJECT_PATH + file)) {
            Image im = ImageUtils.CachedImages.get(OBJECT_PATH + file);
            ie = ImageElement.as(im.getElement());
        }


    }
    @Override
    public void beforeDraw(Context2d context, Context2d indexContext)
    {
        if (getEnvObject().getCurrentRepresentationIndex()!= currentRepresentationIndex || ie == null)
            updateElement();

    }
    @Override
    public void paint(Context2d context, Context2d indexContext) {

        context.translate(dx, dy);
        context.rotate(rotation);

        WebGraphics g = new WebGraphics(context);
        if (ie!= null)
            context.drawImage(ie, 0, 0, box.getWidth(), box.getHeight());
        //draw box surronding object
        //draw the border
        context.setLineWidth(1);
        g.setColor(new Color(137, 174, 32));
        g.draw(box);
        context.rotate(-rotation);
        context.translate(-dx, -dy);

        //Draw over the ghost
        indexContext.translate(dx, dy);
        indexContext.rotate(rotation);

        paintIndex(indexContext);

        indexContext.rotate(-rotation);
        indexContext.translate(-dx, -dy);

    }


    public int getCurrentWidth() {
        if ((Rectangle)elementBounds != null) {
            return ((Rectangle)elementBounds).width;
        } else {
            return 0;
        }
    }

    public int getCurrentHeight() {
        if ((Rectangle)elementBounds != null) {
            return ((Rectangle)elementBounds).height;
        } else {
            return 0;
        }
    }

    public boolean isShowingBehavioursPanel() {
        if (eop != null) {
            return eop.isShowing();
        }
        return false;

    }

    @Override
    public void OnMouseOver(Canvas canvas)
    {
        if (!isShowingBehavioursPanel()) //to avoid blinking
        {
            int left = canvas.getAbsoluteLeft();
            int top = canvas.getAbsoluteTop();
            showBehavioursPanel(left, top, parentCanvas.getScaleFactor());

        }

    }

    @Override
    public void OnMouseLeft(Canvas canvas)
    {
        if (isShowingBehavioursPanel())
            hideBehavioursPanel();

    }


    @Override
    public void OnClick(Canvas canvas)
    {
        if (!isShowingBehavioursPanel()) //to avoid blinking
        {
            int left = canvas.getAbsoluteLeft();// + dobj.getCurrentWidth() / 4;
            int top =  canvas.getAbsoluteTop();// - dobj.getCurrentHeight() / 2;
            showBehavioursPanel(left, top, parentCanvas.getScaleFactor());
        }

    }

    /**
     * Shows the behaviours panel of the object
     *
     * @param left The x offset
     * @param top The y offst
     * @param scale The scale that must be applied to the absolute position on
     * the environment
     */
    public void showBehavioursPanel(final int left, final int top, final double scale) {
        if (eop == null)
            eop = new EnvObjectProperties(envObject);
        else
            eop.refreshObject(envObject);
        eop.setPopupPositionAndShow(new EnvObjectProperties.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                int newX = (int) ((dx + ((Rectangle)elementBounds).width +parentCanvas.getPosX()) * scale);
                int newY = (int) ((dy + parentCanvas.getPosY())* scale);
                eop.setPopupPosition(newX + left, newY + top);
            }
        });
    }

    public void hideBehavioursPanel() {
        if (eop != null) {
            eop.hide();
        }
    }
}