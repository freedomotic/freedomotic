/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.webcam;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Enrico Nicoletti
 */
public class CamMotionDetector extends Protocol {

    private String ipCamera = "http://194.218.96.93/axis-cgi/mjpg/video.cgi?resolution=320x240";
    private ArrayList<Integer> motion = new ArrayList<Integer>();
    private MjpegWebcamConnector mjpegWebcam;
    private int lastMotionLevel = 0;

    public CamMotionDetector() {
        super("Camera Motion Detector", "/com.freedomotic.wmotion/camera-motion-detector.xml");
        ipCamera = configuration.getStringProperty("camera-url", "http://194.218.96.93/axis-cgi/mjpg/video.cgi?resolution=320x240");
    }

    public void displayVideo() {
        //sends a message to a video player to visualize the stream
        Command playMedia = new Command();
        playMedia.setName("Media Player Request");
        playMedia.setDescription("request to play a media file or web stream");
        playMedia.setReceiver("app.actuators.media.player.in");
        playMedia.setProperty("url", ipCamera);
        playMedia.setProperty("fullscreen", "false");
        playMedia.setProperty("dimension", "from source");
    }

    public void setMotionLevel(int motionLevel) {
        if (motion.size() < 30) {
            motion.add(motionLevel);
        } else {
            motion.remove(0);
            motion.add(motionLevel);
        }
        int averageMotionLevel = applyForgettingFactor(motion);
        Freedomotic.logger.fine("Motion level in " + ipCamera + " is " + averageMotionLevel + "%");
        sendMotionNotification(averageMotionLevel);
    }

    private void sendMotionNotification(int averageMotionLevel) {
        if (averageMotionLevel != lastMotionLevel) {
            this.setDescription("Last detected motion level was " + averageMotionLevel + "%");
            if (averageMotionLevel > configuration.getIntProperty("sensibility-threshold", 30)) {
                //here create and send the freedom event
                GenericEvent event = new GenericEvent(this);
                event.addProperty("motion-level", new Integer(averageMotionLevel).toString());
                event.setDestination("app.event.sensor.video.motion");
                notifyEvent(event);
                lastMotionLevel = averageMotionLevel;
            }
        }
    }

    private int applyForgettingFactor(List<Integer> list) {
        double factorSum = 0.0;
        double motionVal = 0.0;
        int id = 0;
        int exp = 0;
        int size = list.size() - 1;
        int currVal;
        int lastVal = list.get(size);
        for (int i = size; i >= 0; i--) {
            //a factor= 0 means only the last received value is used
            //factor=1 menas all value used
            double factor = Math.pow(configuration.getDoubleProperty("forgetting-factor", 0.8), exp);
            currVal = list.get(i);
            double tmpVal = currVal * factor;
            factorSum += factor;
            motionVal += tmpVal;
            exp++;
        }
        double coordx = motionVal / factorSum;
        return (int) coordx;
    }

    @Override
    public void onRun() {
        MotionDetector algorithm = new SimpleMotionDetector();
        if (configuration.getStringProperty("algorithm", "simple").equalsIgnoreCase("simple")) {
            algorithm = new SimpleMotionDetector();
        }
        //instantiate a camera connection with simple motion detection algorithm
        algorithm.setNoiseThreshold(configuration.getIntProperty("noise-threshold", 20));
        MjpegWebcamConnector webcam = new MjpegWebcamConnector(ipCamera, algorithm, this);
        try {
            webcam.connect();
            setDescription("Connected to " + ipCamera.toString());
            displayVideo();
        } catch (IOException ex) {
            stop();
            setDescription("Unable to connect to " + ipCamera.toString());
        }
    }

    @Override
    public void onStop() {
        if (mjpegWebcam != null) {
            mjpegWebcam.disconnect();
        }
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}