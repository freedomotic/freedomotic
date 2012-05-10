
package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface ListBehaviorListener  {

    public void selectedChanged(Config params, boolean fireCommand);
}