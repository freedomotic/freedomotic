/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package it.freedomotic.environment;

import java.io.Serializable;

/**
 *
 * @author Enrico
 */
public class LastOutStrategy implements Ownership, Serializable {


    private static final long serialVersionUID = -4839776027684778640L;

	@Override
    public boolean canTriggerReactionsOnEnter(ZoneLogic z) {
        return true;
    }

    @Override
    public boolean canTriggerReactionsOnExit(ZoneLogic z) {
        if (z.howManyInside() <= 1) { //the last person in the zone is exiting from it
            return true;
        } else {
            return false;
        }
    }
}
