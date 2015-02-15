/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.reactions;

import com.freedomotic.persistence.Repository;
import java.io.File;

/**
 *
 * @author enrico
 */
public interface TriggerRepository extends Repository<Trigger> {
    
    //TODO: remove from here
    public void loadTriggers(File folder);
    public void saveTriggers(File folder);
}
