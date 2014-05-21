/**
 *
 * Copyright (c) 2009-2014 Freedomotic team http://freedomotic.com
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico
 */
@XmlRootElement
public final class Payload
        implements Serializable {

    private static final long serialVersionUID = -5799483105084939108L;
    @XmlElement
    List<Statement> payload = new CopyOnWriteArrayList<Statement>();

    /**
     *
     * @param logical
     * @param attribute
     * @param operand
     * @param value
     * @throws NullPointerException
     */
    public void addStatement(String logical, String attribute, String operand, String value)
            throws NullPointerException {
        enqueueStatement(new Statement().create(logical, attribute, operand, value));
    }

    /**
     *
     * @param attribute
     * @param value
     */
    public void addStatement(String attribute, String value) {
        enqueueStatement(new Statement().create(Statement.AND, attribute, Statement.EQUALS, value));
    }

    /**
     *
     * @param attribute
     * @param value
     */
    public void addStatement(String attribute, int value) {
        enqueueStatement(new Statement().create(Statement.AND,
                attribute,
                Statement.EQUALS,
                Integer.toString(value)));
    }

    /**
     *
     * @param s
     */
    public void enqueueStatement(Statement s) {
        if ((s != null) && !payload.contains(s)) {
            payload.add(s);
        }
    }

    /**
     *
     * @return
     */
    public int size() {
        return payload.size();
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        boolean payloadConsistence = true;

        if (obj instanceof Payload) {
            Payload eventPayload = (Payload) obj;
            Iterator<Statement> it = payload.iterator();

            //check all statement for consistency
            while (it.hasNext()) {
                Statement triggerStatement = it.next();

                List<Statement> filteredEventStatements = eventPayload.getStatements(triggerStatement.attribute);

                if (filteredEventStatements.isEmpty()) {
                    // no statement present into event, corresponding filtered trigger statement
                    if (triggerStatement.value.equalsIgnoreCase(Statement.ANY)) {
                        // if trigger value = ANY, we expectected at least one matching statement, so test fails.
                        if (triggerStatement.getLogical().equalsIgnoreCase(Statement.AND)) {
                            payloadConsistence = false; // that is = payloadConsistence && false;
                        } else {
                            if (triggerStatement.getLogical().equalsIgnoreCase(Statement.OR)) {
                                payloadConsistence = payloadConsistence || false;
                            }
                        }
                    }
                } else {
                    for (Statement eventStatement : filteredEventStatements) {
                        /*
                         * TODO: waring, supports only operand equal in event
                         * compared to equal, morethen, lessthen in triggers.
                         * Refacor with a strategy pattern.
                         */
                        if (eventStatement != null) {
                            //is setting a value must be not used to filter
                            if (triggerStatement.logical.equalsIgnoreCase("SET")) {
                                return true;
                            } else {
                                boolean isStatementConsistent
                                        = isStatementConsistent(triggerStatement.operand, triggerStatement.value,
                                                eventStatement.value);

                                if (triggerStatement.getLogical().equalsIgnoreCase(Statement.AND)) {
                                    payloadConsistence = payloadConsistence && isStatementConsistent; //true AND true; false AND true; false AND false; true AND false
                                } else {
                                    if (triggerStatement.getLogical().equalsIgnoreCase(Statement.OR)) {
                                        payloadConsistence = payloadConsistence || isStatementConsistent;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            payloadConsistence = false;
        }

        return payloadConsistence;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = (67 * hash) + ((this.payload != null) ? this.payload.hashCode() : 0);

        return hash;
    }

    private static boolean isStatementConsistent(String triggerOperand, String triggerValue, String eventValue) {
        if (triggerOperand.equalsIgnoreCase(Statement.EQUALS)) { //event operand="EQUALS", trigger operand="EQUALS"

            if (triggerValue.equalsIgnoreCase(eventValue) || (triggerValue.equals(Statement.ANY))) {
                return true;
            }
        }

        if (triggerOperand.equals(Statement.REGEX)) { //event operand="EQUALS", trigger operand="REGEX"

            Pattern pattern = Pattern.compile(triggerValue);
            Matcher matcher = pattern.matcher(eventValue);

            if (matcher.matches()) {
                return true;
            } else {
                return false;
            }
        }

        //applies only to integer values
        if (triggerOperand.equals(Statement.GREATER_THAN)) { //event operand="EQUALS", trigger operand="GREATER_THAN"

            try {
                Integer intReactionValue = new Integer(triggerValue);
                Integer intEventValue = new Integer(eventValue);

                if (intEventValue > intReactionValue) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                new RuntimeException(Statement.GREATER_THAN.toString()
                        + " operator can be applied only to integer values");

                return false;
            }
        }

        if (triggerOperand.equals(Statement.LESS_THAN)) { //event operand="EQUALS", trigger operand="LESS_THAN"

            try {
                Integer intReactionValue = new Integer(triggerValue);
                Integer intEventValue = new Integer(eventValue);

                if (intEventValue < intReactionValue) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                //is not a number
                new RuntimeException(Statement.LESS_THAN.toString()
                        + " operator can be applied only to integer values");

                return false;
            }
        }

        //applies only to integer values
        if (triggerOperand.equals(Statement.GREATER_EQUAL_THAN)) { //event operand="EQUALS", trigger operand="GREATER_THAN"

            try {
                Integer intReactionValue = new Integer(triggerValue);
                Integer intEventValue = new Integer(eventValue);

                if (intEventValue >= intReactionValue) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                new RuntimeException(Statement.GREATER_EQUAL_THAN.toString()
                        + " operator can be applied only to integer values");

                return false;
            }
        }

        if (triggerOperand.equals(Statement.LESS_EQUAL_THAN)) { //event operand="EQUALS", trigger operand="LESS_THAN"

            try {
                Integer intReactionValue = new Integer(triggerValue);
                Integer intEventValue = new Integer(eventValue);

                if (intEventValue <= intReactionValue) {
                    return true;
                } else {
                    return false;
                }
            } catch (NumberFormatException numberFormatException) {
                //is not a number
                new RuntimeException(Statement.LESS_EQUAL_THAN.toString()
                        + " operator can be applied only to integer values");

                return false;
            }
        }

        return false;
    }

    /**
     *
     * @param attribute
     * @return
     */
    public List<Statement> getStatements(String attribute) {
        ArrayList<Statement> statements = new ArrayList<Statement>();

        for (Statement i : payload) {
            if (i.getAttribute().equalsIgnoreCase(attribute)) {
                statements.add(i);
            }
        }

        return statements;
    }

    /**
     * Returns the value of the statement. BEWARE: there can be more the one
     * statement with the same key. This method returns the first occurrence
     * only.
     *
     * @param attribute is the key of the statement
     * @return the String value of the statement
     */
    public String getStatementValue(String attribute) {
        List<Statement> statements = getStatements(attribute);

        if ((statements != null) && !statements.isEmpty()) {
            return statements.get(0).getValue();
        }

        return "";
    }

    /**
     *
     * @return
     */
    public Iterator<Statement> iterator() {
        return payload.iterator();
    }

    /**
     *
     * @param anotherPayload
     */
    public void merge(Payload anotherPayload) {
        payload.addAll(anotherPayload.payload);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        Iterator<Statement> it = payload.iterator();
        buffer.append("{{");
        boolean first = true;
        while (it.hasNext()) {
            Statement s = it.next();
            if (first) {
                buffer.append(s.toString());
                first = false;
            } else {
                buffer.append("; ").append(s.toString());
            }
        }
        buffer.append("}}");
        return buffer.toString();
    }

    /**
     *
     */
    public void clear() {
        payload.clear();
    }
}
