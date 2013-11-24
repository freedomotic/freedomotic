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

    private int POLLING_WAIT;
    private String Latitude;
    private String Longitude;

    private TwilightUtils TLU;

    public Twilight() {
        //every plugin needs a name and a manifest XML file
        super("Twilight", "/twilight/twilight-manifest.xml");
        //read a property from the manifest file below which is in
        //FREEDOMOTIC_FOLDER/plugins/devices/it.freedomotic.hello/hello-world.xml
        POLLING_WAIT = configuration.getIntProperty("polling-time", 10000);
        Latitude = configuration.getStringProperty("latitude", "0.0");
        Longitude = configuration.getStringProperty("longitude", "0.0");
        TLU = new TwilightUtils(POLLING_WAIT);

        //default value if the property does not exist in the manifest
        setPollingWait(-1); //millisecs interval between hardware device status reads
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
        notifyEvent(TLU.prepareEvent(DateTime.now()));
    }

    @Override
    protected void onStart() {
        LOG.info("Twilight plugin is started");
        updateData();
        setPollingWait(POLLING_WAIT);
    }

    @Override
    protected void onStop() {
        LOG.info("Twilight plugin is stopped ");
        setPollingWait(-1);
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
            LOG.log(Level.SEVERE, "{0} {1}", new Object[]{Twilight.class.getName(), ex.toString()});
        }
        Document doc = null;
        String statusFileURL = null;
        try {
            statusFileURL = "http://www.earthtools.org/sun/" + Latitude + "/" + Longitude + "/" + dom + "/" + moy + "/" + zone + "/" + dst;
            LOG.log(Level.INFO, "Getting twilight data from: {0}", statusFileURL);
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
            TLU.setSunriseTime(new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(srTime[0]), Integer.parseInt(srTime[1]), Integer.parseInt(srTime[2])));
            TLU.setSunsetTime(new DateTime(dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(),
                    Integer.parseInt(ssTime[0]), Integer.parseInt(ssTime[1]), Integer.parseInt(ssTime[2])));
            LOG.log(Level.INFO, "Sunrise at: {0} Sunset at:{1}", new Object[]{TLU.getSunriseTime(), TLU.getSunsetTime()});
 
            return true;
        } else {
            return false;
        }
    }

    
    
    private static final Logger LOG = Logger.getLogger(Twilight.class.getName());
}