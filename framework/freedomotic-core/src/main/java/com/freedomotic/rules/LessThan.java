/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

import java.util.logging.Logger;

/**
 *
 * @author nicoletti
 */
public class LessThan extends BinaryExpression {

    private static final String OPERATOR = Statement.LESS_THAN;
    private static final Logger LOG = Logger.getLogger(LessThan.class.getName());

    @Override
    public String getOperand() {
        return OPERATOR;
    }

    public LessThan(String left, String right) {
        super(left, right);
    }

    @Override
    public Boolean evaluate() {
        try {
            Integer intRightValue = new Integer(getRight());
            Integer intLeftValue = new Integer(getLeft());

            return intLeftValue < intRightValue;
        } catch (NumberFormatException nfe) {
            LOG.warning(Statement.LESS_THAN  + " operator can be applied only to integer values");
            return false;
        }
    }

}
