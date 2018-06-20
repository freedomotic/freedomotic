/**
 *
 * Copyright (c) 2009-2017 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.twilight.providers;

import com.freedomotic.plugins.devices.twilight.WeatherInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author Matteo Mazzoni
 */
public class OpenWeatherMapWI implements WeatherInfo {

    private static final Logger LOG = LoggerFactory.getLogger(OpenWeatherMapWI.class.getCanonicalName());
    private final String latitude;
    private final String longitude;
    private DateTime nextSunrise;
    private DateTime nextSunset;
    private String nextHumidity;
    private String nextPressure; 
    private String nextTemperature;

    /**
     *
     * @param latitude
     * @param longitude
     * @param humidity
     * @param pressure
     */
    public OpenWeatherMapWI(String latitude, String longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     *
     * @return
     */
    @Override
    public DateTime getNextSunset() {
        return nextSunset;
    }

    /**
     *
     * @return
     */
    @Override
    public DateTime getNextSunrise() {
        return nextSunrise;
    }

    /**
     *
     * @return
     */
    @Override
    public String getNextHumidity(){
        return nextHumidity;
    }

    /**
     *
     * @return
     */
    @Override
    public String getNextPressure(){
        return nextPressure;
    }

    /**
     *
     * @return
     */
    @Override
    public String getNextTemperature(){
        return nextTemperature;
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
        Document doc;
        String statusFileURL = "http://api.openweathermap.org/data/2.5/weather?APPID=45ff563b52c93b074d3d23e46f6fa6a3&lat=" + latitude + "&lon=" + longitude + "&mode=xml&type=accurate&units=metric";
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
        DateTime dt = new DateTime();
        int dst = dt.getZone().isStandardOffset(dt.getMillis()) ? 0 : 1;
        int offset = dt.getZone().getStandardOffset(dt.getMillis()) / 3600000;
        Document doc = getXMLStatusFile(dt.getDayOfMonth(), dt.getMonthOfYear(), offset, dst);

        //parse xml 
        if (doc != null) {
            Node sunriseNode = doc.getElementsByTagName("sun").item(0).getAttributes().getNamedItem("rise");
            Node sunsetNode = doc.getElementsByTagName("sun").item(0).getAttributes().getNamedItem("set");
            Node humidityNode = doc.getElementsByTagName("humidity").item(0).getAttributes().getNamedItem("value");
            Node pressureNode = doc.getElementsByTagName("pressure").item(0).getAttributes().getNamedItem("value");
            Node temperatureNode = doc.getElementsByTagName("temperature").item(0).getAttributes().getNamedItem("value");

            // compare with the current time
            nextSunrise = new DateTime(sunriseNode.getNodeValue()).plusHours(offset);
            nextSunset = new DateTime(sunsetNode.getNodeValue()).plusHours(offset);
            nextHumidity = humidityNode.getNodeValue();
            nextPressure = pressureNode.getNodeValue();
            nextTemperature = temperatureNode.getNodeValue();
            LOG.info("Sunrise at: {} Sunset at: {} Humidity is: {} Pressure is: {} Temperature is: {}", nextSunrise, nextSunset, nextHumidity, nextPressure, nextTemperature);
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
    public void setNextSunset(DateTime sunset) {
        nextSunset = sunset;
    }

    /**
     *
     * @param sunrise
     */
    @Override
    public void setNextSunrise(DateTime sunrise) {
        nextSunrise = sunrise;
    }

    /**
     *
     * @param humidity
     */
    @Override
    public void setNextHumidity(String humidity) {
        nextHumidity = humidity;
    }

    /**
     *
     * @param pressure
     */
    @Override
    public void setNextPressure(String pressure) {
        nextPressure = pressure;
    }

    /**
     *
     * @param pressure
     */
    @Override
    public void setNextTemperature(String temperature) {
        nextTemperature = temperature;
    }

}
