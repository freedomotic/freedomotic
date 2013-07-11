/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package es.gpulido.harvester;

import java.io.Serializable;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public interface DTPIface extends Serializable {
    
    public DTPIface clone();
    public String toJSON();
    
    
}
