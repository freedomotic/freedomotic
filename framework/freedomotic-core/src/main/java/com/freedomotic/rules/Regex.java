/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author nicoletti
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
