/*
 Copyright (c) Matteo Mazzoni <matteo@bestmazzo.it> 2013   
   
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */
package it.mazzoni.twilight;

import it.freedomotic.api.EventTemplate;
import it.freedomotic.api.Protocol;
import it.freedomotic.app.Freedomotic;
import it.freedomotic.events.GenericEvent;
import it.freedomotic.exceptions.UnableToExecuteException;
import it.freedomotic.reactions.Command;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class Twilight extends Protocol {

    final int POLLING_WAIT;
    private String Latitude;
    private String Longitude;
    private DateTime sunriseTime;
    private DateTime sunsetTime;
    private Duration toSunset;
    private Duration toSunrise;

    public Twilight() {
        //every plugin needs a name and a manifest XML file
        super("Twilight", "/twilight/twilight-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/it.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("time-between-reads", 10000);
        Latitude = configuration.getStringProperty("latitude", "0.0");
        Longitude = configuration.getStringProperty("longitude", "0.0");
        //POLLING_WAIT is the value of the property "time-between-reads" or 2000 millisecs,
        //default value if the property does not exist in the manifest
        setPollingWait(POLLING_WAIT); //millisecs interval between hardware device status reads
    }

    @Override
    protected void onShowGui() {
        /**
         * uncomment the line below to add a GUI to this plugin the GUI can be
         * started with a right-click on plugin list on the desktop frontend
         * (it.freedomotic.jfrontend plugin)
         */
        //bindGuiToPlugin(new HelloWorldGui(this));
    }

    @Override
    protected void onHideGui() {
        //implement here what to do when the this plugin GUI is closed
        //for example you can change the plugin description
        setDescription("My GUI is now hidden");
    }

    @Override
    protected void onRun() {

        // genera evento: 
        GenericEvent ev = new GenericEvent(getClass());
        ev.setDestination("app.event.sensor.calendar.event.twilight");

        if (toSunset.getMillis() < POLLING_WAIT / 2) {
            // it's sunset
            ev.addProperty("isSunset", "true");
        } else if (toSunrise.getMillis() < POLLING_WAIT / 2) {
            // it's sunrise
            ev.addProperty("isSunrise", "true");
        } else if (sunriseTime.isAfterNow()) {
            // prima dell'alba
            ev.addProperty("beforeSunrise", Long.toString(toSunrise.getStandardMinutes()));
        } else if (sunsetTime.isBeforeNow()) {
            // dopo il tramonto
            ev.addProperty("afterSunset", Long.toString(toSunset.getStandardMinutes()));
        } else if (toSunrise.isShorterThan(toSunset)) {
            // dopo l'alba, 
            ev.addProperty("afterSunrise", Long.toString(toSunrise.getStandardMinutes()));
        } else {
            // prima del tramonto
            ev.addProperty("beforeSunset", Long.toString(toSunset.getStandardMinutes()));
        }

    }

    @Override
    protected void onStart() {
        LOG.info("Twilight plugin is started");
        updateData();
    }

    @Override
    protected void onStop() {
        LOG.info("Twilight plugin is stopped ");
    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        String command = c.getProperty("command");
        if (command.equals("Update Twilight Data")) {
            updateData();
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        //don't mind this method for now
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private Document getXMLStatusFile(int dom, int moy, int zone, int dst) {
        //get the xml file from the socket connection
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = null;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            LOG.severe(Twilight.class.getName() + ex.toString());
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://www.earthtools.org/sun/" + Latitude + "/" + Longitude + "/" + dom + "/" + moy + "/" + zone + "/" + dst;
            LOG.info("Getting twilight data from: " + statusFileURL);
            doc = dBuilder.parse(new URL(statusFileURL).openStream());
            doc.getDocumentElement().normalize();
        } catch (ConnectException connEx) {
            this.stop();
            this.setDescription("Connection timed out, no reply from  " + statusFileURL);
        } catch (SAXException ex) {
            this.stop();
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        } catch (Exception ex) {
            this.stop();
            setDescription("Unable to connect to " + statusFileURL);
            LOG.severe(Freedomotic.getStackTraceInfo(ex));
        }
        return doc;
    }

    private boolean updateData() {
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
            sunriseTime = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(srTime[0]), Integer.parseInt(srTime[1]), Integer.parseInt(srTime[2]));
            sunsetTime = new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(ssTime[0]), Integer.parseInt(ssTime[1]), Integer.parseInt(ssTime[2]));
            LOG.info("Sunset at:" + sunsetTime + ", sunrise at: " + sunriseTime);
            toSunset = sunsetTime.isAfter(dt) ? new Duration(dt, sunsetTime) : new Duration(sunsetTime, dt);
            toSunrise = sunriseTime.isAfter(dt) ? new Duration(dt, sunriseTime) : new Duration(sunriseTime, dt);

            return true;
        } else {
            return false;
        }
    }
    
    private static final Logger LOG = Logger.getLogger(Twilight.class.getName());
}