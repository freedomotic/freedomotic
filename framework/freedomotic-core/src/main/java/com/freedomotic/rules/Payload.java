/**
 *
 * Copyright (c) 2009-2016 Freedomotic team http://freedomotic.com
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
package com.freedomotic.rules;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public final class Payload implements Serializable {

    @XmlTransient
    private static final long serialVersionUID = -5799483105084939108L;
    private final List<Statement> payload = Collections.synchronizedList(new ArrayList<Statement>());

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

                // at this stage the trigger has already all the event.* properties embedded (shoud be skipped)
                if (triggerStatement.getAttribute().startsWith("event.")) {
                    //skip this iteration, and continue with the next statement
                    continue;
                }

                List<Statement> filteredEventStatements = eventPayload.getStatements(triggerStatement.getAttribute());

                if (filteredEventStatements.isEmpty()) {
                    //if the trigger has a property which is not in the event
                    if (!triggerStatement.getLogical().equalsIgnoreCase(Statement.SET)) {
                        //if it is AND/OR/...
                        return false;
                    }
                    //if the trigger has a property which is not in the event, BUT allowed value is ANY
                    if (triggerStatement.getValue().equalsIgnoreCase(Statement.ANY)) {
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
                            if (triggerStatement.getLogical().equalsIgnoreCase("SET")) {
                                return true;
                            } else {
                                boolean isStatementConsistent
                                        = isStatementConsistent(triggerStatement.getOperand(), triggerStatement.getValue(),
                                                eventStatement.getValue());

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
        ExpressionFactory factory = new ExpressionFactory<>();
        Expression exp = factory.createExpression(eventValue, triggerOperand, triggerValue);
        return (boolean) exp.evaluate();
    }

    /**
     *
     * @param attribute
     * @return
     */
    public List<Statement> getStatements(String attribute) {
        ArrayList<Statement> statements = new ArrayList<Statement>();

        synchronized (payload) {
            for (Statement i : payload) {
                if (i.getAttribute().equalsIgnoreCase(attribute)) {
                    statements.add(i);
                }
            }
        }

        return statements;
    }

    public List<Statement> getStatements() {
        return payload;
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
//        StringBuilder buffer = new StringBuilder();
//        Iterator<Statement> it = payload.iterator();
//        buffer.append("{{");
//        boolean first = true;
//        while (it.hasNext()) {
//            Statement s = it.next();
//            if (first) {
//                buffer.append(s.toString());
//                first = false;
//            } else {
//                buffer.append("; ").append(s.toString());
//            }
//        }
//        buffer.append("}}");
        return "";//buffer.toString();
    }

    /**
     *
     */
    public void clear() {
        payload.clear();
    }
}
