/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.twilight.providersit.mazzoni.twilight.providers;

import com.freedomotic.plugins.devices.twilight.WeatherInfo;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 *
 * @author matteo
 */
public class OpenWeatherMapWI implements WeatherInfo{

    private String latitude;
    private String longitude;
    private DateTime nextSunrise;
    private DateTime nextSunset;
     
    public OpenWeatherMapWI(String latitude, String longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }
    
    @Override
    public DateTime getNextSunset() {
        return nextSunset;
    }

    @Override
    public DateTime getNextSunrise() {
        return nextSunrise;
    }
    
     private Document getXMLStatusFile(int dom, int moy, int zone, int dst) throws MalformedURLException, SAXException, IOException {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.severe(ex.getLocalizedMessage());
        }
        Document doc ;
        String statusFileURL = "http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude+ "&mode=xml&type=accurate&units=metric" ;
            LOG.log(Level.INFO, "Getting twilight data from: {0}", statusFileURL);
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();

        return doc;
    }
     private static final Logger LOG = Logger.getLogger(OpenWeatherMapWI.class.getCanonicalName());

    @Override
    public boolean updateData() throws Exception {
         DateTime dt = new DateTime();
        int dst = dt.getZone().isStandardOffset(dt.getMillis()) ? 0 : 1;
        int offset = dt.getZone().getStandardOffset(dt.getMillis()) / 3600000;
        //LOG.log(Level.INFO, "Current TIME: {0}/{1} {2} DST: {3}", new Object[]{dt.getDayOfMonth(), dt.getMonthOfYear(), offset, dst});
        Document doc = getXMLStatusFile(dt.getDayOfMonth(), dt.getMonthOfYear(), offset, dst);
        //parse xml 
        if (doc != null) {
            Node sunriseNode = doc.getElementsByTagName("sun").item(0).getAttributes().getNamedItem("rise");
            Node sunsetNode = doc.getElementsByTagName("sun").item(0).getAttributes().getNamedItem("set");
            // compara con l'ora attuale
            
            nextSunrise = new DateTime(sunriseNode.getNodeValue()).plusHours(offset);
            nextSunset = new DateTime(sunsetNode.getNodeValue()).plusHours(offset);
            LOG.log(Level.INFO, "Sunrise at: {0} Sunset at: {1}", new Object[]{nextSunrise, nextSunset});
 
            return true;
        } else {
            return false;
        }
    }
        @Override
    public void setNextSunset(DateTime sunset) {
        nextSunset = sunset;
    }

    @Override
    public void setNextSunrise(DateTime sunrise) {
        nextSunrise=sunrise;
    }
}
