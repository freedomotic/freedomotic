/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

/**
 *
 * @author nicoletti
 */
public class Equals extends BinaryExpression {

    private static final String OPERATOR = Statement.EQUALS;

    @Override
    public String getOperand() {
        return OPERATOR;
    }

    public Equals(String left, String right) {
        super(left, right);
    }

    /**
     *
     * @return
     */
    @Override
    public Boolean evaluate() {
        if (getLeft().equalsIgnoreCase(getRight()) 
                || (getRight().equals(Statement.ANY))) {
            return true;
        }
        return false;
    }
}
