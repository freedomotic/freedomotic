package com.freedomotic.plugins.devices.persistence.model;

import java.util.Date;
import java.util.UUID;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;

import com.twitter.bijection.Injection;
import com.twitter.bijection.avro.GenericAvroCodecs;

/**
 * The Class SerializedPersistedData
 */
public class SerializedPersistedData {
	
	/** The uuid. */
	private final UUID uuid;
	
	/** The avro schema. */
	private final Schema avroSchema;
	
	/*The type of data (either event or command)*/
	private final String datatype;
	
	/** The serialized event. */
	private final byte[] serializedData;
	
	/** The persistence timestamp. */
	private final Date persistenceTimestamp;
	
	private final UUID freedomoticInstanceId;
	
	/** The record injection. */
	private final Injection<GenericRecord, byte[]> recordInjection;
	
	/**
	 * Instantiates a new serialized persisted data.
	 *
	 * @param uuid the uuid
	 * @param avroSchema the avro schema
	 * @param serializedData the serialized data
	 * @param timestamp the timestamp
	 */
	public SerializedPersistedData(UUID uuid, String datatype, String avroSchema, byte[] serializedData, Date timestamp, UUID freedomoticInstanceId) {
		super();
		this.uuid = uuid;
		this.datatype = datatype;
		this.avroSchema = new Schema.Parser().parse(avroSchema);
		this.serializedData = serializedData;
		this.persistenceTimestamp = timestamp;
		this.recordInjection = GenericAvroCodecs.toBinary(this.avroSchema);
		this.freedomoticInstanceId = freedomoticInstanceId;
	}
	
	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public UUID getUuid() {
		return uuid;
	}
	
	/**
	 * Gets the avro schema.
	 *
	 * @return the avro schema
	 */
	public Schema getAvroSchema() {
		return avroSchema;
	}
	
	/**
	 * Gets the serialized data.
	 *
	 * @return the serialized data
	 */
	public byte[] getSerializedData() {
		return serializedData;
	}
	
	/**
	 * Gets the timestamp.
	 *
	 * @return the timestamp
	 */
	public Date getTimestamp() {
		return persistenceTimestamp;
	}
	
	/**
	 * Deserialize the data
	 *
	 * @return the deserialized object as generic record
	 */
	public GenericRecord deserialize() {
		return this.recordInjection.invert(serializedData).get();
	}

	/**
	 * @return the datatype
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * @return the freedomoticInstanceId
	 */
	public UUID getFreedomoticInstanceId() {
		return freedomoticInstanceId;
	}
	
}
