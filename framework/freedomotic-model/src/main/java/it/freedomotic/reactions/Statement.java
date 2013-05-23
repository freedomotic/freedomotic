/**
 *
 * Copyright (c) 2009-2013 Freedomotic team
 * http://freedomotic.com
 *
 * This file is part of Freedomotic
 *
 * This Program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This Program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Freedomotic; see the file COPYING.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.reactions;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class Statement
        implements Serializable {

    private static final long serialVersionUID = -6983128779561551125L;
	
	public static final String EQUALS = "EQUALS";
    public static final String GREATER_THEN = "GREATER_THEN";
    public static final String LESS_THEN = "LESS_THEN";
    public static final String GREATER_EQUAL_THEN = "GREATER_EQUAL_THEN";
    public static final String LESS_EQUAL_THEN = "LESS_EQUAL_THEN";
    public static final String REGEX = "REGEX";
    public static final String AND = "AND";
    public static final String OR = "OR";
    public static final String NOT = "NOT";
    public static final String ANY = "ANY";
    public String logical;
    public String attribute;
    public String operand;
    public String value;

    protected Statement create(String logical, String attribute, String operand, String value) {
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

    public String getAttribute() {
        return attribute;
    }

    public String getValue() {
        if (value != null) {
            return value;
        } else {
            return "";
        }
    }

    public String getOperand() {
        return operand;
    }

    public String getLogical() {
        return logical;
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    public void setLogical(String logical) {
        this.logical = logical;
    }

    public void setOperand(String operand) {
        this.operand = operand;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return attribute + " " + operand + " " + value;
    }

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
        int hash = 3;

        return hash;
    }
}
