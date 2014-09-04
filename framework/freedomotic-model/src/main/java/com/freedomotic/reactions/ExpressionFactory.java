/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

/**
 *
 * @author nicoletti
 * @param <T>
 */
public class ExpressionFactory<T extends Expression> {

    public T createExpression(String left, String operand, String right) {
        if (operand.endsWith(Statement.EQUALS)) {
            return (T) new EqualsExpression(left, right);
        }
        if (operand.endsWith(Statement.REGEX)) {
            return (T) new RegexExpression(left, right);
        }
        if (operand.endsWith(Statement.GREATER_THAN)) {
            return (T) new GreaterThanExpression(left, right);
        }
        if (operand.endsWith(Statement.GREATER_EQUAL_THAN)) {
            return (T) new GreaterEqualThanExpression(left, right);
        }
        if (operand.endsWith(Statement.LESS_THAN)) {
            return (T) new LessThanExpression(left, right);
        }
        if (operand.endsWith(Statement.LESS_EQUAL_THAN)) {
            return (T) new LessEqualThanExpression(left, right);
        }
        if (operand.endsWith(Statement.BETWEEN_TIME)) {
            return (T) new BetweenTimeExpression(left, right);
        }

        throw new UnsupportedOperationException("Operand " + operand + " is not a "
                + "recognised expression operand. HINT: check for spelling errors");
    }

}
