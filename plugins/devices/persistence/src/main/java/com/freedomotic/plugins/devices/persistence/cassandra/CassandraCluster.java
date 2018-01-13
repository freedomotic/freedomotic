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
package com.freedomotic.plugins.devices.persistence.cassandra;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.exceptions.NoHostAvailableException;

/**
 * Configuration class to setup and run Cassandra Cluster in Persistence Plugin.
 *
 * @author P3trur0, https://flatmap.it
 */
public final class CassandraCluster {

    /**
     * Cassandra cluster reference.
     */
    private final Cluster cluster;

    /**
     * This represents the name of the keyspace to be used, <b>freedomotic</b>
     * by default.
     */
    private String keyspace;

    /**
     * The configuration properties.
     */
    private final Properties configurationProperties;

    /**
     * The Constant LOG.
     */
    private final static Logger LOG = LoggerFactory.getLogger(CassandraCluster.class.getName());

    /**
     * The table representation.
     */
    private static final CassandraTable freedomoticData;

    private PreparedStatement insertStatement, selectStatement;

    static {
        List<CassandraColumn> tableColumns = new ArrayList<CassandraColumn>();
        tableColumns.add(new CassandraColumn("id", "uuid", true));
        tableColumns.add(new CassandraColumn("datatype", "varchar"));
        tableColumns.add(new CassandraColumn("data", "blob"));
        tableColumns.add(new CassandraColumn("avro_schema", "text"));
        tableColumns.add(new CassandraColumn("persistence_timestamp", "timestamp"));
        tableColumns.add(new CassandraColumn("freedomoticInstance", "uuid"));

        freedomoticData = new CassandraTable("freedomotic_data", tableColumns);
    }

    /**
     * Instantiates a new cassandra cluster.
     *
     * @param props the properties representing the configuration
     */
    public CassandraCluster(Properties props) {
        this.configurationProperties = props;
        String contactPoint = props.getProperty("cassandra.host", "127.0.0.1");
        this.keyspace = props.getProperty("cassandra.keyspace", "freedomotic");
        LOG.info("Building Cassandra Cluster for host > " + contactPoint);
        LOG.info("Plugin keyspace is " + keyspace);
        cluster = Cluster.builder().addContactPoint(contactPoint).build();
    }

    /**
     * Inits the cluster
     *
     * @return true, if successful
     */
    public boolean init() {
        Session cassandraSession = null;
        String replicationFactor = configurationProperties.getProperty("cassandra.replicationFactor", "1");
        String strategy = configurationProperties.getProperty("cassandra.strategy", "SimpleStrategy");
        try {

            cassandraSession = this.getSession();

            if (!this.isKeyspaceCreated()) {
                this.createFreedomoticKeyspace(cassandraSession, replicationFactor, strategy);
            }

            this.prepareFreedomoticStatements(cassandraSession);

            return true;
        } catch (Exception e) {
            LOG.error("Error while creating Cassandra keyspace for freedomotic", e);

            if (e instanceof NoHostAvailableException) {
                NoHostAvailableException nha = (NoHostAvailableException) e;
                for (Map.Entry<InetSocketAddress, Throwable> error : nha.getErrors().entrySet()) {
                    LOG.error("Connection error  on " + error.getKey().getHostName() + ", port n. " + error.getKey().getPort(), error.getValue());
                }
            }

            return false;
        } finally {
            if (cassandraSession != null) {
                cassandraSession.close();
            }
        }
    }

    /**
     * Creates the freedomotic keyspace on Cassandra
     *
     * @param session the session to be use to create the keyspace
     * @param replicationFactor the replication factor of this cassandra
     * instance.
     * @param strategy the replication strategy of this instance
     */
    public void createFreedomoticKeyspace(Session session, String replicationFactor, String strategy) {
        if (replicationFactor == null) {
            replicationFactor = "1";
        }

        if (strategy == null) {
            strategy = "SimpleStrategy";
        }

        session.execute("CREATE KEYSPACE IF NOT EXISTS " + keyspace + " " + "WITH replication = {'class':'" + strategy
                + "', 'replication_factor':" + replicationFactor + "};");

        session.execute("CREATE TABLE IF NOT EXISTS " + keyspace + "." + freedomoticData.tableToString() + ";");
    }

    /**
     * This method prepares the statements to perform Cassandra data
     * persistence.
     *
     * @param session
     */
    private void prepareFreedomoticStatements(Session session) {
        insertStatement = session.prepare("INSERT INTO " + keyspace + "." + freedomoticData.getName() + " (id, datatype, data, avro_schema, persistence_timestamp, freedomoticInstance) VALUES (?, ?, ?, ?, ?, ?) IF NOT EXISTS");
        selectStatement = session.prepare("SELECT id, datatype, data, avro_schema, persistence_timestamp, freedomoticInstance FROM " + keyspace + "." + freedomoticData.getName());
    }

    /**
     * It creates and return a generic Cassandra session
     *
     * @return the session
     */
    public Session getSession() {
        return cluster.connect();
    }

    /**
     * It creates and return the session referring to freedomotic keyspace.
     *
     * @return the session referring to freedomotic keyspace
     */
    public Session getSessionWithFreedomoticKeyspace() {
        return cluster.connect(keyspace);
    }

    /**
     * It releases the cluster resources
     */
    public void releaseResource() {
        cluster.close();
    }

    /**
     * Checks if is keyspace created.
     *
     * @return true, if is keyspace created
     */
    public boolean isKeyspaceCreated() {
        KeyspaceMetadata ks = cluster.getMetadata().getKeyspace(keyspace);
        if (ks == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Gets the freedomotic table.
     *
     * @return the freedomotic table
     */
    public static CassandraTable getCassandraTable() {
        return freedomoticData;
    }

    /**
     * @return the select prepared statement
     */
    public PreparedStatement getSelectStatement() {
        return selectStatement;
    }

    /**
     * @return the insert prepared statement
     */
    public PreparedStatement getInsertStatement() {
        return insertStatement;
    }

}
