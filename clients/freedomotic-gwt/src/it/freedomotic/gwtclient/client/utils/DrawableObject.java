package it.freedomotic.gwtclient.client.utils;

import it.freedomotic.gwtclient.client.Freedomotic;
import it.freedomotic.gwtclient.client.widgets.EnvObjectProperties;
import it.freedomotic.model.geometry.FreedomPolygon;
import it.freedomotic.model.object.EnvObject;
import it.freedomotic.model.object.Representation;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;

public class DrawableObject extends DrawableElement {

    private EnvObject envObject;
//	private Bitmap ghostBitmap;
//	private Image ghostImage;
    double rotation = 0;
    double dx = 0;
    double dy = 0;
    private Rectangle ghostPath;
    private Rectangle objectBounds;
//	private Paint ghostPaint= new Paint();
    //current scale/rotate matrix of the object
//	Matrix drawingMatrix = new Matrix();
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
        //eop = new EnvObjectProperties(envObject);
    }

    public EnvObject getEnvObject() {
        return envObject;
    }

    public void setEnvObject(EnvObject envObject) {
        this.envObject = envObject;
        //ghostPaint.setColor(indexColor);
    }

    @Override
    public String getName() {
        return getEnvObject().getName();
    }

    @Override
    public void draw(Context2d context) {
        String file = getEnvObject().getCurrentRepresentation().getIcon();
        Path2D objectPath = DrawingUtils.freedomPolygonToPath((FreedomPolygon) getEnvObject().getCurrentRepresentation().getShape());
        Rectangle2D box = objectPath.getBounds2D();
        objectBounds = objectPath.getBounds();

        rotation = getEnvObject().getCurrentRepresentation().getRotation();
        dx = getEnvObject().getCurrentRepresentation().getOffset().getX();
        dy = getEnvObject().getCurrentRepresentation().getOffset().getY();
        context.translate(dx, dy);
        context.rotate(rotation);
        WebGraphics g = new WebGraphics(context);
        if (ImageUtils.CachedImages.containsKey(OBJECT_PATH + file)) {
            Image im = ImageUtils.CachedImages.get(OBJECT_PATH + file);
            ImageElement ie = ImageElement.as(im.getElement());
            //ghostPath = new Rectangle(ie.getWidth(),ie.getHeight());
            ghostPath = objectBounds;
            context.drawImage(ie, 0, 0, box.getWidth(), box.getHeight());

            //draw box surronding object										
            //draw the border
            context.setLineWidth(1);
            g.setColor(new Color(137, 174, 32));
            g.draw(box);


        } else {
//        	//TODO: Cache path    		
//    		Paint paint = new Paint();
//    		paint.setStyle(Style.FILL);
//    		
//    		ghostPath = new Path();
//    		objectPath.transform(drawingMatrix, ghostPath);
//    		int fillColor=-1;
//    		try
//    		{
//    			fillColor = Color.parseColor(getEnvObject().getCurrentRepresentation().getFillColor());    			
//    			paint.setColor(fillColor);
//    			canvas.drawPath(ghostPath, paint);
//    		}
//    		catch(IllegalArgumentException ex)
//    		{
//    			System.out.println("ParseColor exception in fill");
//    		}
//    		int borderColor=-1;
//    		try
//    		{
//    			borderColor = Color.parseColor(getEnvObject().getCurrentRepresentation().getBorderColor());    			
//    			paint.setColor(borderColor);
//    			paint.setStyle(Style.STROKE);
//    			canvas.drawPath(ghostPath, paint);
//    		}
//    		catch(IllegalArgumentException ex)
//    		{
//    			System.out.println("ParseColor exception in border");
//    		}    		
        }
        context.rotate(-rotation);
        context.translate(-dx, -dy);

    }

    @Override
    public void drawGhost(Context2d context) {
        double rotation = getEnvObject().getCurrentRepresentation().getRotation();
        double dx = getEnvObject().getCurrentRepresentation().getOffset().getX();
        double dy = getEnvObject().getCurrentRepresentation().getOffset().getY();
        context.translate(dx, dy);
        context.rotate(rotation);
        Color c = new Color(getIndexColor());
        WebGraphics g = new WebGraphics(context);
        g.setColor(c);
        g.fill(ghostPath);
        //		if (ghostBitmap!= null)
//		{			
//			ghostBitmap.eraseColor(indexColor);
//			canvas.drawBitmap(ghostBitmap,drawingMatrix, null);
//		}
//		else
//		{			
//			canvas.drawPath(ghostPath, ghostPaint);			
//		}		

        context.rotate(-rotation);
        context.translate(-dx, -dy);


    }

    public int getCurrentWidth() {
        if (objectBounds != null) {
            return objectBounds.width;
        } else {
            return 0;
        }
    }

    public int getCurrentHeight() {
        if (objectBounds != null) {
            return objectBounds.height;
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

    /**
     * Shows the behaviours panel of the object
     *
     * @param left The x offset
     * @param top The y offst
     * @param scale The scale that must be applied to the absolute position on
     * the environment
     */
    public void showBehavioursPanel(final int left, final int top, final double scale) {
        eop = new EnvObjectProperties(envObject);
        //eop.setWidth("500px");
        eop.setPopupPositionAndShow(new EnvObjectProperties.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                int newX = (int) ((dx + objectBounds.getWidth()) * scale);
                int newY = (int) (dy * scale);
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