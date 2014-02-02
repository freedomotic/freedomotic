/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.resttestclient;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * POJO class that represents a plugin resume page from the Drupal Marketplace
 *
 * @author GGPT
 */
public class MarketPlacePluginResume {

    private String title;
    private String uri;
//    private String nid;
//    private String vid;
//    private String type;
//    private String language;
//    private String uid;
//    private String status;
//    private String created;
//    private String changed;
//    private String comment;
//    private String promote;
//    private String moderate;
//    private String sticky;
//    private String tnid;
//    private String translate;
    //Data retrieved from the uri
    private MarketPlacePlugin marketPlacePlugin;

    public void setMarketPlacePlugin() {
        if (this.marketPlacePlugin == null) {

            if (this.uri != null) {
                ClientResource cr = new ClientResource(this.uri);
                Representation test2 = cr.get();
                String jsonData;
                try {
                    jsonData = test2.getText();
                    marketPlacePlugin = new MarketPlacePlugin();
                    marketPlacePlugin.parseJson(jsonData);
                } catch (IOException ex) {
                    Logger.getLogger(MarketPlacePluginResume.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
    }
}
