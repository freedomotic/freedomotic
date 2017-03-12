package com.freedomotic.plugins.devices.persistence.model;

import com.freedomotic.plugins.devices.persistence.cassandra.CassandraCluster;


/**
 * The abstract class representing the DAO to manage Cassandra entities.
 */
public abstract class CassandraDAO {
	
	/** The cluster. */
	private final CassandraCluster cluster;

	/**
	 * Instantiates a new cassandra DAO passing a Cassandra Cluster reference.
	 *
	 * @param cluster the cluster
	 */
	public CassandraDAO(CassandraCluster cluster) {
		this.cluster = cluster;
	}

	/**
	 * Gets the cluster.
	 *
	 * @return the cluster
	 */
	public CassandraCluster getCluster() {
		return cluster;
	}
	
	
}
