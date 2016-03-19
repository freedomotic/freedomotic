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

/**
 *
 * @author Enrico Nicoletti
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
