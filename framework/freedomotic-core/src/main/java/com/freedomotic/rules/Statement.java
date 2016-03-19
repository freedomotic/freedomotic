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
import java.util.Objects;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Enrico Nicoletti
 */
@XmlRootElement
public class Statement
        implements Serializable {

    private static final long serialVersionUID = -6983128779561551125L;

    public static final String EQUALS = "EQUALS";
    public static final String GREATER_THAN = "GREATER_THAN";
    public static final String LESS_THAN = "LESS_THAN";
    public static final String GREATER_EQUAL_THAN = "GREATER_EQUAL_THAN";
    public static final String LESS_EQUAL_THAN = "LESS_EQUAL_THAN";
    public static final String REGEX = "REGEX";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT = "NOT";
    public static final String ANY = "ANY";
    public static final String SET = "SET";
    public static final String BETWEEN_TIME = "BETWEEN_TIME";

    /**
     *
     */
    private String logical;

    /**
     *
     */
    private String attribute;

    /**
     *
     */
    private String operand;

    /**
     *
     */
    private String value;

    /**
     *
     * @param logical
     * @param attribute
     * @param operand
     * @param value
     * @return
     */
    public Statement create(String logical, String attribute, String operand, String value) {
        if ((attribute != null) && (value != null)) {
            if ((attribute.trim().length() != 0) && (value.trim().length() != 0)) {
                this.logical = logical;
                this.attribute = attribute;
                this.operand = operand;
                this.value = value;

                return this;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     *
     * @return
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     *
     * @return
     */
    public String getValue() {
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    /**
     *
     * @return
     */
    public String getOperand() {
        return operand;
    }

    /**
     *
     * @return
     */
    public String getLogical() {
        return logical;
    }

    /**
     *
     * @param attribute
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     *
     * @param logical
     */
    public void setLogical(String logical) {
        this.logical = logical;
    }

    /**
     *
     * @param operand
     */
    public void setOperand(String operand) {
        this.operand = operand;
    }

    /**
     *
     * @param value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return new StringBuilder()
                .append(attribute).append(" ")
                .append(operand).append(" ")
                .append(value).toString();
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Statement other = (Statement) obj;

        if ((this.logical == null) ? (other.logical != null) : (!this.logical.equals(other.logical))) {
            return false;
        }

        if ((this.attribute == null) ? (other.attribute != null) : (!this.attribute.equals(other.attribute))) {
            return false;
        }

        if ((this.operand == null) ? (other.operand != null) : (!this.operand.equals(other.operand))) {
            return false;
        }

        if ((this.value == null) ? (other.value != null) : (!this.value.equals(other.value))) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.logical);
        hash = 43 * hash + Objects.hashCode(this.attribute);
        hash = 43 * hash + Objects.hashCode(this.operand);
        hash = 43 * hash + Objects.hashCode(this.value);
        return hash;
    }
}
