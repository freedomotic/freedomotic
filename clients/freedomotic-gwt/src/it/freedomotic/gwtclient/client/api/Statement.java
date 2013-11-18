package it.freedomotic.gwtclient.client.api;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class Statement implements Serializable {

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
        if ((this.logical == null) ? (other.logical != null) : !this.logical.equals(other.logical)) {
            return false;
        }
        if ((this.attribute == null) ? (other.attribute != null) : !this.attribute.equals(other.attribute)) {
            return false;
        }
        if ((this.operand == null) ? (other.operand != null) : !this.operand.equals(other.operand)) {
            return false;
        }
        if ((this.value == null) ? (other.value != null) : !this.value.equals(other.value)) {
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
