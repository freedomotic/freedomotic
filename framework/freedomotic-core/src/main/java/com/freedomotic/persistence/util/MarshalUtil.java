/**
 * 
 */
package com.freedomotic.persistence.util;

import com.freedomotic.rules.Statement;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;

/**
 * Utility class containing tools to centralize persistence management.
 * 
 * @author Yann Irrilo
 */
public class MarshalUtil {
	
	/**
	 * Write a statement node to a writer for persistence purpose.
	 * @param writer writer to be used for persistence
	 * @param statement object to persist
	 */
	public static void writeNode(HierarchicalStreamWriter writer, Statement statement) {
		writer.startNode("statement");
		writer.startNode("logical");
		writer.setValue(statement.getLogical());
		writer.endNode(); //</logical>
		writer.startNode("attribute");
		writer.setValue(statement.getAttribute());
		writer.endNode(); //</attribute>
		writer.startNode("operand");
		writer.setValue(statement.getOperand());
		writer.endNode(); //</operand>
		writer.startNode("value");
		writer.setValue(statement.getValue());
		writer.endNode(); //</value>
		writer.endNode();
	}
	
}
