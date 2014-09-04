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
public abstract class BooleanExpression implements Expression<Boolean> {

    private String left;
    private String right;

    public BooleanExpression(String left, String right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public abstract Boolean evaluate();
    public abstract String getOperand();

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

}
