package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface BehaviorLogic {

    public void filterParams(Config params, boolean fireCommand);
    public String getName();
    public boolean isActive();
    public String getValueAsString();
}
