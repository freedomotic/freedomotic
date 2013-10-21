/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.model.charting;

import java.io.Serializable;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public interface GenericDataIface extends Serializable {
    
    public GenericDataIface clone();
    public String toJSON();
    
}
