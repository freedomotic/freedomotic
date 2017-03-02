package com.freedomotic.plugins.devices.persistence.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericData.Record;
import org.apache.avro.generic.GenericRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.freedomotic.app.Freedomotic;
import com.freedomotic.plugins.devices.persistence.cassandra.CassandraCluster;
import com.freedomotic.plugins.devices.persistence.model.avro.FreedomoticData;
import com.freedomotic.plugins.devices.persistence.model.avro.Property;
import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;

/**
 * This class models the persistence of Freedomotic Data
 */
public class FreedomoticDataDAO extends CassandraDAO {

	/** The Constant LOG. */
	private final static Logger LOG = LoggerFactory.getLogger(FreedomoticDataDAO.class.getName());
	
	/**
	 * Instantiates a new dao.
	 *
	 * @param cluster the cluster
	 */
	public FreedomoticDataDAO(CassandraCluster cluster) {
		super(cluster);
	}
	
	/** The Constant freedomotic schema. */
	private static final Schema freedomoticDataSchema = FreedomoticData.getClassSchema();
	private static final Schema propertySchema = Property.getClassSchema();
		
	/** The record injection to serialize AVRO entries. */
	private final Injection<GenericRecord, byte[]> recordInjection = GenericAvroCodecs.toBinary(freedomoticDataSchema);
	
	/**
	 * This returns the list of all the data currently persisted on the cluster.
	 * @return
	 */
	public List<SerializedPersistedData> getPersistedData() {
		Session session = getCluster().getSessionWithFreedomoticKeyspace();
		try {
			List<SerializedPersistedData> result = new ArrayList<SerializedPersistedData>();
			ResultSet resultSet = session.execute(getCluster().getSelectStatement().getQueryString());
			
			for(Row row:resultSet.all()) {
				UUID persistedId = row.get("id", UUID.class);
				String datatype = row.getString("datatype");
				String schema = row.get("avro_schema", String.class);
				byte[] binaryContent = row.getBytes("data").array();
				Date timestamp = row.get("persistence_timestamp", Date.class);
				UUID freedomoticInstanceId = row.get("freedomoticInstance", UUID.class);
				result.add(new SerializedPersistedData(persistedId, datatype, schema, binaryContent, timestamp, freedomoticInstanceId));
			}	
			return result;
		} catch (Exception e) {
			LOG.error("Error while retrieving data from Cassandra, Sorry!", e);
			return new ArrayList<SerializedPersistedData>();
		} finally {
			session.close();
		}
	}
	

	private boolean persistFreedomoticData(String type, String data_uuid, String name, Long timestamp, Properties properties) {	
		GenericData.Record freedomoticRecord = new Record(freedomoticDataSchema);
		freedomoticRecord.put("uuid", data_uuid);
		freedomoticRecord.put("name", name);
		freedomoticRecord.put("datetime", timestamp);
		freedomoticRecord.put("properties", this.retrievePropertiesToSerialize(properties));
	
		Session session = getCluster().getSessionWithFreedomoticKeyspace();
		try {
			ResultSet resultSet = session.execute(
					getCluster().getInsertStatement()
					.bind(
					UUID.fromString(data_uuid),
					type,
					ByteBuffer.wrap(recordInjection.apply(freedomoticRecord)), 
					freedomoticDataSchema.toString(),
					Calendar.getInstance().getTime(),
					Freedomotic.getInstanceIdAsUUID()));
					
			boolean dataGotInserted = resultSet.one().getBool("[applied]");
			
			if(dataGotInserted)
				return true;
			else {
				LOG.warn("The data identified by {} has already been persisted", data_uuid);
				return false;
			}
		} catch (Exception e) {
			LOG.error("Error while persisting an data on Cassandra, so {} has not been persisted. Sorry!", data_uuid);
			e.printStackTrace();
			return false;
		} finally {
			session.close();
		}
	}
	
	/**
	 * Persist event.
	 *
	 * @param event_uuid, the identifier of the event to be persisted
	 * @param name the name of the event
	 * @param timestamp the timestamp of the event
	 * @param properties, the list of properties to serialize.
	 * @return true, if persistence goes ok, false otherwise
	 */
	public boolean persistEvent(String event_uuid, String name, Long timestamp, Properties properties) {
		return this.persistFreedomoticData("event", event_uuid, name, timestamp, properties);
	}
	
	/**
	 * Persist command.
	 *
	 * @param command_uuid, the identifier of the command to be persisted
	 * @param name the name of the command
	 * @param timestamp the timestamp of the command
	 * @param properties, the list of properties to serialize.
	 * @return true, if persistence goes ok, false otherwise
	 */
	public boolean persistCommand(String event_uuid, String name, Long timestamp, Properties properties) {
		return this.persistFreedomoticData("command", event_uuid, name, timestamp, properties);
	}
	
	private List<GenericData.Record> retrievePropertiesToSerialize(Properties properties) {
		List<GenericData.Record> props = new ArrayList<GenericData.Record>(properties.size());
		for(String propertyName:properties.stringPropertyNames()) {
			GenericData.Record property = new Record(propertySchema);
			property.put("key", propertyName);
			property.put("value", properties.getProperty(propertyName));
			props.add(property);
		}	
		return props;
	}

}
