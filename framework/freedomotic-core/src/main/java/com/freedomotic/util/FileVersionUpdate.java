package com.freedomotic.util;

/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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

import java.io.File;
import java.io.StringWriter;
import java.util.logging.Logger;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author matteo
 */
public class FileVersionUpdate {

    private static final Logger LOG = Logger.getLogger(FileVersionUpdate.class.getCanonicalName());

    public static void updateThing(File src) {
        try {
            TransformerFactory factory = TransformerFactory.newInstance();
            Source xslt = new StreamSource(new File(Info.PATHS.PATH_CONFIG_FOLDER + "/validator/object-update-" + Info.getVersion() + ".xslt"));
            Transformer transformer = factory.newTransformer(xslt);
StringWriter sw = new StringWriter();
            Source text = new StreamSource(src);
           
            transformer.transform(text, new StreamResult(sw));
            LOG.info(sw.toString());
            //LOG.info(res);
        } catch (TransformerConfigurationException w) {
            LOG.severe(w.getLocalizedMessage());
        } catch (TransformerException z) {
            LOG.severe(z.getLocalizedMessage());
        }
    }

}
