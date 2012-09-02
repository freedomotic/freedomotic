package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface BehaviorLogic {
    /**
     *
     * @param params
     * @param fireCommand
     */
    public void filterParams(final Config params, boolean fireCommand);
    public String getName();
    public boolean isActive();
    public String getValueAsString();
}
