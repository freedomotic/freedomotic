/**
 *
 * Copyright (c) 2009-2018 Freedomotic team http://freedomotic.com
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
package com.freedomotic.plugins.devices.persistence;

import static com.freedomotic.plugins.devices.persistence.util.PersistenceUtility.generateCalendarInMillis;

import java.util.List;
import java.util.UUID;

import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.junit.Assert;
import org.junit.Test;

import com.freedomotic.plugins.devices.persistence.model.FreedomoticDataDAO;
import com.freedomotic.plugins.devices.persistence.model.SerializedPersistedData;
import com.freedomotic.reactions.Command;

/**
 * Unit test for simple App.
 */
public class PersistencePluginTestIT extends CassandraTestSetup {

    /*
	 	It creates a fake event
     */
    private Command getFakeCommandForTest() {
        Command c = new Command();
        c.setProperty("event.date.year", "2016");
        c.setProperty("event.date.month", "12");
        c.setProperty("event.date.day", "8");
        c.setProperty("event.time.hour", "11");
        c.setProperty("event.time.minute", "50");
        c.setProperty("event.time.second", "32");
        c.setProperty("event.type", "event");
        c.setProperty("event.uuid", UUID.randomUUID().toString());
        c.setProperty("event.object.name", "TEST");
        c.setProperty("event.object.protocol", "TEST_PROTOCOL");
        c.setProperty("event.object.address", "123.123.123.123");

        return c;
    }

    @Test
    public void testTimestampValue() {
        Long timestamp = generateCalendarInMillis(this.getFakeCommandForTest());
        Assert.assertEquals(1481194232000l, timestamp.longValue());	//8 December 2016 11:50:32 AM
    }

    /**
     * It performs the saving of an event on Cassandra
     */
    @Test
    public void persistEventOnCassandra() {
        FreedomoticDataDAO dao = new FreedomoticDataDAO(cluster);

        Command c = this.getFakeCommandForTest();
        Long timestamp = generateCalendarInMillis(c);
        String event_uuid = c.getProperty("event.uuid");
        String name = c.getProperty("event.object.name");

        boolean persisted = dao.persistEvent(event_uuid, name, timestamp, c.getProperties().getProperties());
        Assert.assertEquals("Persisted event should have been performed", true, persisted);

        persisted = dao.persistEvent(event_uuid, name, timestamp, c.getProperties().getProperties());
        Assert.assertEquals("Persisted event should NOT have been performed twice", false, persisted);
    }

    /**
     * It persist an event and later it retrieves all the persisted events from
     * Cassandra.
     * <br>
     * Then, it checks if within the persisted events do exist a proper
     * serialized version of the event previously persisted.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void checkIfAPersistedEventIsProperlyRetrievedAndDeserialized() {
        FreedomoticDataDAO dao = new FreedomoticDataDAO(cluster);

        Command c = this.getFakeCommandForTest();
        Long timestamp = generateCalendarInMillis(c);
        String event_uuid = c.getProperty("event.uuid");
        String name = c.getProperty("event.object.name");
        String protocolPropertyToSave = c.getProperty("event.object.protocol");

        boolean persisted = dao.persistEvent(event_uuid, name, timestamp, c.getProperties().getProperties());
        Assert.assertEquals("Persisted event should have been performed", true, persisted);

        List<SerializedPersistedData> events = dao.getPersistedData();

        Assert.assertNotEquals("Retrieved events must be more than zero!", 0, events.size());

        GenericRecord genericRecord = null;

        GenericData.Array<GenericData.Record> properties = null;

        for (SerializedPersistedData event : events) {
            Assert.assertNotNull(event.getFreedomoticInstanceId());
            if (event_uuid.equals(event.getUuid().toString())) {
                genericRecord = event.deserialize();
            }
        }

        Assert.assertNotNull(genericRecord);
        Assert.assertEquals(name, genericRecord.get("name").toString());

        properties = (GenericData.Array<GenericData.Record>) genericRecord.get("properties");

        String savedProtocolProperty = null;

        for (GenericRecord record : properties) {
            String keyname = record.get("key").toString();
            String value = record.get("value").toString();

            if ("event.object.protocol".equals(keyname)) {
                savedProtocolProperty = value;
            }
        }

        Assert.assertEquals(protocolPropertyToSave, savedProtocolProperty);
    }

}
