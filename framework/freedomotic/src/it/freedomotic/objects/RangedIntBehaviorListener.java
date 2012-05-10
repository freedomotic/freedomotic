package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface RangedIntBehaviorListener {

    public void onLowerBoundValue(Config params, boolean fireCommand);

    public void onUpperBoundValue(Config params, boolean fireCommand);

    public void onRangeValue(int rangeValue, Config params, boolean fireCommand);
}
