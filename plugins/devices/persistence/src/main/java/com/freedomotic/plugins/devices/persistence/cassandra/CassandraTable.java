package com.freedomotic.plugins.devices.persistence.cassandra;

import java.util.List;

/**
 * Generic POJO class to model a Cassandra table
 */
public class CassandraTable {
	
	/** The name. */
	private final String name;
	
	/** The columns. */
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
	 * Tit returns a string representaton of the table.
	 * It should be used to prepare a table generation script
	 *
	 * @return the string
	 */
	public String tableToString() {		
		StringBuffer sb = new StringBuffer(name);
		sb.append(" (");
		
		for(CassandraColumn column:this.columns) {
			sb.append(column.toString());
			sb.append(", ");
		}
		sb.replace(sb.lastIndexOf(", "),sb.length(),"");
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
		return this.name+" "+this.type+((primaryKey)?" PRIMARY KEY" : "");
	}
	
}
