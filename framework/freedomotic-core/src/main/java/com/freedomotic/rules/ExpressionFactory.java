/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

/**
 *
 * @author nicoletti
 * @param <T>
 */
public class ExpressionFactory<T extends Expression> {

    public T createExpression(String left, String operand, String right) {
        if (operand.endsWith(Statement.EQUALS)) {
            return (T) new Equals(left, right);
        }
        if (operand.endsWith(Statement.REGEX)) {
            return (T) new Regex(left, right);
        }
        if (operand.endsWith(Statement.GREATER_THAN)) {
            return (T) new GreaterThan(left, right);
        }
        if (operand.endsWith(Statement.GREATER_EQUAL_THAN)) {
            return (T) new GreaterEqualThan(left, right);
        }
        if (operand.endsWith(Statement.LESS_THAN)) {
            return (T) new LessThan(left, right);
        }
        if (operand.endsWith(Statement.LESS_EQUAL_THAN)) {
            return (T) new LessEqualThan(left, right);
        }
        if (operand.endsWith(Statement.BETWEEN_TIME)) {
            return (T) new BetweenTime(left, right);
        }

        throw new UnsupportedOperationException("Operand " + operand + " is not a "
                + "recognised expression operand. HINT: check for spelling errors");
    }

}
