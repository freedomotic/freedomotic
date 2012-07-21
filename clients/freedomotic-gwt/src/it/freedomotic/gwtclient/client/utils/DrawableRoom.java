package it.freedomotic.gwtclient.client.utils;

import it.freedomotic.model.environment.Zone;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import com.google.gwt.canvas.dom.client.CanvasPattern;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.Context2d.Repetition;
import com.google.gwt.canvas.dom.client.FillStrokeStyle;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.user.client.ui.Image;
import com.levigo.util.gwtawt.client.WebGraphics;

public class DrawableRoom extends DrawableElement{
	
	private Zone roomObject;

//	private Paint ghostPaint;
	Path2D drawingPath;
	String textureName;
	private Rectangle2D  bounds;
	//TODO: create global CONSTANTS for all restapi paths
	public static final String TEXTURE_PATH = "v1/environment/resources/";
	CanvasPattern cp;
	//If true the room is renderer and filled with the pattern
	private boolean fillRoom= false;
	
	public DrawableRoom(Zone roomObject)
	{
		super();
		this.roomObject = roomObject;
						
		//Filling image the load of the image is async. So we must first load it.
		textureName = TEXTURE_PATH+roomObject.getTexture();
		ImageUtils.queueImage(textureName);

//		this.borderPaint = borderPaint;
//		this.textPaint = textPaint;			
		setDrawingPath(DrawingUtils.freedomPolygonToPath(roomObject.getShape()));	
//		ghostPaint = new Paint();
//		ghostPaint.setStyle(Paint.Style.FILL);
//		ghostPaint.setAntiAlias(false);	      		
		
	}

	public Zone getRoomObject() {
		return roomObject;
	}

	public void setRoomObject(Zone roomObject) {
		this.roomObject = roomObject;
	}

	
	public void drawFill(Context2d context, WebGraphics g)
	{		
		//we only fill the room when the filling image is ready.
		if(ImageUtils.CachedImages.containsKey(textureName))		
		{						
			FillStrokeStyle fst = context.getFillStyle();
			if (cp==null)
			{
				Image im =ImageUtils.CachedImages.get(textureName);
				ImageElement ie = ImageElement.as(im.getElement());				
				cp = context.createPattern(ie,Repetition.REPEAT);			
			}									
			context.setFillStyle(cp);			
			g.fill(drawingPath);
			context.setFillStyle(fst); 						
		}
	}

	public void setDrawingPath(Path2D drawingPath) {		
		this.drawingPath = drawingPath;
		bounds = this.drawingPath.getBounds2D();
	}
	@Override
	public void draw(Context2d context)
	{
		WebGraphics g = new WebGraphics(context);

		//draw the fill				
		if (isFillRoom())
			drawFill(context,g);		
		//draw the border
		context.setLineWidth(2); // 7 pixel line width.
		g.setColor(Color.DARK_GRAY);
		g.draw(drawingPath);					
		//draw the text
		g.setColor(Color.BLACK);
		context.fillText(roomObject.getName(), bounds.getMinX()+22, bounds.getMinY()+22);
	}

	
	
	@Override
	public void drawGhost(Context2d context)
	{
		Color c = new Color(getIndexColor());				
		WebGraphics g = new WebGraphics(context);		
		g.setColor(c);		
		g.fill(drawingPath);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return roomObject.getName();
	}

	boolean isFillRoom() {
		return fillRoom;
	}

	void setFillRoom(boolean fillRoom) {
		this.fillRoom = fillRoom;
	}


	
}