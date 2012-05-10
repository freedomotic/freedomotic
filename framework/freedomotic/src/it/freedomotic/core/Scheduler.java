package it.freedomotic.core;

import it.freedomotic.reactions.Command;
import it.freedomotic.reactions.Reaction;

/**
 *
 * @author Enrico
 */
public interface Scheduler {

    public void schedule(Command c);

    abstract void start();

    public void schedule(Reaction resolved);
}
