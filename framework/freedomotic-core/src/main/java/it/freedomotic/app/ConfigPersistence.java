/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.app;

import it.freedomotic.model.ds.Config;
import it.freedomotic.persistence.FreedomXStream;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.ConversionException;

/**
 *
 * @author gpt
 */
public class ConfigPersistence {

    public static void serialize(Config config, File file) {
        XStream xstream = FreedomXStream.getXstream();
        xstream.autodetectAnnotations(true);
        String xml = xstream.toXML(config);
        // FileOutputStream fout;
        FileWriter fstream;
        try {
            fstream = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(xml);
            //Close the output stream
            out.close();
            //Freedomotic.logger.info("  configuration succesfully serialized");
        } catch (IOException ex) {
            Logger.getLogger(Config.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Config deserialize(File file) throws IOException, ConversionException {
        Freedomotic.logger.config("Deserializing configuration from " + file.getAbsolutePath());
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
        } catch (FileNotFoundException fileNotFoundException) {
            Freedomotic.logger.warning(fileNotFoundException.getLocalizedMessage());
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
