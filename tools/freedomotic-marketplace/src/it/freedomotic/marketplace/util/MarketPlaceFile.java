/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.marketplace.util;

/**
 *
 * @author gpt
 */
public class MarketPlaceFile {
  
    private String fid;
    private String uid;
    private String filename;
    private String filepath;

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
        return DrupalRestHelper.DRUPALSCHEMA +"://"+ DrupalRestHelper.DRUPALPATH+"/"+filepath;
    }
    /**
     * @return the filepath
     */
    public String getRelativeFilepath() {
        return filepath;
    }
    
    
}
