/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

/**
 *
 * @author nicoletti
 */
public class EqualsExpression extends BooleanExpression {

    private static final String OPERATOR = Statement.EQUALS;

    @Override
    public String getOperand() {
        return OPERATOR;
    }

    public EqualsExpression(String left, String right) {
        super(left, right);
    }

    @Override
    public Boolean evaluate() {
        if (getLeft().equalsIgnoreCase(getRight()) 
                || (getRight().equals(Statement.ANY))) {
            return true;
        }
        return false;
    }
    
    

}
