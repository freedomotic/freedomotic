/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.webcam;

import java.awt.Image;

/**
 *
 * @author Enrico Nicoletti
 */
abstract public class MotionDetector {

    //Deve ritornare un valore tra 0 e 999
    abstract public int enqueueImage(Image latest);
    protected int noiseThreshold;

    public MotionDetector() {
        noiseThreshold = 20;
    }

    public void setNoiseThreshold(int threshold) {
        noiseThreshold = threshold;
    }
}
