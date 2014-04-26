/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.webcam;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;
import com.freedomotic.app.Freedomotic;
import java.awt.Dimension;
import java.awt.Image;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;

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

    public void parseMJPGStream() {
        // preprocess the mjpg stream to remove the mjpg encapsulation
        readLines(4, stream); // reads and discards the first 4 lines
        parseJPGImage();
        readLines(1, stream); // reads and discards the last line
    }

    public void parseJPGImage() { // read the embedded jpeg image
        try {
            JPEGImageDecoder decoder = JPEGCodec.createJPEGDecoder(stream);
            image = decoder.decodeAsBufferedImage();
            int motionLevel = motionDetector.enqueueImage(image);
            plugin.setMotionLevel(motionLevel);
        } catch (Exception e) {
            e.printStackTrace();
            disconnect();
        }
    }

    public void readLines(int n, DataInputStream dis) {
        for (int i = 0; i < n; i++) {
            try {
                boolean end = false;
                String lineEnd = "\n"; // assumes that the end of the line is marked with this
                byte[] lineEndBytes = lineEnd.getBytes();
                byte[] byteBuf = new byte[lineEndBytes.length];

                while (!end) {
                    dis.read(byteBuf, 0, lineEndBytes.length);
                    String t = new String(byteBuf);
                    // System.out.print(t); //uncomment if you want to see what the lines actually look like
                    if (t.equals(lineEnd)) {
                        end = true;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
            while (true) {
                parseMJPGStream();
            }
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
}
