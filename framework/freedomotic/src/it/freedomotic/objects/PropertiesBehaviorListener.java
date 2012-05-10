
package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author enrico
 */


public interface PropertiesBehaviorListener {
        
    public void propertyChanged(String key, String value, Config params, boolean fireCommand);
}
