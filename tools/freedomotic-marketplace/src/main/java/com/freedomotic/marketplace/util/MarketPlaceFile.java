/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.marketplace.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author gpt
 */
@XmlRootElement
public class MarketPlaceFile {

    private String fid;
    private String uid;
    private String filename;
    private String filepath;
    private transient String description = "";

    public MarketPlaceFile() {
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the filepath
     */
    public String getFilepath() {
        return DrupalRestHelper.DRUPALSCHEMA + "://" + DrupalRestHelper.DRUPALPATH + "/" + filepath;
    }

    /**
     * @return the filepath
     */
    @XmlTransient
    public String getRelativeFilepath() {
        return filepath;
    }

    public MarketPlaceFile(String fid, String description) {
        this.fid = fid;
        this.description = description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String formatFile() {
        return "\"fid\":\"" + fid + "\",\"data\":{\"description\":\"" + description + "\"}";
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
