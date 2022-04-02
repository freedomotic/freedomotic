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

import com.freedomotic.plugins.devices.persistence.cassandra.CassandraCluster;

/**
 * The abstract class representing the DAO to manage Cassandra entities.
 */
public abstract class CassandraDAO {

    /**
     * The cluster.
     */
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
