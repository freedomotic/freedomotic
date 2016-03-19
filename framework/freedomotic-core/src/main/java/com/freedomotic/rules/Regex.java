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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Enrico Nicoletti
 */
public class Regex extends BinaryExpression {

    private static final String OPERATOR = Statement.REGEX;

    public Regex(String left, String right) {
        super(left, right);
    }

    @Override
    public Boolean evaluate() {
        Pattern pattern = Pattern.compile(getRight());
        Matcher matcher = pattern.matcher(getLeft());

        return matcher.matches();
    }

    @Override
    public String getOperand() {
        return OPERATOR;
    }

}
