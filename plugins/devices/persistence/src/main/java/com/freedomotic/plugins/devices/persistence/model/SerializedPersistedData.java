/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-iot.com
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

    /**
     * The uuid.
     */
    private final UUID uuid;

    /**
     * The avro schema.
     */
    private final Schema avroSchema;

    /*The type of data (either event or command)*/
    private final String datatype;

    /**
     * The serialized event.
     */
    private final byte[] serializedData;

    /**
     * The persistence timestamp.
     */
    private final Date persistenceTimestamp;

    private final UUID freedomoticInstanceId;

    /**
     * The record injection.
     */
    private final Injection<GenericRecord, byte[]> recordInjection;

    /**
     * Instantiates a new serialized persisted data.
     *
     * @param uuid the uuid
     * @param datatype
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
