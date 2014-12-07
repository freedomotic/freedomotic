/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

/**
 *
 * @author enrico
 */
public class Not extends UnaryExpression<Boolean> {

    private static final String OPERATOR = Statement.NOT;

    public Not(Boolean argument) {
        super(argument);
    }

    @Override
    public Boolean evaluate() {
        return !getArgument();
    }

    @Override
    public String getOperand() {
        return OPERATOR;
    }

}
