/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.model.charting;

import java.io.Serializable;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public interface GenericDataIface extends Serializable {
    
    /**
     *
     * @return
     */
    public GenericDataIface clone();

    /**
     *
     * @return
     */
    public String toJSON();
    
}
