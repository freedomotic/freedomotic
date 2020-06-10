/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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
package com.freedomotic.plugins.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
class FetchHttpFiles {

    private static final Logger LOG = LoggerFactory.getLogger(FetchHttpFiles.class.getName());
    
    private FetchHttpFiles() {}

    /**
     *
     * @param url
     * @param destFolder
     * @param filename
     * @return
     * @throws IOException 
     * @throws Exception
     */
    public static boolean download(URL url, File destFolder, String filename) throws IOException {
        File destinationFile = new File(destFolder.getPath(), filename);
        LOG.info("Plugin download started");
        LOG.info("Source folder: \"{}\"", url);
        LOG.info("Destination folder: \"{}\"", destinationFile);

        URLConnection urlc = url.openConnection();
        
        boolean result = false;

		try (BufferedInputStream bis = new BufferedInputStream(urlc.getInputStream());
			BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(destinationFile.getPath()));) {  
            int i;

            while ((i = bis.read()) != -1) {
                bos.write(i);
            } 
            
            result = true;
        } 

        LOG.info("Plugin download completed");
        return result;
    }

}
