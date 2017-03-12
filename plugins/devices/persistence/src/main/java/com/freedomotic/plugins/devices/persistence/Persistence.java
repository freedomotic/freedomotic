package com.freedomotic.plugins.devices.persistence;

import static com.freedomotic.plugins.devices.persistence.util.PersistenceUtility.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map.Entry;
import java.util.Calendar;
import java.util.Properties;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Session;
import com.freedomotic.api.EventTemplate;
import com.freedomotic.api.Protocol;
import com.freedomotic.exceptions.UnableToExecuteException;
import com.freedomotic.plugins.devices.persistence.cassandra.CassandraCluster;
import com.freedomotic.plugins.devices.persistence.model.FreedomoticDataDAO;
import com.freedomotic.reactions.Command;


/**
 * The Class Persistence represents the entry point of the Plugin
 */
public class Persistence extends Protocol {

	/** The Constant LOG. */
	private final static Logger LOG = LoggerFactory.getLogger(Persistence.class.getName());

	/** The Cassandra cluster reference. */
	private CassandraCluster cluster;
	
	/** The DEO to manage event persistence. */
	private FreedomoticDataDAO dao;

	/**
	 * Instantiates a new Persistence plugin instance.
	 *
	 */
	public Persistence() {
		super("Persistence", "/persistence/persistence-manifest.xml");
		this.setName("Persistence");
		setPollingWait(-1); // disable polling
	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Plugin#onStart()
	 */
	@Override
	public void onStart() {
		setDescription("Starting...");
		Session cassandraSession = null;
		try {
			cluster = new CassandraCluster(configuration.getProperties());
			boolean clusterInitialized = cluster.init();
			LOG.info("Cassandra cluster inizialized > " + clusterInitialized);
			dao = new FreedomoticDataDAO(cluster);
			setDescription("Persistence plugin initialized.");
            setPollingWait(2000);
		} catch (Exception e) {
			LOG.error("Exception {}", e.getLocalizedMessage());
			e.printStackTrace();
			stop();
		} finally {
			if (cassandraSession != null)
				cassandraSession.close();
		}
	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Protocol#onRun()
	 */
	@Override
	protected void onRun() {

	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Plugin#onStop()
	 */
	@Override
	public void onStop() {
		setPollingWait(-1); // disable polling
		try {
			dao = null;
			cluster.releaseResource();
		} catch (Exception e) {
			LOG.error("Error stopping Persistence plugin: {}", e.getLocalizedMessage());
		}
		this.setDescription("Disconnected");
	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Protocol#onCommand(com.freedomotic.reactions.Command)
	 */
	@Override
	protected void onCommand(Command c) throws IOException, UnableToExecuteException {
		if (isRunning()) {
			if (c.getProperty("command") == null || c.getProperty("command").isEmpty()
					|| c.getProperty("command").equalsIgnoreCase("SAVE-DATA")) {
				this.persist(c);
			}
		}
	}

	/**
	 * Persist the event retrieved from the command passed as input
	 *
	 * @param c is the command containing the event to be persisted
	 */
	private void persist(Command c) {
		try {
			
			Long timestamp = Calendar.getInstance().getTimeInMillis();
			
			if(isTimestampExistingOnEventProperties(c))
				timestamp = generateCalendarInMillis(c);
			
			String event_UUID = c.getProperty("event.uuid");
			String eventType = c.getProperty("event.type");
		
			if(event_UUID!=null && "event".equalsIgnoreCase(eventType)) {
				String eventName = c.getProperty("event.object.name");
				dao.persistEvent(event_UUID, eventName, timestamp, c.getProperties().getProperties());
			}
			
			else {
				String command = c.getProperty("type");
				String eventName = c.getProperty("event.object.name");
				if("command".equalsIgnoreCase(command))
					dao.persistCommand(event_UUID, eventName, timestamp, c.getProperties().getProperties());
			}
			
			
			
			
		} catch (Exception ex) {
			LOG.error(ex.getLocalizedMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Protocol#canExecute(com.freedomotic.reactions.Command)
	 */
	@Override
	protected boolean canExecute(Command c) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	/* (non-Javadoc)
	 * @see com.freedomotic.api.Protocol#onEvent(com.freedomotic.api.EventTemplate)
	 */
	@Override
	protected void onEvent(EventTemplate event) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

}
