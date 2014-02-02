/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.resttestclient;

/**
 *
 * @author gpt
 */
public class MarketPlacePluginFileField {

    String fid;
    String description;

    MarketPlacePluginFileField(String fid, String description) {
        this.fid = fid;
        this.description = description;
    }

    public String formatFile() {
        return "\"fid\":\"" + fid + "\",\"data\":{\"description\":\"" + description + "\"}";
    }
}
