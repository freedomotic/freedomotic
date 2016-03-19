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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Enrico Nicoletti
 */
public class LessEqualThan extends BinaryExpression {

    private static final String OPERATOR = Statement.LESS_EQUAL_THAN;
    private static final Logger LOG = LoggerFactory.getLogger(LessEqualThan.class.getName());

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
            LOG.warn(OPERATOR + " operator can be applied only to integer values");
            return false;
        }
    }

}
