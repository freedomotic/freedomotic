package it.freedomotic.gwtclient.client.utils;

import java.util.HashMap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.dom.client.ImageElement;
import com.google.gwt.event.dom.client.ErrorEvent;
import com.google.gwt.event.dom.client.ErrorHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;

public class ImageUtils {


	public static HashMap<String,Image> CachedImages = new HashMap<String,Image>();  
	public static HashMap<String,String> queuedImages = new HashMap<String,String>();
	
	//TODO: move to a get/set.
	//used to know if the web server must be refreshed
	public static boolean newData = true; 
	
	
	// This is important to use a handler!
	public static void loadImage(final String name) {
	    //doGet(name);
		final Image img = new Image(name);
		img.setVisible(false);	
		RootPanel.get().add(img);
		img.addLoadHandler(new LoadHandler() {
	      public void onLoad(LoadEvent event) {	    	
	    	  //queuedImages.remove(name);	    	
	    	  CachedImages.put(name, img);
	    	  newData = true;
	      }	      
	      
	    });
		img.addErrorHandler(new ErrorHandler() {
			
			@Override
			public void onError(ErrorEvent event) {
				// TODO Auto-generated method stub
//				Window.alert("The image: "+ name+ " could not be loaded. Trying again");
				loadImage(name);
			}
		});
	}
				
	public static void queueImage(String name)
	{
	
		if (!queuedImages.containsKey(name))
		{
			queuedImages.put(name, name);
			loadImage(name);
		}
		
	}
	
	
	public  static ImageData scaleImage(Image image, double scaleToRatio) {
		return scaleImage(image, scaleToRatio,scaleToRatio);		
	}
	public  static ImageData scaleImage(Image image, double scaleToRatioh,double scaleToRatiow) {
	    
	    Canvas canvasTmp = Canvas.createIfSupported();
	    Context2d context = canvasTmp.getContext2d();

	    double ch = (image.getHeight() * scaleToRatioh);// + 100;
	    double cw = (image.getWidth() * scaleToRatiow); //+ 100;

	    canvasTmp.setCoordinateSpaceHeight((int) ch);
	    canvasTmp.setCoordinateSpaceWidth((int) cw);
	    
	    ImageElement imageElement = ImageElement.as(image.getElement());
	   
	    // s = source
	    // d = destination 
	    double sx = 0;
	    double sy = 0;
	    double sw = imageElement.getWidth();
	    double sh = imageElement.getHeight();
	    
	    double dx = 0;
	    double dy = 0;
	    double dw = imageElement.getWidth();
	    double dh = imageElement.getHeight();
	    
	    // tell it to scale image
	    context.scale(scaleToRatioh, scaleToRatiow);
	    	    
	    // draw image to canvas
	    context.drawImage(imageElement, sx, sy, sw, sh, dx, dy, dw, dh);
	    
	    // get image data
	    double w = dw * scaleToRatioh;
	    double h = dh * scaleToRatiow;
	    ImageData imageData = context.getImageData(0, 0, w, h);

	    return imageData;
	}
	public  static ImageData fillImage(Image image, double cw,double ch) {
		 	Canvas canvasTmp = Canvas.createIfSupported();
		    Context2d context = canvasTmp.getContext2d();

		    //double ch = (image.getHeight() * scaleToRatioh) + 100;
		    //double cw = (image.getWidth() * scaleToRatiow) + 100;

		    canvasTmp.setCoordinateSpaceHeight((int) ch);
		    canvasTmp.setCoordinateSpaceWidth((int) cw);
		    
		    ImageElement imageElement = ImageElement.as(image.getElement());
		   
		    // s = source
		    // d = destination 
		    double sx = 0;
		    double sy = 0;
		    double sw = imageElement.getWidth();
		    double sh = imageElement.getHeight();
		    
		    double dx = 0;
		    double dy = 0;
		    double dw = imageElement.getWidth();
		    double dh = imageElement.getHeight();
		    
		    // tell it to scale image
		    //context.scale(scaleToRatioh, scaleToRatiow);
		    
		    // draw image to canvas
		    context.drawImage(imageElement, sx, sy, sw, sh, dx, dy, cw, ch);
		    
		    // get image data
		    //double w = dw * scaleToRatioh;
		    //double h = dh * scaleToRatiow;
		    ImageData imageData = context.getImageData(0, 0, cw, ch);

		    return imageData;
		
	}
}
