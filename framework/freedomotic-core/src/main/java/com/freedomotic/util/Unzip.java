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
package com.freedomotic.util;

/**
 * Unzip code found on
 * http://stackoverflow.com/questions/981578/how-to-unzip-files-recursively-in-java
 * all credits to respective authors
 */
import com.freedomotic.app.Freedomotic;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

/**
 *
 * @author Enrico Nicoletti
 */
public class Unzip {

    private static final Logger LOG = LoggerFactory.getLogger(Unzip.class.getName());
    private static final int BUFFER = 2048;

    private Unzip() {
    }

    /**
     *
     * @param zipFile
     * @throws ZipException
     * @throws IOException
     */
    public static void unzip(String zipFile) throws IOException {

        if (StringUtils.isEmpty(zipFile)) {
            LOG.error("File path not provided, no unzipping performed");
            return;
        }

        File file = new File(zipFile);

        if (!file.exists()) {
            LOG.error("File not existing, no unzipping performed");
            return;
        }

        try (ZipFile zip = new ZipFile(file);) {
            String newPath = zipFile.substring(0, zipFile.length() - 4);
            //simulates the unzip here feature
            newPath = newPath.substring(0, newPath.lastIndexOf(File.separator));

            Enumeration<? extends ZipEntry> zipFileEntries = zip.entries();

            // Process each entry
            while (zipFileEntries.hasMoreElements()) {
                // grab a zip file entry
                ZipEntry entry = zipFileEntries.nextElement();
                String currentEntry = entry.getName();
                File destFile = new File(newPath, currentEntry);
                File destinationParent = destFile.getParentFile();

                // create the parent directory structure if needed
                destinationParent.mkdirs();

                if (!entry.isDirectory()) {
                    try (BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
                            FileOutputStream fos = new FileOutputStream(destFile);
                            BufferedOutputStream dest = new BufferedOutputStream(fos, BUFFER);) {

                        int currentByte;

                        // establish buffer for writing file
                        byte[] data = new byte[BUFFER];

                        // read and write until last byte is encountered
                        while ((currentByte = is.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, currentByte);
                        }
                    } catch (IOException ex) {
                        LOG.error(Freedomotic.getStackTraceInfo(ex));
                    }
                }

                if (currentEntry.endsWith(".zip")) {
                    // found a zip file, try to open
                    unzip(destFile.getAbsolutePath());
                }
            }
        }
    }

}
