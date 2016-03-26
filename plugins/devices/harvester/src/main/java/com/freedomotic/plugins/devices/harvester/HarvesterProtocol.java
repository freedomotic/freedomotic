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
package com.freedomotic.plugins.devices.harvester;

import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.events.ProtocolRead;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.model.charting.UsageData;
import com.freedomotic.model.charting.UsageDataFrame;
import com.freedomotic.model.ds.Tuples;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.reactions.Command;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.openjpa.persistence.ArgumentException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Gabriel Pulido de Torres
 */
public final class HarvesterProtocol extends Protocol {

    //Connection connection;
    //PreparedStatement prep;
    EntityManagerFactory factory;
    EntityManager em;
    Properties props;
    String dbType;
    private final static Logger LOG = LoggerFactory.getLogger(HarvesterProtocol.class.getName());

    /**
     *
     */
    public HarvesterProtocol() {
        super("HarvesterProtocol", "/harvester/harvester-manifest.xml");
        this.setName("Harvester");
        setPollingWait(-1); // disable polling
    }

    @Override
    protected void onRun() {

        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().commit();
        }
        //em.getTransaction().begin();
    }

    @Override
    public void onStart() {
        setDescription("Starting...");
        try {
            dbType = configuration.getStringProperty("driver", "h2");

            props = new Properties();
            props.loadFromXML(new FileInputStream(this.getFile().getParent() + File.separator + dbType + ".xml"));
            props.put("openjpa.MetaDataFactory", "org.apache.openjpa.persistence.jdbc.PersistenceMappingFactory(Types=" + UsageData.class.getCanonicalName() + ";)");
            props.put("openjpa.TransactionMode", "local");
            props.put("openjpa.jdbc.SynchronizeMappings", "buildSchema");
            props.put("openjpa.RemoteCommitProvider", "sjvm");
            props.put("openjpa.Log", configuration.getStringProperty("log.options", "DefaultLevel=WARN, Runtime=INFO, Tool=INFO"));

            factory = Persistence.createEntityManagerFactory("UsageData", props);
            em = factory.createEntityManager();
            //setDescription("Saving data to: " + em.getProperties().get("openjpa.ConnectionURL")); // works only with JPA 2.0
            setDescription("Connected to database");
            setPollingWait(2000);
        } catch (FileNotFoundException e) {
            LOG.error("Unable to find configuration file for harvester of type: {}", dbType);
        } catch (ArgumentException e) {
            LOG.warn("ArgumentException {}", e.getLocalizedMessage());
        } catch (Exception e) {
            LOG.error("Exception {}", e.getLocalizedMessage());
            e.printStackTrace();
            stop();
        }

    }

    @Override
    public void onStop() {
        setPollingWait(-1); // disable polling

        try {
            if (em.getTransaction().isActive()) {
                em.getTransaction().commit();
            }
            em.close();
            factory.close();
        } catch (Exception e) {
            LOG.error("Error stopping Harvester: {}", e.getLocalizedMessage());
        }
        this.setDescription("Disconnected");

    }

    @Override
    protected void onCommand(Command c) throws IOException, UnableToExecuteException {
        if (isRunning()) {
            if (c.getProperty("command") == null || c.getProperty("command").isEmpty() || c.getProperty("command").equalsIgnoreCase("SAVE-DATA")) {
                saveData(c);
            } else if (c.getProperty("command").equals("EXTRACT-DATA")) { //extract data
                UsageDataFrame data = extractData(c);
                sendPoints(data, c);
            }
        }
    }

    @Override
    protected boolean canExecute(Command c) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void onEvent(EventTemplate event) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private UsageDataFrame extractData(Command c) {
        String type, id;

        if (c.getProperty("QueryAddress") != null) {
            String[] searchParam = c.getProperty("QueryAddress").split(":");
            type = searchParam[0];
            id = searchParam[1];
        } else {
            type = c.getProperty("FilterType");
            id = c.getProperty("FilterID");
        }
        if (type != null && id != null) {
            Query q = null;

            if (type.startsWith("obj")) {
                // extract data for an object 
                q = em.createNamedQuery("powered");

                String date = c.getProperty("startDate");
                if (date == null || date.isEmpty() || date.equals("CURRENT_DATE")) {
                    q.setParameter("startDate", new Date(0));
                } else {
                    q.setParameter("startDate", new Date(Long.parseLong(date)));
                }

                date = c.getProperty("stopDate");
                if (date == null || date.isEmpty() || date.equals("CURRENT_DATE")) {
                    q.setParameter("stopDate", new Date());
                } else {
                    q.setParameter("stopDate", new Date(Long.parseLong(date)));
                }

                q.setParameter("uuid", id.trim());
                q.setParameter("protocol", "%");

            } else if (type.equals("tag")) {
                Collection<EnvObjectLogic> objs = getApi().things().findAll();

            } else if (type.startsWith("prot")) {
                Collection<EnvObjectLogic> objs = getApi().things().findByProtocol(id);

            } else if (type.equals("room")) {
                // to be implemented
            } else if (type.startsWith("env")) {
                Collection<EnvObjectLogic> objs = getApi().things().findByEnvironment(id);

            }

            UsageDataFrame df = new UsageDataFrame(UsageDataFrame.FULL_UPDATE, q.getResultList());
            LOG.info(df.toString());
            return df;
        } else {
            LOG.warn("Harvester cannot extract data if it misses FilterType or FilterID properties");
            return null;
        }
    }

    private void sendPoints(UsageDataFrame df, Command c) {
        // find command sender (in order to reply)
        // create response, filling data from List

        if (c.getProperty("QueryAddress") != null) {
            ProtocolRead ev = new ProtocolRead(this, "harvester", c.getProperty("QueryAddress"));
            ObjectMapper om = new ObjectMapper();
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                om.writeValue(os, df);
            } catch (IOException ex) {
                LOG.error(ex.getLocalizedMessage());
            }
            ev.addProperty("behaviorValue", os.toString());
            this.notifyEvent(ev);
        } else {
            Tuples t = c.getProperties().getTuples();
            t.clear();
            for (HashMap<String, String> data : df.getDataAsMap()) {
                t.add(data);
            }
        }

    }

    @Override
    protected void onShowGui() {
    }

    private void saveData(Command c) {
        try {
            UsageData item = new UsageData();

            Timestamp ts = new java.sql.Timestamp(
                    Integer.parseInt(c.getProperty("event.date.year")) - 1900,
                    Integer.parseInt(c.getProperty("event.date.month")) - 1,
                    Integer.parseInt(c.getProperty("event.date.day")),
                    Integer.parseInt(c.getProperty("event.time.hour")),
                    Integer.parseInt(c.getProperty("event.time.minute")),
                    Integer.parseInt(c.getProperty("event.time.second")),
                    0);

            item.setDateTime(ts);
            item.setObjName(c.getProperty("event.object.name"));
            item.setObjProtocol(c.getProperty("event.object.protocol"));
            item.setObjAddress(c.getProperty("event.object.address"));
            item.setUuid(c.getProperty("event.object.uuid"));

            //search for all objects behaviors changes    
            Pattern pat = Pattern.compile("^current\\.object\\.behavior\\.(.*)");
            for (Entry<Object, Object> entry : c.getProperties().entrySet()) {
                String key = (String) entry.getKey();
                Matcher fits = pat.matcher(key);
                if (fits.find() && !fits.group(1).equals("data")) { //exclude unwanted behaviors
                    UsageData item2 = item.clone();
                    item2.setObjBehavior(fits.group(1));
                    item2.setObjValue((String) entry.getValue());

                    if (isRunning() && em != null) {
                        if (!em.getTransaction().isActive()) {
                            em.getTransaction().begin();
                        }
                        em.persist(item2);
                    }
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
