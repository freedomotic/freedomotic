/**
 *
 * Copyright (c) 2009-2015 Freedomotic team http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This Program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Freedomotic; see the file COPYING. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.freedomotic.plugins.devices.ipcameramotion;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.events.GenericEvent;
import com.freedomotic.events.MessageEvent;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.PluginStartupException;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.CommandRepository;
import com.freedomotic.settings.Info;
import com.github.sarxos.webcam.*;
import com.github.sarxos.webcam.ds.ipcam.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.Border;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpCameraMotion
        extends Protocol {

    private static final Logger LOG = LoggerFactory.getLogger(IpCameraMotion.class.getName());
    private final int PANEL_WIDTH = configuration.getIntProperty("panel-width", 256);
    private final int PANEL_HEIGHT = configuration.getIntProperty("panel-height", 144);
    private final int DETECTOR_INTERVAL = configuration.getIntProperty("detector-interval", 2000);
    private final String FPS = configuration.getStringProperty("fps", "0.5");
    private List<WebcamPanel> panels;
    private List<WebcamMotionDetector> detectors;
    private ProtocolRead event;

    static {
        Webcam.setDriver(new IpCamDriver(new IpCamStorage(Info.PATHS.PATH_DEVICES_FOLDER + "/ipcamera-motion/" + "cameras.xml")));
    }
    JFrame gui = null;

    public IpCameraMotion() {
        super("IpCamera Motion", "/ipcamera-motion/ipcamera-motion-manifest.xml");
        setPollingWait(-1);
    }

    @Override
    protected void onShowGui() {
        bindGuiToPlugin(gui);
    }

    @Override
    protected void onHideGui() {
        setDescription("IpCamera Motion");
    }

    @Override
    protected void onRun() {
        new DetectMotion();
    }

    @Override
    protected void onStart() throws PluginStartupException {
        gui = new JFrame("IpCamera Motion");
        panels = new ArrayList<WebcamPanel>();
        detectors = new ArrayList<WebcamMotionDetector>();
        try {
            loadCameras();
        } catch (MalformedURLException ex) {
            // TODO if there are no cameras available stop plugin
            throw new PluginStartupException("Error loading cameras " + ex.getMessage(), ex);
        }
        LOG.info("IpCamera Motion plugin started");

    }

    @Override
    protected void onStop() {
        for (WebcamMotionDetector detector : detectors) {
            detector.stop();
        }
        for (WebcamPanel panel : panels) {
            panel.stop();
        }
        for (Webcam webcam : Webcam.getWebcams()) {
            webcam.close();
        }
        LOG.info("IpCamera Motion plugin stopped");
    }

    @Override
    protected void onCommand(Command c)
            throws IOException, UnableToExecuteException {
        switch (c.getProperty("command")) {
            case "CAPTURE-IMAGE":
                LOG.debug("Command capture image " + c.getProperty("camera-name"));
                captureImage(c.getProperty("camera-name"));
                break;
            case "CAPTURE-IMAGE-NOTIFY-MAIL":
                LOG.debug("Command capture image and notify mail " + c.getProperty("camera-name"));
                String capturedImagePath = captureImage(c.getProperty("camera-name"));
                // notify a MessageEvent of "mail" type with the capture image absolute path
                // this event will be managed by Mailer plugin to attach the image
                MessageEvent event = new MessageEvent(null, c.getProperty("message"));
                event.setType("mail");
                event.setTo(c.getProperty("to-address"));
                event.setFrom(c.getProperty("from-address"));
                event.setAttachmentPath(capturedImagePath);
                notifyEvent(event);
                break;

        }
        LOG.info("IpCamera Motion plugin receives a command called " + c.getName() + " with parameters "
                + c.getProperties().toString());
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void loadCameras() throws MalformedURLException {

        gui.setLayout(new GridLayout(0, 3, 1, 1));
        //panels = new ArrayList<WebcamPanel>();
        for (Webcam webcam : Webcam.getWebcams()) {
            // TODO set panel dimensions as config parameters
            WebcamPanel panel = new WebcamPanel(webcam, new Dimension(PANEL_WIDTH, PANEL_HEIGHT), false);
            panel.setFitArea(true); // best fit into panel area (do not break image proportions)
            panel.setFPSLimited(true);
            panel.setFPSLimit(Double.valueOf(FPS)); // 0.5 FPS = 1 frame per 2 seconds
            Border title = BorderFactory.createTitledBorder(webcam.getName());
            panel.setBorder(title);
            gui.add(panel);
            panels.add(panel);

            WebcamDevice device = webcam.getDevice();
            URL url = ((IpCamDevice) device).getURL();

            // notify event to create IpCamera thing
            //event = new ProtocolRead(this, "ipcamera-motion", url.toString());
            //event.addProperty("object.class", "IpCamera");
            //event.addProperty("object.name", webcam.getName());
            //event.addProperty("autodiscovery.allow-clones", "false");
            //notifyEvent(event);

            final Webcam refWebcam = webcam;
            final WebcamPanel refPanel = panel;

            // open webcam and start panel in parallel, by doing this in new thread GUI will
            // not be blocked for the time when webcam is being initialized
            // webcam will be open in asynchronouns mode:
            // webcam.open() = synchronouse mode, getImage() is blocking
            // webcam.open(true) = asynchronous mode, getImage() is non-blocking (return immediately, but may return old image)

            Thread t = new Thread() {
                @Override
                public void run() {
                    refWebcam.open(true); // open in asynchronous mode, do nothing if already open
                    refPanel.start(); // start motion detector
                }
            };
            t.setDaemon(true);
            t.start();

        }

        gui.pack();

    }

    private class DetectMotion implements WebcamMotionListener {

        //List<WebcamMotionDetector> detectors  = new ArrayList<WebcamMotionDetector>();
        public DetectMotion() {
            for (Webcam webcam : Webcam.getWebcams()) {

                WebcamMotionDetector detector = new WebcamMotionDetector(webcam);

                // 2000 = 1 check per 2 seconds, 0.5 FPS which is the same value as in panel
                detector.setInterval(DETECTOR_INTERVAL); // move to manifest
                detector.addMotionListener(this);
                detectors.add(detector);

                final Webcam refWebcam = webcam;
                final WebcamMotionDetector refDetector = detector;

                // open webcam and start motion detector in parallel, by doing this in new thread GUI will
                // not be blocked for the time when webcam is being initialized

                Thread t = new Thread() {
                    @Override
                    public void run() {
                        refWebcam.open(true); // asynchronous mode, do nothing if already open
                        refDetector.start(); // start motion detector
                    }
                };
                t.setDaemon(true);
                t.start();
            }
        }

        @Override
        public void motionDetected(WebcamMotionEvent wme) {
            Webcam webcam = ((WebcamMotionDetector) wme.getSource()).getWebcam();
            IpCamDevice device = (IpCamDevice) webcam.getDevice(); // in case whe IP camera driver is used
            //here create and send the event related to motion detection
            GenericEvent event = new GenericEvent(this);
            event.setDestination("app.event.sensor.video.motion");
            event.getPayload().addStatement("camera-name", webcam.getName());
            event.getPayload().addStatement("motion-area", String.valueOf(wme.getArea())); //percentage of complete image pixels area that has been changed between two consecutive images
            event.getPayload().addStatement("center-of-gravity", wme.getCog().toString()); //center-of-gravity (the center of motion area)
            notifyEvent(event);
            LOG.info("IpCamera {} detected motion!", webcam.getName());
        }
    }

    private String captureImage(String cameraName) {
        Webcam webcam = getCameraByName(cameraName);
        File capturedImage = null;
        if (webcam != null) {
            webcam.open(true);
            BufferedImage image = webcam.getImage();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH-mm-ss");
            String dateStr = dateFormat.format(new Date());
            capturedImage = new File(Info.PATHS.PATH_DEVICES_FOLDER + "/ipcamera-motion/data/captured-images/" + webcam.getName() + "_" + dateStr + ".jpg");
            // JPG is circa 10x smaller than the same image in PNG
            try {
                ImageIO.write(image, "JPG", capturedImage);

            } catch (IOException ex) {
                LOG.error("Error during image capture from camera " + cameraName + " for: " + ex.getMessage());
                return null;
            }
        }
        return capturedImage.getAbsolutePath();
    }

    /**
     * This class returns a webcam object from its name
     *
     * @param name
     * @return
     */
    public Webcam getCameraByName(String name) {
        for (Webcam webcam : Webcam.getWebcams()) {
            if (webcam.getName().equalsIgnoreCase(name)) {
                return webcam;
            }
        }
        return null;
    }
}
