/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.freedomotic.plugins.devices.zwave;

import com.freedomotic.model.object.Behavior;
import com.freedomotic.model.object.RangedIntBehavior;
import java.util.HashMap;
import java.util.Map;
import org.zwave4j.ZWave4j;

/**
 *
 * @author Matteo Mazzoni <matteo@bestmazzo.it>
 */
public class ZwaveUtils {

    public static final Map<Short,Behavior> commandClassToBehavior = new HashMap<Short,Behavior>();
    public static final Map<Short,String> commandClassToObjectType = new HashMap<Short,String>();
    
    public ZwaveUtils(){
      RangedIntBehavior t = new RangedIntBehavior();
      t.setName("temperature");
      commandClassToBehavior.put((short) 64, t);
      commandClassToObjectType.put((short) 64, "Thermostat");
      
      RangedIntBehavior b = new RangedIntBehavior();
      b.setName("battery");
      commandClassToBehavior.put((short) 128, b);
      
      RangedIntBehavior s = new RangedIntBehavior();
      s.setName("temperature");
      commandClassToBehavior.put((short) 67, s);
      commandClassToObjectType.put((short) 67, "Thermostat");
    }
    
    
}
