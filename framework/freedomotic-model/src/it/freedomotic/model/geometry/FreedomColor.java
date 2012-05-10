
package it.freedomotic.model.geometry;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class FreedomColor implements Serializable{
    private int red;
    private int green;
    private int blue;
    private int alpha;

    public FreedomColor(){
        
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = alpha;
    }

    public int getBlue() {
        return blue;
    }

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public int getGreen() {
        return green;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int red) {
        this.red = red;
    }
}
