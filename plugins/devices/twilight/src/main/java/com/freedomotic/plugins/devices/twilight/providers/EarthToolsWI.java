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
package com.freedomotic.plugins.devices.twilight.providers;

import com.freedomotic.plugins.devices.twilight.WeatherInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Matteo Mazzoni
 */
public class EarthToolsWI implements WeatherInfo {

    private static final Logger LOG = LoggerFactory.getLogger(EarthToolsWI.class.getCanonicalName());
    private final String latitude;
    private final String longitude;
    private ZonedDateTime nextSunrise;
    private ZonedDateTime nextSunset;

    /**
     *
     * @param latitude
     * @param longitude
     */
    public EarthToolsWI(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     *
     * @return
     */
    @Override
    public ZonedDateTime getNextSunset() {
        return nextSunset;
    }

    /**
     *
     * @return
     */
    @Override
    public ZonedDateTime getNextSunrise() {
        return nextSunrise;
    }

    
    private Document getXMLStatusFile(int dom, int moy, int zone, int dst) throws MalformedURLException, SAXException, IOException {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.error(ex.getLocalizedMessage());
        }
        Document doc = null;
        String statusFileURL = null;

        statusFileURL = "http://new.earthtools.org/sun/" + latitude + "/" + longitude + "/" + dom + "/" + moy + "/" + zone + "/" + dst;
        LOG.info("Getting twilight data from: {}", statusFileURL);
        doc = dBuilder.parse(new URL(statusFileURL).openStream());
        doc.getDocumentElement().normalize();

        return doc;
    }

    /**
     *
     * @return @throws Exception
     */
    @Override
    public boolean updateData() throws Exception {
        ZonedDateTime dt = ZonedDateTime.now();
        int dst = dt.getZone().getRules().isDaylightSavings(dt.toInstant()) ? 1 : 0;
        int offset = dt.getZone().getRules().getStandardOffset(dt.toInstant()).getTotalSeconds() / 3600;
        Document doc = getXMLStatusFile(dt.getDayOfMonth(), dt.getMonthValue(), offset, dst);
     
        //parse xml 
        if (doc != null) {
            Node sunriseNode = doc.getElementsByTagName("sunrise").item(0);
            Node sunsetNode = doc.getElementsByTagName("sunset").item(0);
            
            // compare with the current time
            LocalTime srTime = LocalTime.parse(sunriseNode.getFirstChild().getNodeValue());
            LocalTime ssTime = LocalTime.parse(sunsetNode.getFirstChild().getNodeValue());
            nextSunrise = dt.with(srTime);
            nextSunset = dt.with(ssTime);
            LOG.info("Sunrise at: {} Sunset at: {}", nextSunrise, nextSunset);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @param sunset
     */
    @Override
    public void setNextSunset(ZonedDateTime sunset) {
        nextSunset = sunset;
    }

    /**
     *
     * @param sunrise
     */
    @Override
    public void setNextSunrise(ZonedDateTime sunrise) {
        nextSunrise = sunrise;
    }

}
