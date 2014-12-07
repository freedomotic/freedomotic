/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.rules;

import com.freedomotic.exceptions.VariableResolutionException;
import com.freedomotic.model.ds.Config;
import com.freedomotic.reactions.Command;
import com.freedomotic.reactions.Reaction;
import com.freedomotic.reactions.Trigger;
import java.util.Map;

/**
 *
 * @author enrico
 */
public interface Resolver {

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Config aContext);

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Map<String, String> aContext);

    /**
     *
     * @param PREFIX
     * @param aContext
     */
    void addContext(final String PREFIX, final Payload aContext);

    /**
     * Creates a resolved clone of the reaction in input. All commands in the
     * reaction are resolved according to the context given in the contructor.
     *
     * @param r
     * @return a clone of the resolver reaction
     */
    Reaction resolve(Reaction r);

    /**
     * Creates a resolved clone of the command in input according to the current
     * context given in input to the constructor.
     *
     * @param c
     * @return
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    Command resolve(Command c) throws CloneNotSupportedException, VariableResolutionException;

    /**
     * Creates a resolved clone of the trigger in input according to the current
     * context given in input to the constructor.
     *
     * @param trigger
     * @return
     * @throws com.freedomotic.exceptions.VariableResolutionException
     */
    Trigger resolve(Trigger t) throws VariableResolutionException;
    
}
