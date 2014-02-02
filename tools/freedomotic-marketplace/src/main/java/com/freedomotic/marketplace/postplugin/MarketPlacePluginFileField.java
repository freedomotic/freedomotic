/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.postplugin;

/**
 * Class that represents one of the Fields that is uploaded and associated with
 * a plugin node
 *
 * @author gpt
 */
public class MarketPlacePluginFileField {

    String fid;
    String description;

    public MarketPlacePluginFileField(String fid, String description) {
        this.fid = fid;
        this.description = description;
    }

    public String formatFile() {
        return "\"fid\":\"" + fid + "\",\"data\":{\"description\":\"" + description + "\"}";
    }
}
