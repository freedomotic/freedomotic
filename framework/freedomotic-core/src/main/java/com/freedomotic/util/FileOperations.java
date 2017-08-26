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
 *
 */
package com.freedomotic.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * It is an utility class to perform file operations. <br>
 * For example it contains methods to <i>create files</i>
 *
 * @author P3trur0, https://flatmap.it
 *
 */
public class FileOperations {

    private static final Logger LOG = LoggerFactory.getLogger(FileOperations.class.getName());

    /**
     * This utility method creates a generic summary file.
     * <br>
     * It should contain both an header and a content.
     * <br>
     * <i>If no file is provided, this method returns null.</i>
     * <br>
     * <i>If neither header nor content is provided, this method replace it with
     * an empty string.</i>
     *
     * @param summaryFile the file to be created
     * @param header the header of the file
     * @param content the content of the file
     * @return the summaryFile properly populated
     * @throws IOException if any error happens in processing the file
     */
    public static File writeSummaryFile(File summaryFile, String header, String content) throws IOException {

        if (summaryFile == null) {
            LOG.info("No summary file pointer provided, returning null!");
            return null;
        }

        if (header == null || header.isEmpty()) {
            LOG.warn("No valid header provided, replacing with an empty default header");
            header = "\n";
        }

        if (content == null || content.isEmpty()) {
            LOG.warn("No valid content provided, replacing with an empty default content");
            content = "\n";
        }

        if (!summaryFile.exists()) {
            LOG.info("Creating a new summary file \"{}\"", summaryFile.getAbsolutePath());
            summaryFile.createNewFile();
        }

        try (FileWriter fstream = new FileWriter(summaryFile);
                BufferedWriter indexfile = new BufferedWriter(fstream);) {

            indexfile.write(header);
            indexfile.append(content);
            return summaryFile;
        } catch (IOException e) {
            LOG.error("Error while creating summary file \"{}\"", summaryFile.getAbsolutePath(), e);
            throw e;
        }
    }

}
