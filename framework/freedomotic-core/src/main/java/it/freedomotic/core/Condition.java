/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.core;

import it.freedomotic.reactions.Statement;

/**
 *
 * @author nicoletti
 */
public class Condition {
    private String target;
    private Statement statement;
    
    public Condition(String target, Statement statement){
        this.target=target;
        this.statement=statement;
    }
    
    public Condition(){
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }
}
