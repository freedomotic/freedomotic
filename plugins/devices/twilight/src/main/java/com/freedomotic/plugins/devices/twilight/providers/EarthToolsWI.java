/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.twilight.providers;

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
public class EarthToolsWI implements WeatherInfo{

    private String latitude;
    private String longitude;
    private DateTime nextSunrise;
    private DateTime nextSunset;
     
    public EarthToolsWI(String latitude, String longitude){
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
        Document doc = null;
        String statusFileURL = null;
        
            statusFileURL = "http://new.earthtools.org/sun/" + latitude + "/" + longitude + "/" + dom + "/" + moy + "/" + zone + "/" + dst;
            LOG.log(Level.INFO, "Getting twilight data from: {0}", statusFileURL);
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();

        return doc;
    }
     private static final Logger LOG = Logger.getLogger(EarthToolsWI.class.getCanonicalName());

    @Override
    public boolean updateData() throws Exception {
         DateTime dt = new DateTime();
        int dst = dt.getZone().isStandardOffset(dt.getMillis()) ? 0 : 1;
        int offset = dt.getZone().getStandardOffset(dt.getMillis()) / 3600000;
        //LOG.log(Level.INFO, "Current TIME: {0}/{1} {2} DST: {3}", new Object[]{dt.getDayOfMonth(), dt.getMonthOfYear(), offset, dst});
        Document doc = getXMLStatusFile(dt.getDayOfMonth(), dt.getMonthOfYear(), offset, dst);
        //parse xml 
        if (doc != null) {
            Node sunriseNode = doc.getElementsByTagName("sunrise").item(0);
            Node sunsetNode = doc.getElementsByTagName("sunset").item(0);
            // compara con l'ora attuale
            String srTime[] = sunriseNode.getFirstChild().getNodeValue().split(":");
            String ssTime[] = sunsetNode.getFirstChild().getNodeValue().split(":");
            nextSunrise = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(srTime[0]), Integer.parseInt(srTime[1]), Integer.parseInt(srTime[2]));
            nextSunset = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(ssTime[0]), Integer.parseInt(ssTime[1]), Integer.parseInt(ssTime[2]));
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
