package com.freedomotic.clients.client.utils;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;

import java.awt.*;
import java.util.LinkedHashMap;

/**
 * Created by gpt on 3/04/14.
 */
public class Layer  {

    ExtendedCanvas mparentCanvas;
    private String mName;
    private boolean mVisible = false;

    Canvas indexCanvas;
    Context2d indexContext;

    private LayerPojo layer;

    LinkedHashMap<Integer, DrawableElement> objectsInLayer = new LinkedHashMap<Integer, DrawableElement>();
    public Layer(ExtendedCanvas extCanvas, String objectUUID){

        mparentCanvas = extCanvas;
        indexCanvas = Canvas.createIfSupported();
        indexContext = indexCanvas.getContext2d();
        layer = new LayerPojo(objectUUID);
    }

    public int addObjectToLayer(DrawableElement de)
    {
        de.setIndexColor(generateNextValidColor());
        objectsInLayer.put(de.getIndexColor(), de);
        return de.getIndexColor();
    }

    //Handle the objects that are being drawn in the canvas

    //region Index
    private int redValue = 0;
    private int greenValue = 0;
    private int blueValue = 0;
    private int alphaValue = 255;

    public int generateNextValidColor() {
        int step = 1;
        redValue += step;
        if (redValue >= 256) {
            greenValue += step;
            redValue = 0;
            if (greenValue >= 256) {
                blueValue += step;
                greenValue = 0;
                if (blueValue >= 256) {
                    System.out.println("We have reached the limit of the number of objects!! 255*255*255!!!");
                }
            }

        }
        Color c = new Color(redValue, greenValue, blueValue, alphaValue);
        return (c.getRGB());

    }

    public DrawableElement getElementUnderCoordinates(int x, int y) {
        if (indexContext != null) {
            int posX = 0;
            int posY = 0;
            //retrieve the color under the click on the ghost canvas
            ImageData id = indexContext.getImageData(x, y, 1, 1);
            Color c = new Color(id.getRedAt(posX, posY), id.getGreenAt(posX, posY), id.getBlueAt(posX, posY), id.getAlphaAt(posX, posY));
            //GWT.log(Integer.toString(c.getRGB()));
            if (objectsInLayer.containsKey(c.getRGB()));
            {
                if (objectsInLayer.get(c.getRGB()) != null) {
                    DrawableElement de = objectsInLayer.get(c.getRGB());
                    return de;
                }
            }
        }
        return null;
    }
    //endregion


    public void draw()
    {
        indexContext.clearRect(0, 0, mparentCanvas.getCanvasWitdh(),  mparentCanvas.getCanvasHeight());

        if (mVisible) {
            for (DrawableElement de : objectsInLayer.values()) {
                de.draw(mparentCanvas.getContext(), indexContext);

            }
        }
    }

    public void updateElements()
    {
        for (DrawableElement de : objectsInLayer.values()) {
            de.updateElement();
        }
    }

    public void setSize() {
        int width = mparentCanvas.getCanvasWitdh();
        int height = mparentCanvas.getCanvasHeight();
        indexCanvas.setWidth(width + "px");
        indexCanvas.setHeight(height + "px");
        indexCanvas.setCoordinateSpaceWidth(width);
        indexCanvas.setCoordinateSpaceHeight(height);
    }

    public String getName() {
        return getLayer().getName();
    }

    public void setName(String name) {
        getLayer().setName(name);
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setVisible(boolean visible) {
        this.mVisible = visible;
    }

    public LayerPojo getLayer() {
        return layer;
    }
}
