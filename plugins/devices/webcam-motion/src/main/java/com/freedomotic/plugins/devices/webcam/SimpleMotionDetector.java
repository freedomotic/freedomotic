/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.webcam;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.awt.image.PixelGrabber;

/**
 *
 * @author Enrico Nicoletti
 */
public class SimpleMotionDetector extends MotionDetector {

    int oldSum;
    private int[] previousPixels = null;
    private int[] latestPixels = null;
    private int motionLevel;
    int frameNo = 0;

    public SimpleMotionDetector() {
        super();
    }

    public int enqueueImage(Image latest) {
        int w = latest.getWidth(null);
        int h = latest.getHeight(null);

        //First time we run this function allocate the array
        if (latestPixels == null) {
            latestPixels = new int[w * h];
        }
        if (previousPixels == null) {
            previousPixels = new int[w * h];
        }
        PixelGrabber latestPixelGrabber = new PixelGrabber(latest, 0, 0, w, h, latestPixels, 0, w);

        try {
            latestPixelGrabber.grabPixels();
        } catch (InterruptedException e) {
            System.out.println("interrupted waiting for pixels");
        }

        if ((latestPixelGrabber.getStatus() & ImageObserver.ABORT) != 0) {
            System.out.println("image fetch aborted or errored");
        }

        if (previousPixels != null) {
            motionLevel = 0;
            for (int ct = 0; ct < (w * h); ct++) {
                int latestPixel = latestPixels[ct];
                int latestAlpha = (latestPixel >> 24) & 0xff;
                int latestRed = (latestPixel >> 16) & 0xff;
                int latestGreen = (latestPixel >> 8) & 0xff;
                int latestBlue = (latestPixel) & 0xff;

                int previousPixel = previousPixels[ct];
                int previousAlpha = (previousPixel >> 24) & 0xff;
                int previousRed = (previousPixel >> 16) & 0xff;
                int previousGreen = (previousPixel >> 8) & 0xff;
                int previousBlue = (previousPixel) & 0xff;

                if ((Math.abs(previousRed - latestRed) > noiseThreshold) | (Math.abs(previousGreen - latestGreen) > noiseThreshold) | (Math.abs(previousBlue - latestBlue) > noiseThreshold)) {
                    motionLevel++;
                }
            }
        }

        System.arraycopy(latestPixels, 0, previousPixels, 0, latestPixels.length);

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }

        //Normalise motion level to be in the range 0-999
        int noOfPixels = w * h;
        motionLevel = (motionLevel * 1000) / noOfPixels;

        //return no motion for first few frames to avoid initial motion spike
        if (frameNo < 5) {
            frameNo++;
            return 0;
        }

        return motionLevel;
    }
}
