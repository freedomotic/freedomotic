/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.core;

import com.freedomotic.reactions.Statement;

/**
 *
 * @author nicoletti
 */
public class Condition {
    private String target;
    private Statement statement;
    
    /**
     *
     * @param target
     * @param statement
     */
    public Condition(String target, Statement statement){
        this.target=target;
        this.statement=statement;
    }
    
    /**
     *
     */
    public Condition(){
    }

    /**
     *
     * @return
     */
    public String getTarget() {
        return target;
    }

    /**
     *
     * @param target
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     *
     * @return
     */
    public Statement getStatement() {
        return statement;
    }

    /**
     *
     * @param statement
     */
    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
