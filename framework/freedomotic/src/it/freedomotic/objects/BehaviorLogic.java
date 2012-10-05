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
    
    public boolean isChanged();
    
    public void setChanged(boolean value);

    public boolean isActive();

    public String getValueAsString();
    public final String VALUE_OPPOSITE = "opposite";
    public final String VALUE_PREVIOUS = "previous";
    public final String VALUE_NEXT = "next";
}
