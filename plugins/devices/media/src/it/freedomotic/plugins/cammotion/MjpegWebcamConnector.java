/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.plugins.cammotion;

import it.freedomotic.app.Freedomotic;
import it.freedomotic.plugins.CamMotionDetector;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;

/**
 *
 * @author Enrico Nicoletti
 */
public class MjpegWebcamConnector extends Thread {

    DataInputStream stream;
    private Image image = null;
    private MyAuthenticator authenticator;
    private String username = "user";
    private String password = "pwd";
    private String url;
    private Dimension imageSize = null;
    private boolean isConnected = false;
    private HttpURLConnection httpConnection = null;
    MotionDetector motionDetector;
    CamMotionDetector plugin;

    public MjpegWebcamConnector(String url, MotionDetector detectorAlgorithm, CamMotionDetector plugin) {
        this.url = url;
        this.plugin = plugin;
        this.motionDetector = detectorAlgorithm;
    }

    public synchronized boolean connect() throws IOException {
        try {
            URL httpUrl = new URL(url);
            httpConnection = (HttpURLConnection) httpUrl.openConnection();
            // Verifica della connesione
            System.out.println("HTTP-HEADER:" + httpConnection.getHeaderFields().toString()); // multipart/x-mixed-replace; boundary=--myboundary
            InputStream is = httpConnection.getInputStream();
            isConnected = true;
            BufferedInputStream bis = new BufferedInputStream(is);
            stream = new DataInputStream(bis);
            this.start();
        } catch (IOException iOException) {
            return false;
        }
        return true;
    }

