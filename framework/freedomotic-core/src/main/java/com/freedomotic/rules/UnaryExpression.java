/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

/**
 *
 * @author enrico
 * @param <E>
 */
public abstract class UnaryExpression<E> implements Expression {
    private E argument;
    
    public UnaryExpression(E argument) {
        this.argument = argument;
    }

    public E getArgument() {
        return argument;
    }

    public void setArgument(E argument) {
        this.argument = argument;
    }
    
}
