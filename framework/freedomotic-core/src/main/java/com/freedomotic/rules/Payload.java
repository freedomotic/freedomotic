/**
 *
 * Copyright (c) 2009-2020 Freedomotic Team http://www.freedomotic-iot.com
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

import com.google.gson.Gson;
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
    private final List<Statement> payloadLst = Collections.synchronizedList(new ArrayList<>());

    /**
     *
     * @param logical
     * @param attribute
     * @param operand
     * @param value
     */
    public void addStatement(String logical, String attribute, String operand, String value) {
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
     * Adds a statement to the payload.
     * 
     * @param s statement to add
     */
    public void enqueueStatement(Statement s) {
        if ((s != null) && !payloadLst.contains(s)) {
            payloadLst.add(s);
        }
    }

    /**
     * Returns the number of statements.
     * 
     * @return
     */
    public int size() {
        return payloadLst.size();
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
            Iterator<Statement> it = payloadLst.iterator();

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
                         * TODO: waring, supports only operand EQUAL in events
                         * compared to EQUAL, MORE THAN, LESS THAN in triggers.
                         * Refactor with a strategy pattern.
                         */
                        if (eventStatement != null) {
                            //is setting a value must be not used to filter
                            if ("SET".equalsIgnoreCase(triggerStatement.getLogical())) {
                                return true;
                            } else {
                                boolean isStatementConsistent = isStatementConsistent(triggerStatement.getOperand(), triggerStatement.getValue(), eventStatement.getValue());
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
        hash = (67 * hash) + ((this.payloadLst != null) ? this.payloadLst.hashCode() : 0);

        return hash;
    }

    /**
     * Checks the statements consistency.
     * 
     * @param triggerOperand
     * @param triggerValue
     * @param eventValue
     * @return 
     */
    private static boolean isStatementConsistent(String triggerOperand, String triggerValue, String eventValue) {
        ExpressionFactory factory = new ExpressionFactory<>();
        Expression exp = factory.createExpression(eventValue, triggerOperand, triggerValue);
        return (boolean) exp.evaluate();
    }

    /**
     * Gets a list of statements given an attribute.
     * 
     * @param attribute
     * @return a list of statements
     */
    public List<Statement> getStatements(String attribute) {
        List<Statement> statements = new ArrayList<>();

        synchronized (payloadLst) {
            payloadLst.stream().filter((i) -> (i.getAttribute().equalsIgnoreCase(attribute))).forEachOrdered((i) -> {
                statements.add(i);
            });
        }
        return statements;
    }

    /**
     * Gets the list of statements.
     * 
     * @return the list of statements
     */
    public List<Statement> getStatements() {
        return payloadLst;
    }
    
    /**
     * Gets the statements in JSON format.
     * 
     * @return 
     */
    public String getStatementsAsJson() {
        return new Gson().toJson(payloadLst);
    }

    /**
     * Returns the value of the statement. BEWARE: there can be more the one
     * statement with the same key. This method returns the first occurrence
     * only.
     *
     * @param attribute the key of the statement
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
        return payloadLst.iterator();
    }

    /**
     *
     * @param anotherPayload
     */
    public void merge(Payload anotherPayload) {
        payloadLst.addAll(anotherPayload.payloadLst);
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return "";
    }

    /**
     *
     */
    public void clear() {
        payloadLst.clear();
    }
}
