package it.freedomotic.objects;

import it.freedomotic.model.ds.Config;

/**
 *
 * @author Enrico
 */
public interface BooleanBehaviorListener {

    public void onTrue(Config params, boolean fireCommand);

    public void onFalse(Config params, boolean fireCommand);
}
