/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.bluetooth_id;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.reactions.Command;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.net.*;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.EnvObjectPersistence;
import com.freedomotic.util.Info;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.bluetooth.*;
import java.util.Vector;
import java.util.logging.Logger;

public class BluetoothID extends Protocol {

    public static final Logger LOG = Logger.getLogger(BluetoothID.class.getName());
    public static final Vector/*
             * <RemoteDevice>
             */ devicesDiscovered = new Vector();
    String address_list;
    String attachment = "";
    String capture_path = Info.PATHS.PATH_PLUGINS_FOLDER + File.separator + "bluetooth-id" + File.separator + "capture";
    short i = 0;

    public BluetoothID() {
        super("Bluetooth_id", "/bluetooth-id/bluetooth-id-manifest.xml");
        setPollingWait(-1); //disable polling
    }

    @Override
    public void onStart() {
        //called when the user starts the plugin from UI
        deleteFilesOlderThanNdays(2, capture_path);
    }

    @Override
    public void onStop() {
        //called when the user stops the plugin from UI
    }

    @Override
    protected void onRun() {
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        deleteFilesOlderThanNdays(2, capture_path);
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //this method receives freedomotic commands send on channel app.actuators.protocol.arduinousb.in
        searchBluetooth();
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public static void saveImage(String imageUrl, String destinationFile) throws IOException {
        URL url = new URL(imageUrl);
        InputStream is = url.openStream();
        OutputStream os = new FileOutputStream(destinationFile);

        byte[] b = new byte[90480];
        int length;

        while ((length = is.read(b)) != -1) {
            os.write(b, 0, length);
        }

        is.close();
        os.close();
    }

    public void deleteFilesOlderThanNdays(int daysBack, String dirWay) {

        File directory = new File(dirWay);
        System.out.println("Bluetooth ID: Deleting files in directory " + dirWay);
        if (directory.exists()) {

            File[] listFiles = directory.listFiles();
            long purgeTime = System.currentTimeMillis() - (daysBack * 24 * 60 * 60 * 1000);
            for (File listFile : listFiles) {
                if (listFile.lastModified() < purgeTime) {
                    //System.out.println(listFile.toString()+","+purgeTime+","+listFile.lastModified());
                    if (!listFile.delete()) {
                        System.out.println("Bluetooth ID plugin: Unable to delete file: " + listFile);
                    }
                }
            }
        } else {
            System.out.println("Bluetooth ID plugin: Files were not deleted, directory " + dirWay + " does'nt exist!");
        }
    }

    public void searchBluetooth() {
        try {
            address_list = "";
            short capture_no = 4;
            Date date;
            SimpleDateFormat ft = new SimpleDateFormat("yyyy.MM.dd_HH.mm.ss");
            //ReadableByteChannel rbc;
            // FileOutputStream fos;
            String path;
            // Capture photo from IP cam
            String web = "http://192.168.1.106:81/snapshot.cgi?user=admin&pwd=recrgt&resolution=32";
            //String web="http://192.168.1.108:8001/snapshot.cgi?user=admin&pwd=recrgt&resolution=32&rate=";
            //String web=configuration.getStringProperty("camera_link", "http://localhost");
            attachment = "";
            for (i = 0; i < 4; i++) {
                Thread.sleep(1000);
                date = new Date();
                path = Info.PATHS.PATH_DEVICES_FOLDER + File.separator + "bluetooth-id" + File.separator + "capture" + File.separator + ft.format(date) + ".jpg";
                attachment = attachment + path + ",";
                System.out.println("Wilson Debug " + i + ":" + attachment);
                //rbc = Channels.newChannel(website.openStream());
                saveImage(web, path);
                //fos = new FileOutputStream(path);
                //fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                //fos.close();
                //rbc.close();
            }
            //System.out.println(attachment);

            //String web="http://192.168.1.106:81/snapshot.cgi?user=admin&pwd=recrgt";
            //path = Info.getDevicesPath() + File.separator + "com.wilsonkong888.bluetooth_id"+ File.separator+"capture"+ File.separator+ft.format(date)+".jpg";
            //saveImage(web,path);
            final Object inquiryCompletedEvent = new Object();

            devicesDiscovered.clear();

            DiscoveryListener listener = new DiscoveryListener() {

                public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                    System.out.println("Device " + btDevice.getBluetoothAddress() + " found");
                    address_list = address_list + btDevice.getBluetoothAddress();
                    devicesDiscovered.addElement(btDevice);
                    try {
                        System.out.println("     name " + btDevice.getFriendlyName(false));
                    } catch (IOException cantGetDeviceName) {
                    }
                }

                public void inquiryCompleted(int discType) {
                    try {
                        System.out.println("Device Inquiry completed!");
                        synchronized (inquiryCompletedEvent) {
                            inquiryCompletedEvent.notifyAll();
                        }
                        boolean thief = true;

                        //System.out.println("Address list :"+address_list);
                        for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("bluetooth_id")) {
                            String mac_address = object.getPojo().getPhisicalAddress();
                            String name = object.getPojo().getName();
                            ProtocolRead event;
                            //System.out.println(mac_address);
                            //System.out.println(name);
                            address_list = address_list.toLowerCase();
                            if (address_list.contains(mac_address.toLowerCase())) {
                                // user exist

                                event = new ProtocolRead(this, "bluetooth-id", mac_address);
                                event.addProperty("bluetooth-id.present", "true");
                                thief = false;
                                Freedomotic.sendEvent(event);
                            } else {
                                // user not exist
                                event = new ProtocolRead(this, "bluetooth-id", mac_address);
                                event.addProperty("bluetooth-id.present", "false");
                                Freedomotic.sendEvent(event);

                            }
                        }
                        if (thief == true) {
                            final Command c = new Command();
                            c.setName("Send Home Invasion");
                            c.setReceiver("app.actuators.messaging.mailWilson.in");
                            c.setProperty("subject", "Invasion detected at your home!");
                            c.setProperty("message", "Please check your home immediately!");
                            c.setProperty("attachment", attachment);
                            c.setReplyTimeout(10000); //10 seconds
                            Freedomotic.sendCommand(c);
                        }
                    } catch (Exception ex) {
                        System.out.println("Bluetooth ID error: " + ex.getMessage());
                    }
                }

                public void serviceSearchCompleted(int transID, int respCode) {
                }

                public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                }
            };
            /*
             * synchronized(inquiryCompletedEvent) { boolean started =
             * LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC,
             * listener); if (started) { System.out.println("wait for device
             * inquiry to complete..."); inquiryCompletedEvent.wait();
             * System.out.println(devicesDiscovered.size() + " device(s)
             * found"); }
             }
             */
            boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);

            if (started) {
                System.out.println("Bluetooth-id: wait for device inquiry to complete...");
                //inquiryCompletedEvent.wait();
                //System.out.println(devicesDiscovered.size() +  " device(s) found");
            }

            //System.out.println("Address list :"+address_list);
            //System.out.println("Address list :");
        } catch (Exception e) {
            //ioe.printStackTrace();
            LOG.config("Bluetooth ID error: " + e.getMessage());
        }
    }
}
