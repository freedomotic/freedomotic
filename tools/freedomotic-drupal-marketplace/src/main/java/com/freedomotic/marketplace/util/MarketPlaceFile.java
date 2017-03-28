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
package com.freedomotic.marketplace.util;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Gabriel Pulido de Torres
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
