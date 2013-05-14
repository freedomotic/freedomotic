/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.wilsonkong888.wifi_id;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.io.InputStream;
import java.io.*;
import java.net.*;
import it.freedomotic.events.ProtocolRead;
import it.freedomotic.objects.EnvObjectLogic;
import it.freedomotic.objects.EnvObjectPersistence;

/**
 *
 * @author Mauro Cicolella
 */
public class Wifi_id extends Protocol{


    public Wifi_id() {
        super("Wifi_id", "/com.wilsonkong888.wifi_id/wifi_id-manifest.xml");
        setPollingWait(configuration.getIntProperty("polling_rate", 5000)); //waits 2000ms in onRun method before call onRun() again
    }

    @Override
    public void onStart() {
        //called when the user starts the plugin from UI

    }

    @Override
    public void onStop() {
        //called when the user stops the plugin from UI

    }

    @Override
    protected void onRun() {
        //called in a loop while this plugin is running
        //loops waittime is specified using setPollingWait()
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line,html="";
        ProtocolRead event;

        try {

            Authenticator.setDefault(new MyAuthenticator());
            //URL url = new URL(configuration.getStringProperty("url", ""));
            //InputStream ins = url.openConnection().getInputStream();
            //BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
            //String str;
            //while((str = reader.readLine()) != null)
                //System.out.println(str);
            
            
            url = new URL(configuration.getStringProperty("url", ""));
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                html=html + line;
            }
            //Freedomotic.logger.config(html);
            html=html.toLowerCase();
            
            for (EnvObjectLogic object : EnvObjectPersistence.getObjectByProtocol("wifi_id")){
                String mac_address = object.getPojo().getPhisicalAddress();
                String name = object.getPojo().getName();
                //Freedomotic.logger.info(mac_address);
                //Freedomotic.logger.info(name);
                if (html.contains(mac_address.toLowerCase())){
                    // user exist
                    event = new ProtocolRead(this, "wifi_id", mac_address);
                    event.addProperty("wifi_id.present", "true");
                    Freedomotic.sendEvent(event);                    
                }else{
                    // user not exist
                    event = new ProtocolRead(this, "wifi_id", mac_address);
                    event.addProperty("wifi_id.present", "false");
                    Freedomotic.sendEvent(event);                       
                }
            }
            is.close();
        
            //print the string in the freedomotic log using INFO level
            //Freedomotic.logger.info(buffer.toString());
            

            
        } catch (MalformedURLException e) {
             //mue.printStackTrace();
            Freedomotic.logger.config("WIFI_id error: "+e.getMessage());
            
        } catch (IOException e) {
             //ioe.printStackTrace();
            Freedomotic.logger.config("WIFI_id error: "+e.getMessage());
        }
 
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        //this method receives freedomotic commands send on channel app.actuators.protocol.arduinousb.in

    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


   class MyAuthenticator extends Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String kuser, kpass;
            // I haven't checked getRequestingScheme() here, since for NTLM
            // and Negotiate, the usrname and password are all the same.
            //System.err.println("Feeding username and password for " + getRequestingScheme());
            kuser = configuration.getStringProperty("url_username", "");
            kpass= configuration.getStringProperty("url_password", "");
            return (new PasswordAuthentication(kuser, kpass.toCharArray()));
        }
    }
}
