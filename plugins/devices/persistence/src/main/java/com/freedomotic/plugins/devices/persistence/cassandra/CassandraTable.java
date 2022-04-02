/**
 *
 * Copyright (c) 2009-2022 Freedomotic Team http://www.freedomotic-platform.com
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

import java.util.List;

/**
 * Generic POJO class to model a Cassandra table
 */
public class CassandraTable {

    /**
     * The name.
     */
    private final String name;

    /**
     * The columns.
     */
    private final List<CassandraColumn> columns;

    /**
     * Instantiates a new cassandra table.
     *
     * @param name the name
     * @param columns the columns
     */
    public CassandraTable(String name, List<CassandraColumn> columns) {
        super();
        this.name = name;
        this.columns = columns;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Tit returns a string representaton of the table. It should be used to
     * prepare a table generation script
     *
     * @return the string
     */
    public String tableToString() {
        StringBuilder sb = new StringBuilder(name);
        sb.append(" (");

        for (CassandraColumn column : this.columns) {
            sb.append(column.toString());
            sb.append(", ");
        }
        sb.replace(sb.lastIndexOf(", "), sb.length(), "");
        return sb.append(")").toString();
    }
}

class CassandraColumn {

    private final String name;
    private final String type;
    private final boolean primaryKey;

    public CassandraColumn(String name, String type, boolean primaryKey) {
        super();
        this.name = name;
        this.type = type;
        this.primaryKey = primaryKey;
    }

    public CassandraColumn(String name, String type) {
        super();
        this.name = name;
        this.type = type;
        this.primaryKey = false;
    }

    @Override
    public String toString() {
        return this.name + " " + this.type + ((primaryKey) ? " PRIMARY KEY" : "");
    }

}