    public synchronized void disconnect() {
        try {
            if (isConnected) {
                stream.close();
                isConnected = false;
            }
        } catch (Exception e) {
        }
    }

//    private void parseMJPGStream() {
//        // preprocess the mjpg stream to remove the mjpg encapsulation
//        readLines(4, stream); // reads and discards the first 4 lines
//        //read untill --myboundary (and skip the line after it)
//        boolean done = false;
//        ByteArrayOutputStream line;
//        while (done) {
//            line.write(readLine(stream);
//            if (line.toString().equals("--myboundary")) {
//                done = true;
//                
//            }
//        }
//        parseJPGImage();
//        readLines(1, stream); // reads and discards the last line
//    }
//
    private void parseJPGImage(String input) { // read the embedded jpeg image
        try {
            byte b[] = input.toString().getBytes();
            ByteArrayInputStream tmp = new ByteArrayInputStream(b);
            //JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(stream);
            image = ImageIO.read(tmp);
            //image = decoder.decodeAsBufferedImage();
            int motionLevel = motionDetector.enqueueImage(image);
            plugin.setMotionLevel(motionLevel);
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }
//
//    private void readLines(int n, DataInputStream dis) {
//        for (int i = 0; i < n; i++) {
//            readLine(dis);
//        }
//    }
//
//    private ByteArrayOutputStream readLine(DataInputStream dis) {
//        try {
//            boolean end = false;
//            final String LINEEND = "\n"; // assumes that the end of the line is marked with this
//            byte[] lineEndBytes = LINEEND.getBytes();
//            byte[] byteBuf = new byte[lineEndBytes.length];
//            ByteArrayOutputStream line = new ByteArrayOutputStream();
//            while (!end) {
//                dis.read(byteBuf, 0, lineEndBytes.length);
//                line.write(byteBuf);
//                String t = new String(byteBuf);
//                System.out.print(t); //uncomment if you want to see what the lines actually look like
//                if (t.equals(LINEEND)) {
//                    end = true;
//                }
//            }
//            return line;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return new ByteArrayOutputStream();
//        }
//    }

    @Override
    public void run() {
        if (!isConnected) {
            try {
                connect();
            } catch (IOException iOException) {
                Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(iOException));
            }
        }
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(new URL(url).openStream()));
            String inputLine;
            StringBuilder buffer = new StringBuilder();
            int lineCount = 0;
            boolean lineCountStart = false;
            boolean saveImage = false;
            while ((inputLine = in.readLine()) != null) {
                // Should be checking just for "--" probably
                if (inputLine.lastIndexOf("--myboundary") > -1) {
                    if (buffer.length() > 0) {
                        parseJPGImage(buffer.toString());
                    }
                    // Got an image boundary, stop last image
                    // Start counting lines to get past:
                    // Content-Type: image/jpeg
                    // Content-Length: 22517
                    saveImage = false;
                    lineCountStart = true;
                    System.out.println("Got a new boundary");
                    System.out.println(inputLine);
                } else if (lineCountStart) {
                    lineCount++;
                    if (lineCount >= 2) {
                        lineCount = 0;
                        lineCountStart = false;
                        saveImage = true;
                        System.out.println("Starting a new image");
                        buffer = new StringBuilder();
                    }
                } else if (saveImage) {
                    //System.out.println(inputLine.toString());
                    buffer.append(inputLine);
                } else {
                    System.out.println("What's this:");
                    System.out.println(inputLine);
                }
            }
            in.close();
        } catch (Exception e) {
            plugin.stop();
            Freedomotic.logger.severe(Freedomotic.getStackTraceInfo(e));
        }
    }

    public Image getImage() {
        return image;
    }

    private class MyAuthenticator extends Authenticator {

        private String username;
        private String password;

        public MyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.username, this.password.toCharArray());
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
//    http://www.damonkohler.com/2010/10/mjpeg-streaming-protocol.html
//    public void streamAMjpeg(Socket socket, JpegProvider jpegProvider) throws Exception {
//  byte[] data = jpegProvider.getJpeg();
//  OutputStream outputStream = socket.getOutputStream();
//  outputStream.write((
//      "HTTP/1.0 200 OK\r\n" +
//      "Server: YourServerName\r\n" +
//      "Connection: close\r\n" +
//      "Max-Age: 0\r\n" +
//      "Expires: 0\r\n" +
//      "Cache-Control: no-cache, private\r\n" +
//      "Pragma: no-cache\r\n" +
//      "Content-Type: multipart/x-mixed-replace; " +
//      "boundary=--BoundaryString\r\n\r\n").getBytes());
//  while (true) {
//    data = jpegProvider.getJpeg();
//    outputStream.write((
//        "--BoundaryString\r\n" +
//        "Content-type: image/jpg\r\n". +
//        "Content-Length: " +
//        data.length +
//        "\r\n\r\n").getBytes());
//    outputStream.write(data);
//    outputStream.write("\r\n\r\n".getBytes());
//    outputStream.flush();
//  }
//}
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
//            String inputLine;
//            int lineCount = 0;
//            boolean lineCountStart = false;
//            boolean saveImage = false;
//            while ((inputLine = in.readLine()) != null) {
//            	// Should be checking just for "--" probably
//            	if (inputLine.lastIndexOf("--myboundary") > -1)
//            	{
//            		// Got an image boundary, stop last image
//            		// Start counting lines to get past:
//            		// Content-Type: image/jpeg
//            		// Content-Length: 22517
//
//            		saveImage = false;
//            		lineCountStart = true;
//
//            		System.out.println("Got a new boundary");
//            		System.out.println(inputLine);
//            	}
//            	else if (lineCountStart)
//            	{
//            		lineCount++;
//            		if (lineCount >= 2)
//            		{
//            			lineCount = 0;
//            			lineCountStart = false;
//            			imageCount++;
//            			saveImage = true;
//                		System.out.println("Starting a new image");
//
//            		}
//            	}
//            	else if (saveImage)
//            	{
//            		System.out.println("Saving an image line");
//            	}
//            	else {
//
//            		System.out.println("What's this:");
//            		System.out.println(inputLine);
//            	}
//            }
//            in.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }	
}
