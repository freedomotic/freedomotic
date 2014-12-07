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
public interface Expression<T> {
    
    public T evaluate();
    public String getOperand();
    
}
