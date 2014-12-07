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
public class LessEqualThan extends BinaryExpression {

    private static final String OPERATOR = Statement.LESS_EQUAL_THAN;
    private static final Logger LOG = Logger.getLogger(LessEqualThan.class.getName());

    @Override
    public String getOperand() {
        return OPERATOR;
    }

    public LessEqualThan(String left, String right) {
        super(left, right);
    }

    @Override
    public Boolean evaluate() {
        try {
            Integer intRightValue = new Integer(getRight());
            Integer intLeftValue = new Integer(getLeft());

            return intLeftValue <= intRightValue;
        } catch (NumberFormatException nfe) {
            LOG.warning(OPERATOR  + " operator can be applied only to integer values");
            return false;
        }
    }

}
