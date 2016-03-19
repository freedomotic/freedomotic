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
package com.freedomotic.app;

import com.freedomotic.model.ds.Config;
import com.freedomotic.persistence.FreedomXStream;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public class ConfigPersistence {

    private static final Logger LOG = LoggerFactory.getLogger(ConfigPersistence.class.getName());

    /**
     *
     * @param config
     * @param file
     */
    public static void serialize(Config config, File file) {
        FreedomXStream.toXML(config, file);
    }

    /**
     *
     * @param file
     * @return
     * @throws IOException
     * @throws ConversionException
     */
    public static Config deserialize(File file)
            throws IOException, ConversionException {
        LOG.info("Deserializing manifest from " + file.getAbsolutePath());

        XStream xstream = FreedomXStream.getXstream();
        xstream.autodetectAnnotations(true);

        String line;
        StringBuilder xml = new StringBuilder();

        FileInputStream fin = null;
        BufferedReader myInput = null;

        try {
            fin = new FileInputStream(file);
            myInput = new BufferedReader(new InputStreamReader(fin));

            while ((line = myInput.readLine()) != null) {
                xml.append(line).append("\n");
            }

            Config c = null;
            xstream.alias("config", Config.class);
            c = (Config) xstream.fromXML(xml.toString());
            c.setXmlFile(file);

            return c;
        } catch (FileNotFoundException ex) {
            LOG.warn("Error while deserializing configuration from " + file.getAbsolutePath(), ex);
        } finally {
            if (myInput != null) {
                myInput.close();
            }
        }

        return new Config();
    }

    private ConfigPersistence() {
    }
}
