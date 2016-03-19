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
package com.freedomotic.persistence;

import com.freedomotic.exceptions.DataUpgradeException;
import com.freedomotic.model.environment.Environment;
import com.freedomotic.model.object.EnvObject;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import com.freedomotic.settings.Info;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses XSLT (http://en.wikipedia.org/wiki/XSLT) to transform and update XML
 * files. Transformation file should be in conf/validator folder and have a
 * naming schema like /conf/validator/TYPE-upgrade-FROMVERSION.xslt
 * /conf/validator/things-upgrade-5.5.0.xslt"
 *
 * @author Matteo Mazzoni
 */
class DataUpgradeServiceImpl implements DataUpgradeService<String> {

    private static final Logger LOG = LoggerFactory.getLogger(DataUpgradeServiceImpl.class.getCanonicalName());
    // Cache the loaded transformations
    Map<File, Source> sources = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String upgrade(Class type, String xml, String fromVersion) throws DataUpgradeException {

        if (fromVersion.trim().equals(Info.getVersion())) {
            LOG.debug("Given data are already consistent with the most recent framework version. No XML transformation was performed");
            return xml;
        } else {
            LOG.info("Upgrading data of type {} from version {} to version {}",
                    new Object[]{type.getCanonicalName(), fromVersion, Info.getVersion()});
        }

        String upgradedXml;
        Source xsltAlgorthm;
        try {
            if (type == EnvObject.class) {
                xsltAlgorthm = getTransformationAlgorithm("thing", fromVersion);
            } else if (type == Environment.class) {
                xsltAlgorthm = getTransformationAlgorithm("environment", fromVersion);
            } else if (type == Reaction.class) {
                xsltAlgorthm = getTransformationAlgorithm("reaction", fromVersion);
            } else if (type == Command.class) {
                xsltAlgorthm = getTransformationAlgorithm("command", fromVersion);
            } else if (type == Trigger.class) {
                xsltAlgorthm = getTransformationAlgorithm("trigger", fromVersion);
            } else {
                // Return an exception if it's not a class that this service able to upgrade
                throw new DataUpgradeException("Data upgrade service: upgrading entities of type " + type.getCanonicalName() + " is not supported");
            }
            upgradedXml = upgradeContent(xml, xsltAlgorthm);
        } catch (TransformerException | DataUpgradeException transformerException) {
            throw new DataUpgradeException("Error while upgrading an XML data source", transformerException);
        }
        return upgradedXml;
    }

    /**
     * Upgrades a xml content to the current framework version
     *
     * @param input
     * @param xsltAlgorithm
     * @return
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    private String upgradeContent(String input, Source xsltAlgorithm) throws TransformerConfigurationException, TransformerException {
        // Load all the needed resources
        StreamSource streamSource = new StreamSource(new StringReader(input));
        StreamResult streamResult = new StreamResult(new StringWriter());
        // Create the transformer
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xsltAlgorithm);
        // Apply the transformation algorithm defined in the XSLT file
        transformer.transform(streamSource, streamResult);
        return streamResult.getWriter().toString();
    }

    /**
     * Loads the right XSLT transformation script according to the version of
     * the data to transform.
     *
     * @param fromVersion The original data version which should be made
     * compatible with the current framework version
     * @return the transformation Source
     */
    private Source getTransformationAlgorithm(String baseFile, String fromVersion) {
        // Take the source from cache or load it from file
        File xsltFile = new File(Info.PATHS.PATH_CONFIG_FOLDER + "/validator/" + baseFile + "-upgrade-" + fromVersion + ".xslt");
        if (!sources.containsKey(xsltFile)) {
            Source source = new StreamSource(xsltFile);
            sources.put(xsltFile, source);
        }
        Source result = sources.get(xsltFile);
        if (result == null) {
            throw new IllegalStateException("Cannot load a valid XSLT transformation file from " + xsltFile.getAbsolutePath());
        }
        return sources.get(xsltFile);
    }

}
